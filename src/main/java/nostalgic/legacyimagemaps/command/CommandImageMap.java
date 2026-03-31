package nostalgic.legacyimagemaps.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import nostalgic.legacyimagemaps.config.LegacyImageMapsConfig;
import nostalgic.legacyimagemaps.imagemaps.ImageMapRequest;
import nostalgic.legacyimagemaps.imagemaps.ImageMapUtils;
import nostalgic.legacyimagemaps.imagemaps.cache.CacheAll;

public class CommandImageMap extends CommandBase {
        public static final String helpString = "/imagemap <url> scale <scale> start=<start> end=<end> <transparencythreshold> <flags> <color> OR /imagemap <width> <height> start=<start> end=<end> <transparencythreshold> <flags> <color>";
        public static final TextComponentString help = new TextComponentString(helpString);
        public static final TextComponentString clearUsage = (TextComponentString) new TextComponentString("Options: bytemaps, colormaps, itemstacks, downloads, inmemory").setStyle(new Style().setColor(TextFormatting.RED));

        @Override
        public String getName() {
            return "imagemap";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return helpString;
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
            try {
                if (args[0].equals("help")) {
                    sender.sendMessage(help);
                } else if (args[0].equals("unfill")) {
                    if (LegacyImageMapsConfig.options.clearMapsUsingCommand) {
                        if (sender instanceof EntityPlayer) {
                            ImageMapUtils.convertFilledMapToEmptyMap((EntityPlayer) sender);
                        }
                    }
                } else if (args[0].equals("clear")) {
                    if (args.length == 1) {
                        sender.sendMessage(clearUsage);
                    } else {
                        switch(args[1]) {
                            case("bytemaps"):
                                CacheAll.byteMap.clear();
                                sender.sendMessage(new TextComponentString("Byte array cache cleared"));
                                break;
                            case("colormaps"):
                                CacheAll.colorMap.clear();
                                sender.sendMessage(new TextComponentString("Color map cache cleared"));
                                break;
                            case("itemstacks"):
                                CacheAll.byteMapToItemStackMap.clear();
                                sender.sendMessage(new TextComponentString("ItemStack cache cleared"));
                                break;
                            case("downloads"):
                                CacheAll.downloadedImageCache.clear();
                                sender.sendMessage(new TextComponentString("Download cache cleared"));
                                break;
                            case("inmemory"):
                                CacheAll.runtimeImageCache.clear();
                                sender.sendMessage(new TextComponentString("In-memory download cache cleared"));
                                break;
                            default:
                                sender.sendMessage(clearUsage);
                        }
                    }
                } else {
                    try {
                        new ImageMapRequest(sender, args);
                    } catch (Exception e) {
                        sender.sendMessage(help.setStyle(new Style().setColor(TextFormatting.RED)));
                        System.out.println(e);
                    }
                }
            } catch (Exception e) {
                sender.sendMessage(help.setStyle(new Style().setColor(TextFormatting.RED)));
            }
        }

        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
            return sender.canUseCommand(this.getRequiredPermissionLevel(), this.getName());
        }

        @Override
        public int getRequiredPermissionLevel() {
            return LegacyImageMapsConfig.options.minimumPermissionLevelRequired;
        }
}
