package nostalgic.legacyimagemaps.imagemaps.cache;

import java.util.LinkedHashMap;
import java.util.Map;

public class CacheLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
    private final int max;

    public CacheLinkedHashMap(int max) {
        super(max, 0.75f, true);
        this.max = max;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > max;
    }
}
