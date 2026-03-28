package nostalgic.legacyimagemaps.imagemaps;

import net.minecraft.block.material.MapColor;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
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
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class ImageMap {
    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    public static final long connectionMaxSize = LegacyImageMapsConfig.options.maxImageSize;
    public static final long maxImageDimensions = LegacyImageMapsConfig.options.maxImageDimensions;
    public static final int mapItemDimension = LegacyImageMapsConfig.options.mapItemDimension;
    public static final int maxMapCount = LegacyImageMapsConfig.options.maxMapCount;
    public final ICommandSender sender;

    BufferedImage image;
    BufferedImage imageScaled;
    BufferedImage[][] images;
    byte[][] byteMaps;
    ItemStack[] maps;

    public ImageMap(ICommandSender sender) {
        this.sender = sender;
    }

    //do NOT run this off thread, it can and probably will crash the game with concurrent modification, or be a duplication exploit
    public ItemStack[] convertByteArraysToItemStacks() {
        World world = sender.getEntityWorld();

        maps = new ItemStack[byteMaps.length];

        for (int i = 0; i < byteMaps.length; i++) {
            ItemStack imageMapItem = new ItemStack(Items.FILLED_MAP);
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

            maps[i] = imageMapItem;
        }

        return maps;
    }

    public void convertImagesToByteArray() {
        convertImagesToByteArray(127);
    }

    public void convertImagesToByteArray(int transparencyThreshold) {
        convertImagesToByteArray(0,(images.length * images[0].length)-1, transparencyThreshold);
    }

    public void convertImagesToByteArray(int start, int end, int transparencyThreshold) {
        int current = 0;
        int currentProcessing = 0;
        byte[][] thisByteMaps = new byte[end-start+1][16384];
        if (start < 0) {
            start = 0;
        }
        if (end > thisByteMaps.length-1) {
            end = thisByteMaps.length-1;
        }

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

                                                double colorDistance = rDist * rDist +
                                                        gDist * gDist +
                                                        bDist * bDist;

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
        notifyServer(new TextComponentTranslation("legacyimagemaps.image_converted_count") + " (" + byteMaps.length + ")");
    }

    public void prepareArray() {
        int imageScaledWidth = imageScaled.getWidth()/128;
        int imageScaledHeight = imageScaled.getHeight()/128;

        images = new BufferedImage[imageScaledWidth][imageScaledHeight];

        for (int x = 0; x < imageScaledWidth; x++) {
            for (int y = 0; y < imageScaledHeight; y++) {
                images[x][y] = imageScaled.getSubimage(x * 128, y * 128, 128, 128);
            }
        }
    }

    public boolean scaleImage(int scale, boolean letterbox, boolean preserveTransparency, Color color, boolean test) {
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

        return scaleImage(scaleWidth, scaleHeight, letterbox, preserveTransparency, color, test);
    }

    public boolean scaleImage(int width, int height, boolean letterbox, boolean preserveTransparency, Color color, boolean test) {
        if (!test) {
            imageScaled = new BufferedImage(width * 128, height * 128, BufferedImage.TYPE_4BYTE_ABGR);
            boolean landscape = image.getWidth() > image.getHeight();

            int widthTotal = width * 128;
            int heightTotal = height * 128;

            Graphics graphics = imageScaled.createGraphics();

            if (!preserveTransparency) {
                graphics.setColor(color);
                graphics.fillRect(0, 0, width * 128, height * 128);
            }

            if (!letterbox) {
                graphics.drawImage(image, 0, 0, widthTotal, heightTotal, null);
            } else {
                if (landscape) {
                    double heightFinal = ((double) widthTotal/image.getWidth())*image.getHeight();
                    graphics.drawImage(image, 0, (heightTotal - (int)heightFinal)/2, widthTotal, (int)heightFinal, null);
                } else {
                    double widthFinal = ((double) heightTotal/image.getHeight())*image.getWidth();
                    graphics.drawImage(image, (widthTotal - (int)widthFinal)/2, 0, (int)widthFinal, heightTotal, null);
                }
            }

            graphics.dispose();
            notifyServer(new TextComponentTranslation("legacyimagemaps.image_scaled") + " " + imageScaled.getWidth()+"x"+imageScaled.getHeight());

            if ((width * height) > maxMapCount) {
                notifyBoth(new TextComponentTranslation("legacyimagemaps.image_converted_too_big") + " (" + (width*height) + "/" + maxMapCount + ")",true);
                return false;
            }

            return true;
        } else {
            notifySender(new TextComponentTranslation("legacyimagemaps.image_required_map_count") + " " + width*height);
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
            notifyServer(String.valueOf(new TextComponentTranslation("legacyimagemaps.image_missing_hash")));
            return null;
        }
    }

    public int[] getImageDimensions() {
        return new int[]{image.getWidth(),image.getHeight()};
    }

    public void setImageDirectly(BufferedImage image) {
        this.image = image;
    }

    public boolean downloadImageFromURL(String url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36");
            long connectionSize = connection.getContentLengthLong();

            if (!(connectionSize > connectionMaxSize)) {
                image = ImageIO.read(connection.getInputStream());

                int imageWidth = image.getWidth();
                int imageHeight = image.getHeight();

                if (imageWidth > maxImageDimensions || imageHeight > maxImageDimensions) {
                    notifyBoth(new TextComponentTranslation("legacyimagemaps.image_too_big") +
                            " (" + imageWidth + "x" + imageHeight + " / " + maxImageDimensions + "x" + maxImageDimensions + ")",true);
                }
            } else {
                notifyBoth(new TextComponentTranslation("legacyimagemaps.file_too_big") + " (" + connectionSize + "/" + connectionMaxSize + ")",true);
                return false;
            }
            connection.disconnect();
        } catch (Exception e) {
            notifyBoth(String.valueOf(new TextComponentTranslation("legacyimagemaps.invalid_image_or_url")),true);
            if (connection != null) {
                connection.disconnect();
            }
            return false;
        }

        notifyServer(new TextComponentTranslation("legacyimagemaps.image_from_url_success") + " (" + url + ")");
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