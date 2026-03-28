package nostalgic.legacyimagemaps.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import nostalgic.legacyimagemaps.imagemaps.ImageMapRequest;

public class CommandImageMap extends CommandBase {
    @Override
        public String getName() {
            return "imagemap";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "/imagemap";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
            new ImageMapRequest(sender, args[0], 1, true, true, "#000000", false);
        }

        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
            return true;
        }
}
