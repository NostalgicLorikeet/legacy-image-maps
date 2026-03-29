package nostalgic.legacyimagemaps;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nostalgic.legacyimagemaps.command.CommandImageMap;
import nostalgic.legacyimagemaps.imagemaps.cache.CacheAll;
import nostalgic.legacyimagemaps.legacyimagemaps.Tags;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, acceptableRemoteVersions = "*")
public class LegacyImageMaps {
    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    public static File legacyImageMapsDirectory;
    public static File legacyImageMapsByteMapCacheFile;
    public static File legacyImageMapsColorMapCacheFile;

    public static File legacyImageMapsByteMapsToItemStackFile;

    /**
     * <a href="https://cleanroommc.com/wiki/forge-mod-development/event#overview">
     *     Take a look at how many FMLStateEvents you can listen to via the @Mod.EventHandler annotation here
     * </a>
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        legacyImageMapsDirectory = new File(event.getModConfigurationDirectory().getParentFile(),"LegacyImageMaps");
        legacyImageMapsByteMapCacheFile = new File(legacyImageMapsDirectory,"bytemap_cache.dat");
        legacyImageMapsColorMapCacheFile = new File(legacyImageMapsDirectory,"colormap_cache.dat");

        if (!legacyImageMapsDirectory.exists()) {
            legacyImageMapsDirectory.mkdir();
        }

        if (legacyImageMapsByteMapCacheFile.exists()) {
            try {
                NBTTagCompound byteMapCompound = CompressedStreamTools.readCompressed(Files.newInputStream(legacyImageMapsByteMapCacheFile.toPath()));
                for (String key : byteMapCompound.getKeySet()) {
                    CacheAll.byteMap.put(key, byteMapCompound.getByteArray(key));
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }

        if (legacyImageMapsColorMapCacheFile.exists()) {
            try {
                NBTTagCompound colorMapCompound = CompressedStreamTools.readCompressed(Files.newInputStream(legacyImageMapsColorMapCacheFile.toPath()));
                int[] keys = colorMapCompound.getIntArray("keys");
                int[] values = colorMapCompound.getIntArray("values");

                for (int i = 0; i < keys.length; i++) {
                    CacheAll.colorMap.put(keys[i],values[i]);
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandImageMap());
    }

    @SubscribeEvent
    public void cacheSave(WorldEvent.Save event) {
        if (event.getWorld().provider.getDimension() != 0) return;

        NBTTagCompound byteMapNBT = new NBTTagCompound();
        NBTTagCompound colorMapNBT = new NBTTagCompound();
        NBTTagCompound byteMapToItemStackMapNBT = new NBTTagCompound();

        Map<String, byte[]> byteMapOld;
        Map<Integer, Integer> colorMapOld;
        Map<Long, ItemStack> byteMapToItemStackMapOld;

        synchronized (CacheAll.byteMap) {
            byteMapOld = new HashMap<>(CacheAll.byteMap);
        }

        synchronized (CacheAll.colorMap) {
            colorMapOld = new HashMap<>(CacheAll.colorMap);
        }

        synchronized (CacheAll.byteMapToItemStackMap) {
            byteMapToItemStackMapOld = new HashMap<>(CacheAll.byteMapToItemStackMap);
        }

        int[] colorMapKeys = new int[colorMapOld.size()];
        int[] colorMapValues = new int[colorMapOld.size()];

        for (Map.Entry<String, byte[]> entry : byteMapOld.entrySet()) {
            byteMapNBT.setByteArray(entry.getKey(), entry.getValue());
        }

        int i = 0;
        for (Map.Entry<Integer, Integer> entry : colorMapOld.entrySet()) {
            colorMapKeys[i] = entry.getKey();
            colorMapValues[i] = entry.getValue();
            i++;
        }

        colorMapNBT.setIntArray("keys",colorMapKeys);
        colorMapNBT.setIntArray("values",colorMapValues);

        legacyImageMapsByteMapsToItemStackFile = new File(DimensionManager.getCurrentSaveRootDirectory(),"data/itemstack_map.dat");

        i = 0;
        for (Map.Entry<Long, ItemStack> entry : byteMapToItemStackMapOld.entrySet()) {
            byteMapToItemStackMapNBT.setTag(String.valueOf(entry.getKey()), entry.getValue().serializeNBT());
            i++;
        }

        try {
            try (OutputStream output = Files.newOutputStream(legacyImageMapsByteMapCacheFile.toPath())) {
                CompressedStreamTools.writeCompressed(byteMapNBT, output);
            }
            try (OutputStream output = Files.newOutputStream(legacyImageMapsColorMapCacheFile.toPath())) {
                CompressedStreamTools.writeCompressed(colorMapNBT, output);
            }
            try (OutputStream output = Files.newOutputStream(legacyImageMapsByteMapsToItemStackFile.toPath())) {
                CompressedStreamTools.writeCompressed(byteMapToItemStackMapNBT, output);
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    @SubscribeEvent
    public void localCacheLoad (WorldEvent.Load event) {
        if (event.getWorld().provider.getDimension() != 0) return;

        legacyImageMapsByteMapsToItemStackFile = new File(DimensionManager.getCurrentSaveRootDirectory(),"data/itemstack_map.dat");
        CacheAll.byteMapToItemStackMap.clear();

        if (legacyImageMapsByteMapsToItemStackFile.exists()) {
            try {
                NBTTagCompound byteMapCompound = CompressedStreamTools.readCompressed(Files.newInputStream(legacyImageMapsByteMapsToItemStackFile.toPath()));
                for (String key : byteMapCompound.getKeySet()) {
                    CacheAll.byteMapToItemStackMap.put(Long.valueOf(key),new ItemStack(byteMapCompound.getCompoundTag(key)));
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
    }
}
