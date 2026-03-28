package nostalgic.legacyimagemaps.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

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

        }

        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
            return true;
        }
}
