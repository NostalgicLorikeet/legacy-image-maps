package nostalgic.legacyimagemaps.imagemaps;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import nostalgic.legacyimagemaps.config.LegacyImageMapsConfig;

import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageMapRequest {
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(2);
    private final World world;
    private final ICommandSender sender;
    private final ImageMap imagemap;
    private ItemStack[] maps;

    public ImageMapRequest(ICommandSender sender, String[] args) {
        world = sender.getEntityWorld();
        imagemap = new ImageMap(sender);
        this.sender = sender;

        if (!world.isRemote) {
            THREAD_POOL.execute(() -> {
                String url = "";
                int scale = -1;
                int width;
                int height;
                int start = 0;
                int end = -1;
                int transparencyThreshold = LegacyImageMapsConfig.options.transparencyThreshold;
                boolean noLetterbox = false;
                boolean removeAlpha = false;
                Color color = Color.decode("#000000");
                boolean test = false;
                boolean dither = false;

                ArrayList<String> validFlags = new ArrayList<>();
                validFlags.add("NOLETTERBOX");
                validFlags.add("REMOVEALPHA");
                validFlags.add("TEST");
                validFlags.add("DITHER");
                Integer[] intOptions = new Integer[3];
                int intCount = 0;
                boolean scaled = false;
                for (int i = 0; i < args.length; i++) {
                    if (i == 0) {
                        url = args[i];
                    } else {
                        if (validFlags.contains(args[i].toUpperCase())) {
                            switch (args[i].toUpperCase()) {
                                case("NOLETTERBOX"): noLetterbox = true; break;
                                case("REMOVEALPHA"): removeAlpha = true; break;
                                case("TEST"): test = true; break;
                                case("DITHER"): dither = true; break;
                            }
                        } else {
                            if (args[i].equals("scale")) {
                                scaled = true;
                            }
                            if (args[i].chars().allMatch(Character::isDigit)) {
                                intOptions[intCount] = Integer.valueOf(args[i]);
                                intCount++;
                            }
                            if (args[i].startsWith("start=")) {
                                start = Integer.parseInt(args[i].substring(6));
                            }
                            if (args[i].startsWith("end=")) {
                                end = Integer.parseInt(args[i].substring(4));
                            }
                        }
                        if (removeAlpha && i == args.length - 1) {
                            try {
                                color = Color.decode(args[i]);
                            } catch(Exception e) {
                                try {
                                    color = new StyleSheet().stringToColor(args[i]);
                                } catch(Exception a) {
                                    color = Color.decode("#000000");
                                }
                            }
                        }
                    }
                }
                if (scaled) {
                    height = -1;
                    width = -1;
                    scale = intOptions[0];
                    if (intOptions[1] != null) {
                        transparencyThreshold = intOptions[1];
                    }
                } else {
                    if (intOptions[0] != null && intOptions[1] != null) {
                        width = intOptions[0];
                        height = intOptions[1];
                        if (intOptions[2] != null) {
                            transparencyThreshold = intOptions[2];
                        }
                    } else {
                        height = -1;
                        width = -1;
                        if (intOptions[0] != null) {
                            transparencyThreshold = intOptions[0];
                        }
                    }
                }
                if (scale == -1) {
                    scale = 1;
                }
                if (imagemap.downloadImageFromURL(url)) {
                    if ((width == -1 && height == -1) ? imagemap.scaleImage(scale, noLetterbox, removeAlpha, color, test) :
                                                        imagemap.scaleImage(width, height, noLetterbox, removeAlpha, color, test)) {
                        if (dither) {
                            imagemap.dither();
                        }
                        imagemap.prepareArray();
                        if (end == -1) {
                            end = imagemap.getImageSegmentArrayMaxValue();
                        }
                        imagemap.convertImagesToByteArray(start, end, transparencyThreshold);
                        sender.getServer().addScheduledTask(() -> {
                            int pos = imagemap.realStart;
                            EntityPlayer senderPlayer = null;
                            maps = imagemap.convertByteArraysToItemStacks();
                            boolean isPlayer = sender instanceof EntityPlayer;
                            boolean isPlayerMP = sender instanceof EntityPlayerMP;
                            boolean isSurvival = false;
                            if (isPlayer) {
                                senderPlayer = (EntityPlayer) sender;
                                isSurvival = !senderPlayer.isCreative();
                            }
                            for (ItemStack map : maps) {
                                if (isSurvival && LegacyImageMapsConfig.options.survivalUseUpEmptyMaps) {
                                    boolean hasRequiredItem = false;
                                    ItemStack emptyMap = null;
                                    for (ItemStack stack : ((EntityPlayer) sender).inventory.mainInventory) {
                                        if (!stack.isEmpty() && (LegacyImageMapsConfig.options.useCustomMapBaseType ?
                                                stack.getItem().getRegistryName().equals(ImageMapUtils.customMapBaseItemRegistry) && (
                                                        LegacyImageMapsConfig.options.customMapBaseItemDamage < 0 ||
                                                        stack.getItemDamage() == LegacyImageMapsConfig.options.customMapBaseItemDamage)
                                                : stack.getItem() == Items.MAP && stack.getMetadata() == 0)) {
                                            hasRequiredItem = true;
                                            emptyMap = stack;
                                            break;
                                        }
                                    }
                                    if (hasRequiredItem) {
                                        emptyMap.shrink(1);
                                    } else {
                                        imagemap.notifySender(I18n.translateToLocal("legacyimagemaps.insufficient_map_count") + " " + realStart,true);
                                        break;
                                    }
                                }
                                if (isPlayerMP) {
                                    if (((EntityPlayerMP) sender).connection == null) {
                                        break;
                                    }
                                }
                                if (isPlayer) {
                                    if (((EntityPlayer) sender).isDead || senderPlayer == null) {
                                        break;
                                    }
                                    if (!senderPlayer.inventory.addItemStackToInventory(map)) {
                                        senderPlayer.dropItem(map, false);
                                    }
                                } else {
                                    Block.spawnAsEntity(world, sender.getPosition(), map);
                                }
                                pos++;
                            }
                        });
                    }
                }
            });
        }
    }
}