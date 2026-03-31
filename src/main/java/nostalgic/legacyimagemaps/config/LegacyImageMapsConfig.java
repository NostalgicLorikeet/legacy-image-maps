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

        @Config.Comment({"Maximum total number of maps that a scaled image can be equal to per request.", "Default: 64"})
        public int maxMapCount = 64;

        @Config.Comment({"Time in milliseconds required between map requests.", "Default: 0"})
        public int requiredRequestInterval = 0;

        @Config.Comment({"What dimension maps should be set in. Can be any number even if the dimension doesn't exist.", "Default: 0"})
        public int mapItemDimension = 0;

        @Config.Comment({"What coordinates maps should be set in. Putting them in accessible coordinates in a real dimension will cause them to be overwritten when accessed.", "Default: 0"})
        public int mapX = 32000000;
        public int mapZ = 32000000;

        @Config.Comment({"Maximum number of new map ItemStacks a player is allowed to make. If a player requests a map that is identical to an already created one it isn't counted against them.",
                "It is recommended to use this on a server that may be prone to lagging due to excessive usage of imagemaps",
                "Admins bypass this limit",
                "It is recommended to set maxCachedByteMapsToItemStacks higher to prevent players from going over the limit too much if you expect many images to have many identical maps made.",
                "Default: false"})
        public boolean useMaxPerPlayerMapCount = false;
        public int maxPerPlayerMapCount = 256;

        @Config.Comment({"Whether all maps created by a player count against a player's limit, not just new images.", "Default: false"})
        public boolean maxPerPlayerMapCountCountAllMaps = false;

        @Config.Comment({"Maximum number of byte maps->maps as ItemStacks that are logged in a world-local list.",
                "This is so that maps consisting of the same exact image point to the same map_*.dat file.",
                "Default: 1024"})
        public int maxCachedByteMapsToItemStacks = 1024;

        @Config.Comment({"Maximum number of color rgb->map colors that should be cached before cached colors are removed.", "You could theoretically set this as high as 16777216 but that is probably a really bad idea.", "Default: 8192"})
        public int maxCachedColorConversions = 8192;

        @Config.Comment({"Maximum number of image segments->color byte arrays (aka all the colors on a map) that should be cached before cached arrays are removed.", "512"})
        public int maxCachedByteArrays = 512;

        @Config.Comment({"What user agent should be used when making requests to image URLs.", "Default: \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/53\""})
        public String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/53";

        @Config.Comment({"Alpha threshold at which a pixel should be rendered as transparent on an image map.", "This can be altered on a per-image basis on player request", "Default: 127"})
        public int transparencyThreshold = 127;

        @Config.Comment({"Whether to use standard Euclidean distance for RGB->map colors instead of Redmean.", "When changing this you should run \"/imagemap clear colormaps\" to clear the color byte cache", "Default: false"})
        public boolean useEuclideanDistance = false;

        @Config.Comment({"Whether to allow dithering as an option for imagemaps.", "Default: true"})
        public boolean allowDithering = true;

        @Config.Comment({"Whether to allow players to produce multiple copies of the same map or set of maps at once.", "Default: true"})
        public boolean allowMultipleCopies = true;

        @Config.Comment({"Adds a \"hasImageMap\" NBT boolean equal to 1 (true) to the data of imagemap items. May be helpful for crafting with a mod like CraftTweaker.", "Default: true"})
        public boolean addHasImageMapBoolToNBT = true;

        @Config.Comment({"Minimum server permission level required to use the command.", "Default: 2"})
        public int minimumPermissionLevelRequired = 2;

        @Config.Comment({"Whether to use up empty maps from the player's inventory when run in survival mode.", "Default: true"})
        public boolean survivalUseUpEmptyMaps = true;

        @Config.Comment({"Allow players to clear held maps using \"/imagemap unfill\".", "Default: false"})
        public boolean clearMapsUsingCommand = false;

        @Config.Comment({"Whether to always drop maps at the player's location or deposit them in their inventory first.",
                "Setting to false will cause extra maps to be picked up and deleted in creative with a full inventory, but should be fine in survival.",
                "Default: true"})
        public boolean dropMaps = true;

        @Config.Comment({"Use a different item as a base for image maps. Set damage value to -1 for any.", "Default: false"})
        public boolean useCustomMapBaseType = false;
        public String customMapBaseItemID = "minecraft:flint";
        public int customMapBaseItemDamage = -1;

        @Config.Comment({"Save the global caches (the byte map and color map) to disk with the world. Disable if this causes performance issues.",
                "Setting to false will still cache these values per-session",
                "Default: true"})
        public boolean saveCacheToDisk = true;

        @Config.Comment({"Cache downloaded images. This option is toggled separately from the above.", "Default: true"})
        public boolean cacheDownloadedImages = true;

        @Config.Comment({"Max number of URLs that should be logged in the map of cached images.",
                "This does not equate to the number of images that are cached because an image may be sourced from multiple URLs",
                "for example because of how Discord file tokens work.",
                "Images are stored based on their hash, so if a URL downloads an image that has the same hash as an already-downloaded one, it does not save two identical images",
                "and instead logs both urls as having the same resultant hash.",
                "Images are hashed according to how they existed in memory while being processed, so if two images are visually identical but have different metadata when downloaded,",
                "then they will probably be considered identical by the mod (assuming they are converted into an identical BufferedImage).",
                "Default: true"})
        public int maxDownloadedImages = 128;

        @Config.Comment({"Number of downloaded images that should be cached directly in memory during runtime.",
                "This directly stores the BufferedImage object in memory, so it might be marginally faster than file caching by the OS.",
                "Default: 4"})
        public int maxCachedImagesDuringRuntime = 4;

        @Config.Comment({"Save the byte map->item stack cache to disk with the world.", "Default: true"})
        public boolean saveItemStackCacheToDisk = true;

        @Config.Comment({"Number of thread available for image processing.", "Default: 2"})
        public int threadCount = 2;

        //@Config.Comment({"Give a name with coordinates to produced maps. These will show up when hovered over on Item Frames so it may be distracting.", "Default: false"})
        //public boolean giveMapsCoordNames = false;
    }
}
