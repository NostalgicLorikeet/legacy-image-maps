package nostalgic.legacyimagemaps.imagemaps.cache;

import nostalgic.legacyimagemaps.config.LegacyImageMapsConfig;

import java.util.Collections;
import java.util.Map;

public class CacheAll {
    public static final Map<String, byte[]> byteMap = Collections.synchronizedMap(new CacheLinkedHashMap<>(LegacyImageMapsConfig.options.maxCachedByteArrays));
    public static final Map<Integer, Integer> colorMap = Collections.synchronizedMap(new CacheLinkedHashMap<>(LegacyImageMapsConfig.options.maxCachedColorConversions));
    public static final Object infiniteTortureDevice = new Object();
}
