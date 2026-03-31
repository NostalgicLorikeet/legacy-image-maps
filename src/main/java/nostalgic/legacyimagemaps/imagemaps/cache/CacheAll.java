package nostalgic.legacyimagemaps.imagemaps.cache;

import net.minecraft.item.ItemStack;
import nostalgic.legacyimagemaps.config.LegacyImageMapsConfig;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Map;

public class CacheAll {
    public static final Map<String, byte[]> byteMap = Collections.synchronizedMap(new CacheLinkedHashMap<>(LegacyImageMapsConfig.options.maxCachedByteArrays));
    public static final Map<Integer, Integer> colorMap = Collections.synchronizedMap(new CacheLinkedHashMap<>(LegacyImageMapsConfig.options.maxCachedColorConversions));
    public static final Map<Long, ItemStack> byteMapToItemStackMap = Collections.synchronizedMap(new CacheLinkedHashMap<>(LegacyImageMapsConfig.options.maxCachedByteMapsToItemStacks));
    public static final Map<String, String> downloadedImageCache = Collections.synchronizedMap(new CacheLinkedHashMap<>(LegacyImageMapsConfig.options.maxDownloadedImages));
    public static final Map<String, BufferedImage> runtimeImageCache = Collections.synchronizedMap(new CacheLinkedHashMap<>(LegacyImageMapsConfig.options.maxCachedImagesDuringRuntime));
}
