package nostalgic.legacyimagemaps.imagemaps;

import net.minecraft.block.material.MapColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import nostalgic.legacyimagemaps.config.LegacyImageMapsConfig;

import java.util.concurrent.ConcurrentHashMap;

public class ImageMapUtils {
    private static final String namespace = LegacyImageMapsConfig.options.customMapBaseItemID.substring(0,LegacyImageMapsConfig.options.customMapBaseItemID.indexOf(':'));
    private static final String name = LegacyImageMapsConfig.options.customMapBaseItemID.substring(LegacyImageMapsConfig.options.customMapBaseItemID.indexOf(':')+1);
    public static final ResourceLocation customMapBaseItemRegistry = new ResourceLocation(namespace,name);

    public static long lastRequestTime = 0;

    public static final ConcurrentHashMap<String, Integer> playerNewItemStackCount = new ConcurrentHashMap<>();

    public static void convertFilledMapToEmptyMap(EntityPlayer player) {
        if (!player.getEntityWorld().isRemote) {
            ItemStack hand = player.getHeldItemMainhand();
            if (!hand.isEmpty() && hand.getItem() == Items.FILLED_MAP) {
                player.setHeldItem(EnumHand.MAIN_HAND, Items.MAP.getDefaultInstance());
                player.getHeldItemMainhand().setCount(hand.getCount());
            }
        }
    }

    //getMapColor in MapColor is client-only by default, so its copied over here
    public static int getMapColor(MapColor color, int index) {
        int i = 220;

        if (index == 3)
        {
            i = 135;
        }

        if (index == 2)
        {
            i = 255;
        }

        if (index == 1)
        {
            i = 220;
        }

        if (index == 0)
        {
            i = 180;
        }

        int j = (color.colorValue >> 16 & 255) * i / 255;
        int k = (color.colorValue >> 8 & 255) * i / 255;
        int l = (color.colorValue & 255) * i / 255;
        return -16777216 | j << 16 | k << 8 | l;
    }
}
