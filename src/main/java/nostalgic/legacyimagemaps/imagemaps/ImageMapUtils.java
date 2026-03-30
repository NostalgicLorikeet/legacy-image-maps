package nostalgic.legacyimagemaps.imagemaps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import nostalgic.legacyimagemaps.config.LegacyImageMapsConfig;

public class ImageMapUtils {
    private static final String namespace = LegacyImageMapsConfig.options.customMapBaseItemID.substring(0,LegacyImageMapsConfig.options.customMapBaseItemID.indexOf(':'));
    private static final String name = LegacyImageMapsConfig.options.customMapBaseItemID.substring(LegacyImageMapsConfig.options.customMapBaseItemID.indexOf(':')+1);
    public static final ResourceLocation customMapBaseItemRegistry = new ResourceLocation(namespace,name);
    public static long lastRequestTime = 0;

    public static void convertFilledMapToEmptyMap(EntityPlayer player) {
        if (!player.getEntityWorld().isRemote) {
            ItemStack hand = player.getHeldItemMainhand();
            if (!hand.isEmpty() && hand.getItem() == Items.FILLED_MAP) {
                player.setHeldItem(EnumHand.MAIN_HAND, Items.MAP.getDefaultInstance());
                player.getHeldItemMainhand().setCount(hand.getCount());
            }
        }
    }
}
