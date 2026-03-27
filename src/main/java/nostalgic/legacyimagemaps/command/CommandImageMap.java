package nostalgic.legacyimagemaps.command;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import nostalgic.legacyimagemaps.imagemaps.ImageMap;

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
            World world = sender.getEntityWorld();

            String path = (args.length > 0) ? args[0] : "urlnotexist";

            if (!world.isRemote) {
                ImageMap imagemap = new ImageMap(sender);
                if (imagemap.downloadImageFromURL(path)) {
                    imagemap.scaleImage(4,true,false, "#000000",false);
                    imagemap.prepareArray();
                    imagemap.convertImagesToMaps();
                    for (int i = 0; i < imagemap.maps.length; i++) {
                        Block.spawnAsEntity(world,sender.getPosition(),imagemap.maps[i]);
                    }
                }
            }
        }

        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
            return true;
        }
}
