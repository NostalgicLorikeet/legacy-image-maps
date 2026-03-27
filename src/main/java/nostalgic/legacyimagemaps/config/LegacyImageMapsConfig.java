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

        @Config.Comment({"Maximum dimensions that a downloaded image is allowed to be.", "Default: 4096"})
        public int maxImageDimensions = 4096;

        @Config.Comment({"What dimension maps should be set in. Can be any number even if the dimension doesn't exist. Change if the dimension DOES exist.", "Default: 0"})
        public int mapItemDimension = 0;

        @Config.Comment({"What coordinates maps should be set in. Putting them in accessible coordinates in a real dimension will cause them to be overwritten when accessed.", "Default: 0"})
        public int mapX = 32000000;
        public int mapZ = 32000000;

        @Config.Comment({"Give a name with coordinates to produced maps. These will show up when hovered over on Item Frames so it may be distracting.", "Default: false"})
        public boolean giveMapsCoordNames = false;
    }
}
