package nostalgic.legacyimagemaps.imagemaps;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageMapRequest {
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(2);
    World world;

    ICommandSender requester;
    private ImageMap imagemap;
    ItemStack[] maps;

    String imageHash;
    int width;
    int height;
    boolean letterbox;
    boolean preserveTransparency;
    Color color;

    String url;
    boolean test;

    public ImageMapRequest(ICommandSender requester, String url, int scale,
                           boolean letterbox, boolean preserveTransparency, String colorString, boolean test) {
        world = requester.getEntityWorld();
        if (!world.isRemote) {
            THREAD_POOL.execute(() -> {
                this.requester = requester;
                imagemap = new ImageMap(requester);

                this.letterbox = letterbox;
                this.preserveTransparency = preserveTransparency;
                this.color = Color.decode(colorString);

                this.test = test;
                this.url = url;

                if (imagemap.downloadImageFromURL(url)) {
                    int[] scales = imagemap.getScaledWidthAndHeight(scale);

                    this.width = scales[0];
                    this.height = scales[1];
                    this.imageHash = imagemap.getImageHash();

                    run();
                }
            });
        }
    }

    public ImageMapRequest(ICommandSender requester, String url, int width, int height,
                           boolean letterbox, boolean preserveTransparency, String colorString, boolean test) {
        world = requester.getEntityWorld();
        if (!world.isRemote) {
            THREAD_POOL.execute(() -> {
                this.requester = requester;
                imagemap = new ImageMap(requester);

                this.width = width;
                this.height = height;
                this.letterbox = letterbox;
                this.preserveTransparency = preserveTransparency;
                this.color = Color.decode(colorString);

                this.test = test;
                this.url = url;

                if (imagemap.downloadImageFromURL(url)) {
                    this.imageHash = imagemap.getImageHash();

                    run();
                }
            });
        }
    }

    public void run() {
        if (imagemap.scaleImage(width, height, letterbox, preserveTransparency, color, test)) {
            imagemap.prepareArray();
            imagemap.convertImagesToByteArray();
        }

        IThreadListener main;

        if (FMLCommonHandler.instance().getSide().isClient()) {
            main = Minecraft.getMinecraft();
        } else {
            main = FMLCommonHandler.instance().getMinecraftServerInstance();
        }

        main.addScheduledTask(() -> {
            maps = imagemap.convertByteArraysToMapItems();
            for (ItemStack map : maps) {
                Block.spawnAsEntity(world,requester.getPosition(),map);
            }
        });
    }

    //public int hashOptions() {
    //    String options = imageHash + "," + width + "," + height + "," + letterbox + "," + preserveTransparency + "," + color;
    //    return options.hashCode();
    //}
}
