package jflowmap.util;

import java.awt.Color;

/**
 * @author Ilya Boyandin
 */
public class ColorUtils {

    public static final Color[] createCategoryColors(int numberOfColors, double brightness, int alpha) {
        Color[] colors = new Color[numberOfColors];
        for (int i = 0; i < numberOfColors; i++) {
            colors[i] = new Color(ColorUtils.convertHSVtoRGB(i * 1.0 / numberOfColors, brightness, 1.0, alpha), true);
        }
        return colors;
    }

    /**
     * This function is shamelessly borrowed from ColorConversions in apache sanselan.
     */
    public static int convertHSVtoRGB(double H, double S, double V, int alpha) {
        double R, G, B;

        if (S == 0) { // HSV values = 0 ÷ 1
            R = V * 255;
            G = V * 255;
            B = V * 255;
        } else {
            double h = H * 6;
            if (h == 6)
                h = 0; // H must be < 1
            double i = Math.floor(h);
            double v1 = V * (1 - S);
            double v2 = V * (1 - S * (h - i));
            double v3 = V * (1 - S * (1 - (h - i)));

            double r, g, b;

            if (i == 0) {
                r = V;
                g = v3;
                b = v1;
            } else if (i == 1) {
                r = v2;
                g = V;
                b = v1;
            } else if (i == 2) {
                r = v1;
                g = V;
                b = v3;
            } else if (i == 3) {
                r = v1;
                g = v2;
                b = V;
            } else if (i == 4) {
                r = v3;
                g = v1;
                b = V;
            } else {
                r = V;
                g = v1;
                b = v2;
            }

            R = r * 255; // RGB results = 0 ÷ 255
            G = g * 255;
            B = b * 255;
        }

        return convertRGBtoRGB(R, G, B, alpha);
    }

    private static final int convertRGBtoRGB(double R, double G, double B, int alpha) {
        int red = (int) Math.round(R);
        int green = (int) Math.round(G);
        int blue = (int) Math.round(B);

        red = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));
        blue = Math.min(255, Math.max(0, blue));

        int rgb = (alpha << 24) | (red << 16) | (green << 8) | (blue << 0);

        return rgb;
    }

}
