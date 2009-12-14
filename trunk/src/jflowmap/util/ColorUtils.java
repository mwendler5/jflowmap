package jflowmap.util;

import java.awt.Color;

import prefuse.util.ColorLib;

/**
 * @author Ilya Boyandin
 */
public class ColorUtils {

    public static final Color[] createCategoryColors(int numberOfColors) {
        Color[] colors = new Color[numberOfColors];
        int[] palette = ColorLib.getCategoryPalette(numberOfColors, .7f, .4f, 1.f, 1.f);
        for (int i = 0; i < numberOfColors; i++) {
            colors[i] = new Color(palette[i]);
        }
        return colors;
    }

}
