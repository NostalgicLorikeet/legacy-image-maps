package nostalgic.legacyimagemaps.imagemaps;

import com.google.common.hash.Hashing;
import ditherer.Ditherer;
import net.minecraft.block.material.MapColor;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import nostalgic.legacyimagemaps.config.LegacyImageMapsConfig;
import nostalgic.legacyimagemaps.imagemaps.cache.CacheAll;
import nostalgic.legacyimagemaps.legacyimagemaps.Tags;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class ImageMap {
    static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    static final long connectionMaxSize = LegacyImageMapsConfig.options.maxImageSize;
    static final int maxImageDimensions = LegacyImageMapsConfig.options.maxImageDimensions;
    static final int mapItemDimension = LegacyImageMapsConfig.options.mapItemDimension;
    static final int maxMapCount = LegacyImageMapsConfig.options.maxMapCount;
    static final Ditherer ditherer = new Ditherer(PaletteHolder.ditheringPalette);
    final ICommandSender sender;

    BufferedImage image;
    BufferedImage imageScaled;
    BufferedImage[][] images;
    byte[][] byteMaps;
    ItemStack[] maps;

    public int realStart;

    public ImageMap(ICommandSender sender) {
        this.sender = sender;
    }

    //do NOT run this off thread, it can and probably will crash the game with concurrent modification, or be a duplication exploit
    public ItemStack[] convertByteArraysToItemStacks() {
        World world = sender.getEntityWorld();

        maps = new ItemStack[byteMaps.length];

        for (int i = 0; i < byteMaps.length; i++) {
            long hash = Hashing.murmur3_128().hashBytes(byteMaps[i]).asLong();
            ItemStack imageMapItem;

            if (CacheAll.byteMapToItemStackMap.containsKey(hash)) {
                imageMapItem = CacheAll.byteMapToItemStackMap.get(hash).copy();
            } else {
                imageMapItem = new ItemStack(Items.FILLED_MAP);
                int imageMapId = world.getMapStorage().getUniqueDataId("map");
                String imageMapName = "map_" + imageMapId;
                MapData imageMapData = new MapData(imageMapName);
                imageMapData.scale = 3;
                imageMapData.dimension = (byte) mapItemDimension;
                imageMapData.calculateMapCenter(LegacyImageMapsConfig.options.mapX, LegacyImageMapsConfig.options.mapZ, imageMapData.scale);

                imageMapData.colors = byteMaps[i];

                world.getMapStorage().setData(imageMapName, imageMapData);
                imageMapData.markDirty();
                imageMapItem.setItemDamage(imageMapId);
                //if (LegacyImageMapsConfig.options.giveMapsCoordNames) {
                //    imageMapItem.setStackDisplayName("ImageMap " + x + "," + y);
                //}
                CacheAll.byteMapToItemStackMap.put(hash, imageMapItem.copy());
            }

            maps[i] = imageMapItem;
        }

        return maps;
    }

    public void convertImagesToByteArray() {
        convertImagesToByteArray(LegacyImageMapsConfig.options.transparencyThreshold);
    }

    public void convertImagesToByteArray(int start, int end) {
        convertImagesToByteArray(start, end, LegacyImageMapsConfig.options.transparencyThreshold);
    }

    public void convertImagesToByteArray(int transparencyThreshold) {
        convertImagesToByteArray(0,(images.length * images[0].length)-1, transparencyThreshold);
    }

    public void convertImagesToByteArray(int start, int end, int transparencyThreshold) {
        if (start > end) {
            start = 0;
            end = this.getImageSegmentArrayMaxValue();
        }
        if (start < 0) {
            start = 0;
        }
        if (end > this.getImageSegmentArrayMaxValue()) {
            end = this.getImageSegmentArrayMaxValue();
        }

        int current = 0;
        int currentProcessing = 0;
        byte[][] thisByteMaps = new byte[end-start+1][16384];

        for (int x = 0; x < images.length; x++) {
            for (int y = 0; y < images[x].length; y++) {
                if (current >= start && current <= end) {
                    String hash = getImageHash(images[x][y]);
                    byte[] colors = new byte[16384];
                    if (CacheAll.byteMap.containsKey(hash)) {
                        colors = CacheAll.byteMap.get(hash);
                    } else {
                        for (int pixelY = 0; pixelY < images[x][y].getHeight(); pixelY++) {
                            for (int pixelX = 0; pixelX < images[x][y].getWidth(); pixelX++) {
                                int clr = images[x][y].getRGB(pixelX, pixelY);
                                int r = (clr & 0x00ff0000) >> 16;
                                int g = (clr & 0x0000ff00) >> 8;
                                int b = clr & 0x000000ff;
                                int a = (clr >> 24) & 0xFF;

                                double currentClosestDistance = Double.MAX_VALUE;
                                int colorClosest = 0;

                                if (a <= transparencyThreshold) {
                                    colorClosest = 0;
                                } else {
                                    Color colorAsColor = new Color(r, g, b);

                                    if (CacheAll.colorMap.containsKey(colorAsColor.getRGB())) {
                                        colorClosest = CacheAll.colorMap.get(colorAsColor.getRGB());
                                    } else {
                                        for (MapColor colorCompare : MapColor.COLORS) {
                                            if (colorCompare == null || colorCompare == MapColor.AIR) continue;

                                            for (int i = 0; i < 4; i++) {
                                                Color color = new Color(colorCompare.getMapColor(i));

                                                int rDist = r - color.getRed();
                                                int gDist = g - color.getGreen();
                                                int bDist = b - color.getBlue();
                                                double rAvg = (r + color.getRed()) / 2.0;

                                                double colorDistance;

                                                if (LegacyImageMapsConfig.options.useEuclideanDistance) {
                                                    colorDistance = rDist * rDist +
                                                            gDist * gDist +
                                                            bDist * bDist;
                                                } else {
                                                    colorDistance = (2.0 + rAvg / 256.0) * rDist * rDist +
                                                            4.0 * gDist * gDist +
                                                            (2.0 + (255.0 - rAvg) / 256.0) * bDist * bDist;
                                                }

                                                if (colorDistance < currentClosestDistance) {
                                                    currentClosestDistance = colorDistance;
                                                    colorClosest = (colorCompare.colorIndex * 4 + i);
                                                }
                                            }
                                        }
                                        CacheAll.colorMap.put(colorAsColor.getRGB(), colorClosest);
                                    }
                                }
                                colors[(pixelY * 128) + pixelX] = (byte) colorClosest;
                            }
                        }

                        CacheAll.byteMap.put(hash, colors);
                    }
                    thisByteMaps[currentProcessing] = colors;
                    currentProcessing++;
                }
                current++;
            }
        }

        byteMaps = thisByteMaps;
        notifyServer(I18n.translateToLocal("legacyimagemaps.image_converted_count") + " (" + byteMaps.length + ")");

        realStart = start;
    }

    public int getImageSegmentArrayMaxValue() {
        return (images.length*images[0].length)-1;
    }

    public void prepareArray() {
        int imageScaledWidth = imageScaled.getWidth()/128;
        int imageScaledHeight = imageScaled.getHeight()/128;

        images = new BufferedImage[imageScaledWidth][imageScaledHeight];

        for (int x = 0; x < imageScaledWidth; x++) {
            for (int y = 0; y < imageScaledHeight; y++) {
                BufferedImage tile = new BufferedImage(128, 128, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D g = tile.createGraphics();
                g.drawImage(imageScaled, 0, 0, 128, 128, x * 128, y * 128, (x + 1) * 128, (y + 1) * 128, null);
                g.dispose();

                images[x][y] = tile;
            }
        }
    }

    public void dither() {
        if (LegacyImageMapsConfig.options.allowDithering) {
            ditherer.dither(imageScaled);
        }
    }

    public boolean scaleImage(int scale, boolean noLetterbox, boolean removeAlpha, Color color, boolean test) {
        int scaleWidth;
        int scaleHeight;

        int widthOriginal = image.getWidth();
        int heightOriginal = image.getHeight();

        boolean landscape = widthOriginal > heightOriginal;

        if (landscape) {
            scaleWidth = scale;
            double ratio = (double)heightOriginal/widthOriginal;
            int scaledHeightOnMap = (int)((scale*128)*ratio);
            scaleHeight = scaledHeightOnMap <= 128 ? 1 : (scaledHeightOnMap % 128 == 0 ? scaledHeightOnMap/128 : (scaledHeightOnMap/128)+1);
        } else {
            scaleHeight = scale;
            double ratio = (double)widthOriginal/heightOriginal;
            int scaledWidthOnMap = (int)((scale*128)*ratio);
            scaleWidth = scaledWidthOnMap <= 128 ? 1 : (scaledWidthOnMap % 128 == 0 ? scaledWidthOnMap/128 : (scaledWidthOnMap/128)+1);
        }

        return scaleImage(scaleWidth, scaleHeight, noLetterbox, removeAlpha, color, test);
    }

    public boolean scaleImage(int width, int height, boolean noLetterbox, boolean removeAlpha, Color color, boolean test) {
        if (!test) {
            imageScaled = new BufferedImage(width * 128, height * 128, BufferedImage.TYPE_4BYTE_ABGR);
            boolean landscape = image.getWidth() > image.getHeight();

            int widthTotal = width * 128;
            int heightTotal = height * 128;

            Graphics2D graphics = imageScaled.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            if (removeAlpha) {
                graphics.setColor(color);
                graphics.fillRect(0, 0, width * 128, height * 128);
            }

            if (noLetterbox) {
                graphics.drawImage(image, 0, 0, widthTotal, heightTotal, null);
            } else {
                double scaleWidth = (double) widthTotal / image.getWidth();
                double scaleHeight = (double) heightTotal / image.getHeight();
                double scale = Math.min(scaleWidth, scaleHeight);
                int finalW = (int) (image.getWidth() * scale);
                int finalH = (int) (image.getHeight() * scale);
                int x = (widthTotal - finalW) / 2;
                int y = (heightTotal - finalH) / 2;

                graphics.drawImage(image, x, y, finalW, finalH, null);
            }

            graphics.dispose();
            notifyServer(I18n.translateToLocal("legacyimagemaps.image_scaled") + " " + imageScaled.getWidth()+"x"+imageScaled.getHeight());

            if ((width * height) > maxMapCount) {
                notifyBoth(I18n.translateToLocal("legacyimagemaps.image_converted_too_big") + " (" + (width*height) + "/" + maxMapCount + ")",true);
                return false;
            }

            return true;
        } else {
            notifySender(I18n.translateToLocal("legacyimagemaps.image_required_map_count") + " " + width*height);
            return false;
        }
    }

    public String getImageHash() {
        return getImageHash(image);
    }

    public String getImageHash(BufferedImage imageA) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(((DataBufferByte) imageA.getRaster().getDataBuffer()).getData()));
        } catch (NoSuchAlgorithmException e) {
            notifyServer(String.valueOf(I18n.translateToLocal("legacyimagemaps.image_missing_hash")));
            return null;
        }
    }

    public int[] getImageDimensions() {
        return new int[]{image.getWidth(),image.getHeight()};
    }

    public void setImageDirectly(BufferedImage image) {
        this.image = image;
    }

    public boolean downloadImageFromURL(String urlString) {
        HttpURLConnection connection = null;
        URL url;
        try {
            url = new URL(urlString);
            InetAddress address = InetAddress.getByName(url.getHost());
            if (address.isAnyLocalAddress() || address.isLinkLocalAddress() || address.isSiteLocalAddress() || address.isLoopbackAddress()) {
                throw new Exception();
            }
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", LegacyImageMapsConfig.options.userAgent);
            long connectionSize = connection.getContentLengthLong();

            if (!(connectionSize > connectionMaxSize)) {
                image = ImageIO.read(connection.getInputStream());

                int imageWidth = image.getWidth();
                int imageHeight = image.getHeight();

                if (imageWidth > maxImageDimensions || imageHeight > maxImageDimensions) {
                    notifyBoth(I18n.translateToLocal("legacyimagemaps.image_too_big") +
                            " (" + imageWidth + "x" + imageHeight + " / " + maxImageDimensions + "x" + maxImageDimensions + ")", true);
                }
            } else {
                notifyBoth(I18n.translateToLocal("legacyimagemaps.file_too_big") + " (" + connectionSize + "/" + connectionMaxSize + ")", true);
                return false;
            }
            connection.getInputStream().close();
            connection.disconnect();
        } catch (Exception e) {
            notifyBoth(String.valueOf(I18n.translateToLocal("legacyimagemaps.invalid_image_or_url")), true);
            if (connection != null) {
                connection.disconnect();
            }
            return false;
        }

        notifyServer(I18n.translateToLocal("legacyimagemaps.image_from_url_success") + " (" + url + ")");
        return true;
    }

    public void notifySender(String message) {
        notifySender(message, false);
    }

    public void notifySender(String message, Boolean error) {
        TextComponentString tcMessage = new TextComponentString(message);
        if (error) tcMessage.setStyle(new Style().setColor(TextFormatting.RED));

        sender.sendMessage(tcMessage);
    }

    public static void notifyServer(String message) {
        notifyServer(message, false);
    }

    public static void notifyServer(String message, Boolean error) {
        if (!error) {
            LOGGER.info(message, Tags.MOD_NAME);
        } else {
            LOGGER.error(message, Tags.MOD_NAME);
        }
    }

    public void notifyBoth (String message, Boolean error) {
        notifySender(message,error);
        notifyServer(message,error);
    }

    public void notifyBoth (String message) {
        notifySender(message,false);
        notifyServer(message,false);
    }
}