package nostalgic.legacyimagemaps.imagemaps.cache;

import net.minecraft.item.ItemStack;
import nostalgic.legacyimagemaps.config.LegacyImageMapsConfig;

import java.util.Collections;
import java.util.Map;

public class CacheAll {
    public static final Map<String, byte[]> byteMap = Collections.synchronizedMap(new CacheLinkedHashMap<>(LegacyImageMapsConfig.options.maxCachedByteArrays));
    public static final Map<Integer, Integer> colorMap = Collections.synchronizedMap(new CacheLinkedHashMap<>(LegacyImageMapsConfig.options.maxCachedColorConversions));
    public static final Map<Long, ItemStack> byteMapToItemStackMap = Collections.synchronizedMap(new CacheLinkedHashMap<>(LegacyImageMapsConfig.options.maxCachedByteMapsToItemStacks));
}
