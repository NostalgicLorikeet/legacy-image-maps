package nostalgic.legacyimagemaps.imagemaps;

import ditherer.ColorPalette;
import net.minecraft.block.material.MapColor;

import java.awt.*;
import java.util.ArrayList;

public final class PaletteHolder {
    public static final ColorPalette ditheringPalette;

    static {
        ArrayList<Color> colors = new ArrayList<>();

        for (MapColor colorCompare : MapColor.COLORS) {
            if (colorCompare == null) continue;
            for (int i = 0; i < 4; i++) {
                Color color = new Color(colorCompare.getMapColor(i));
                colors.add(color);
                i++;
            }
        }

        ditheringPalette = new ColorPalette(colors.toArray(new Color[0]));
    }
}
