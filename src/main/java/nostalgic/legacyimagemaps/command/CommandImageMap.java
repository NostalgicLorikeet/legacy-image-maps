package nostalgic.legacyimagemaps.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import nostalgic.legacyimagemaps.config.LegacyImageMapsConfig;
import nostalgic.legacyimagemaps.imagemaps.ImageMapRequest;

public class CommandImageMap extends CommandBase {
        public static final String helpString = "/imagemap <url> scale <scale> start=<start> end=<end> <transparencythreshold> <flags> <color> OR /imagemap <width> <height> start=<start> end=<end> <transparencythreshold> <flags> <color>";
        public static final TextComponentString help = new TextComponentString(helpString);

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
            if (args[0].equals("help")) {
                sender.sendMessage(help);
            } else {
                try {
                    new ImageMapRequest(sender, args);
                } catch (Exception e) {
                    sender.sendMessage(help.setStyle(new Style().setColor(TextFormatting.RED)));
                    System.out.println(e);
                }
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
