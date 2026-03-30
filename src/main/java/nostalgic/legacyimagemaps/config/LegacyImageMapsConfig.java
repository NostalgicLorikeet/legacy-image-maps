package nostalgic.legacyimagemaps.config;

import net.minecraftforge.common.config.Config;
import nostalgic.legacyimagemaps.legacyimagemaps.Tags;

@Config(modid = Tags.MOD_ID, name = Tags.MOD_ID)
public final class LegacyImageMapsConfig
{
    @Config.Comment("Config options for Legacy Image Maps")
    @Config.Name("Legacy Image Maps")
    @Config.RequiresMcRestart
    public static ImagesMapsConfig options = new ImagesMapsConfig();

    public static class ImagesMapsConfig {
        @Config.Comment({"Maximum size in bytes that a downloaded image is allowed to be.", "Default: 2097152"})
        public int maxImageSize =  2097152;

        @Config.Comment({"Maximum dimension that a downloaded image is allowed to be.", "Default: 4096"})
        public int maxImageDimensions = 4096;

        @Config.Comment({"Maximum total number of maps that a scaled image can be equal to.", "Default: 64"})
        public int maxMapCount = 64;

        @Config.Comment({"Time in milliseconds required between map requests.", "Default: 0"})
        public int requiredRequestInterval = 0;

        @Config.Comment({"What dimension maps should be set in. Can be any number even if the dimension doesn't exist.", "Default: 0"})
        public int mapItemDimension = 0;

        @Config.Comment({"What coordinates maps should be set in. Putting them in accessible coordinates in a real dimension will cause them to be overwritten when accessed.", "Default: 0"})
        public int mapX = 32000000;
        public int mapZ = 32000000;

        @Config.Comment({"Maximum number of byte maps->maps as itemstacks that are logged in a world-local list.",
                "This is so that maps consisting of the same exact image point to the same map_*.dat file.",
                "Default: 128"})
        public int maxCachedByteMapsToItemStacks = 128;

        @Config.Comment({"Maximum number of color rgb->map colors that should be cached before cached colors are removed.", "You could theoretically set this as high as 16777216 but that is probably a really bad idea.", "Default: 8192"})
        public int maxCachedColorConversions = 8192;

        @Config.Comment({"Maximum number of image segments->color byte arrays (aka all the colors on a map) that should be cached before cached arrays are removed.", "512"})
        public int maxCachedByteArrays = 512;

        @Config.Comment({"What user agent should be used when making requests to image URLs.", "Default: \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/53\""})
        public String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/53";

        @Config.Comment({"Alpha threshold at which a pixel should be rendered as transparent on an image map.", "This can be altered on a per-image basis on player request", "Default: 127"})
        public int transparencyThreshold = 127;

        @Config.Comment({"Whether to use standard Euclidean distance for RGB->map colors instead of Redmean.", "When changing this you should delete the \"colormap_cache.dat\" file in the LegacyImageMaps directory under Minecraft's root", "Default: false"})
        public boolean useEuclideanDistance = false;

        @Config.Comment({"Whether to allow dithering as an option for imagemaps.", "Default: true"})
        public boolean allowDithering = true;

        @Config.Comment({"Minimum server permission level required to use the command.", "Default: 2"})
        public int minimumPermissionLevelRequired = 2;

        @Config.Comment({"Whether to use up empty maps from the player's inventory when run in survival mode.", "Default: true"})
        public boolean survivalUseUpEmptyMaps = true;

        @Config.Comment({"Allow players to clear held maps using \"/imagemap unfill\".", "Default: false"})
        public boolean clearMapsUsingCommand = false;

        @Config.Comment({"Use a different item as a base for image maps. Set damage value to -1 for any.", "Default: false"})
        public boolean useCustomMapBaseType = false;
        public String customMapBaseItemID = "minecraft:flint";
        public int customMapBaseItemDamage = -1;

        @Config.Comment({"Save the global caches (the byte map and color map) to disk with the world. This may cause performance issues on lower-end machines.", "Default: true"})
        public boolean saveCacheToDisk = true;

        @Config.Comment({"Save the byte map->item stack cache to disk with the world. This may cause performance issues on lower-end machines.", "Default: true"})
        public boolean saveItemStackCacheToDisk = true;

        //@Config.Comment({"Give a name with coordinates to produced maps. These will show up when hovered over on Item Frames so it may be distracting.", "Default: false"})
        //public boolean giveMapsCoordNames = false;
    }
}
