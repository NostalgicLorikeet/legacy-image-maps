package nostalgic.legacyimagemaps.imagemaps;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.awt.*;
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
                String url = args[0];
                int scale = 1;
                int width = -1;
                int height = -1;
                int start;
                int end;
                int transparencyThreshold;
                Boolean noLetterbox = false;
                Boolean test = false;
                Boolean removeAlpha = false;
                Color color = Color.decode("#000000");

                if (imagemap.downloadImageFromURL(url)) {
                    if ((width == -1 && height == -1) ?
                            imagemap.scaleImage(scale, noLetterbox, removeAlpha, color, test) :
                            imagemap.scaleImage(width, height, noLetterbox, removeAlpha, color, test)
                        ) {
                        imagemap.prepareArray();
                        imagemap.convertImagesToByteArray();

                        sender.getEntityWorld().getMinecraftServer().addScheduledTask(() -> {
                            maps = imagemap.convertByteArraysToItemStacks();
                            for (ItemStack map : maps) {
                                Block.spawnAsEntity(world,sender.getPosition(),map);
                            }
                        });
                    }
                }
            });
        }
    }
}