package nostalgic.legacyimagemaps;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import nostalgic.legacyimagemaps.command.CommandImageMap;
import nostalgic.legacyimagemaps.config.LegacyImageMapsConfig;
import nostalgic.legacyimagemaps.imagemaps.cache.CacheAll;
import nostalgic.legacyimagemaps.imagemaps.cache.CacheLinkedHashMap;
import nostalgic.legacyimagemaps.legacyimagemaps.Tags;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, acceptableRemoteVersions = "*")
public class LegacyImageMaps {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    /**
     * <a href="https://cleanroommc.com/wiki/forge-mod-development/event#overview">
     *     Take a look at how many FMLStateEvents you can listen to via the @Mod.EventHandler annotation here
     * </a>
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        CacheAll.byteMap = Collections.synchronizedMap(new CacheLinkedHashMap<>(LegacyImageMapsConfig.options.maxCachedByteArrays));
        CacheAll.colorMap = Collections.synchronizedMap(new CacheLinkedHashMap<>(LegacyImageMapsConfig.options.maxCachedColorConversions));
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandImageMap());
    }

    @Mod.EventHandler
    public void cacheSave(WorldEvent.Save event) {
        
    }
}
