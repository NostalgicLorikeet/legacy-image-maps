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

        @Config.Comment({"What dimension maps should be set in. Can be any number even if the dimension doesn't exist.", "Default: 0"})
        public int mapItemDimension = 0;

        @Config.Comment({"What coordinates maps should be set in. Putting them in accessible coordinates in a real dimension will cause them to be overwritten when accessed.", "Default: 0"})
        public int mapX = 32000000;
        public int mapZ = 32000000;

        @Config.Comment({"Maximum number of byte maps->maps as itemstacks that are logged in a world-local map.",
                "This is so that maps consisting of the same exact image point to the same map.dat file.",
                "Default: 128"})
        public int maxCachedByteMapsToItemStacks = 128;

        @Config.Comment({"Maximum number of color rgb->map colors that should be cached before cached colors are removed.", "You could theoretically set this as high as 16777216 but that is probably a really bad idea.", "Default: 8192"})
        public int maxCachedColorConversions = 8192;

        @Config.Comment({"Maximum number of image segments->color byte arrays (aka all the colors on a map) that should be cached before cached arrays are removed.", "512"})
        public int maxCachedByteArrays = 512;

        @Config.Comment({"What user agent should be used when making requests to image URLs.", "Default: \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/53\""})
        public String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/53";

        @Config.Comment({"Alpha threshold at which a pixel should be rendered as transparent on an image map.", "Default: 127"})
        public int transparencyThreshold = 127;

        @Config.Comment({"Whether to use standard Euclidean distance for RGB->map colors instead of Redmean.", "When changing this you should delete the \"colormap_cache.dat\" file in the LegacyImageMaps directory under Minecraft's root", "Default: false"})
        public boolean useEuclideanDistance = false;

        @Config.Comment({"Minimum permission level needed to use /imagemap.", "Default: 127"})
        public int minimumPermissionRequired = 0;

        @Config.Comment({"Allow players in survival to run this command if they have no empty maps in their inventory.", "Default: false"})
        public boolean allowSurvivalUseWithoutMaps = false;

        @Config.Comment({"Allow players in survival to run this command in general.", "Default: false"})
        public boolean allowSurvivalUse = false;

        //@Config.Comment({"Give a name with coordinates to produced maps. These will show up when hovered over on Item Frames so it may be distracting.", "Default: false"})
        //public boolean giveMapsCoordNames = false;
    }
}
