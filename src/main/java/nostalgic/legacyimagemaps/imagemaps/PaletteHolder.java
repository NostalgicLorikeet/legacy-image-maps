package nostalgic.legacyimagemaps.imagemaps;

import ditherer.ColorPalette;
import net.minecraft.block.material.MapColor;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public final class PaletteHolder {
    public static final ColorPalette ditheringPalette;
    public static final HashMap<Integer, Integer> ditherIntMap = new HashMap<>();

    static {
        ArrayList<Color> colors = new ArrayList<>();

        for (MapColor colorCompare : MapColor.COLORS) {
            if (colorCompare == null || colorCompare == MapColor.AIR) continue;
            for (int i = 0; i < 4; i++) {
                Color color = new Color(ImageMapUtils.getMapColor(colorCompare,i));
                colors.add(color);
                ditherIntMap.put(color.getRGB() & 0x00FFFFFF,colorCompare.colorIndex * 4 + i);
            }
        }

        ditheringPalette = new ColorPalette(colors.toArray(new Color[0]));
    }
}
