package net.rene.custommapimages.command;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static net.rene.custommapimages.CustomMapImages.LOGGER;
import static net.rene.custommapimages.command.ColorHelper.colorDistance;


public class ImageBlender {
    private static final HashMap<Color, Short> colorPairs = (HashMap<Color, Short>) generateColorPairsMap();
    static final int ALLOWED_COLOR_DISTANCE = 8000;

    private ImageBlender() {}

    public static Color blendAll(Color... c) {
        if (c == null || c.length == 0) {
            return null;
        }
        float ratio = 1f / (c.length);

        int a = 0;
        int r = 0;
        int g = 0;
        int b = 0;

        for (Color color : c) {
            int rgb = color.getRGB();
            int a1 = (rgb >> 24 & 0xff);
            int r1 = ((rgb & 0xff0000) >> 16);
            int g1 = ((rgb & 0xff00) >> 8);
            int b1 = (rgb & 0xff);
            a += (a1 * ratio);
            r += (r1 * ratio);
            g += (g1 * ratio);
            b += (b1 * ratio);
        }

        return new Color(a << 24 | r << 16 | g << 8 | b);
    }

    public static byte[][] blend(BufferedImage image) {
        byte[][] raster = new byte[image.getWidth() * 2][image.getHeight() * 2];
        for (int i = 0; i < image.getWidth(); ++i) {
            for (int j = 0; j < image.getHeight(); ++j) {
                Color color = new Color(image.getRGB(i, j));
                Color bestColorInSet = getBestColor(color);
                short colors = colorPairs.get(bestColorInSet);
                byte[] c = new byte[2];
                c[0] = (byte) (colors & 0xff);
                c[1] = (byte) ((colors & 0xff00) >> 8);

                raster[i * 2][j * 2]            = c[0];
                raster[i * 2][j * 2 + 1]        = c[1];
                raster[i * 2 + 1][j * 2]        = c[1];
                raster[i * 2 + 1][j * 2 + 1]    = c[0];
            }
        }
        return raster;
    }
    public static Color getBestColor(Color c1) {
        Color bestColor = new Color(88, 124, 39);
        double minDistance = Double.MAX_VALUE;
        for (Map.Entry<Color, Short> c :colorPairs.entrySet()) {
            double colorDistance = colorDistance(c1, c.getKey());
            if (colorDistance < minDistance) {
                minDistance = colorDistance;
                bestColor = c.getKey();
            }
        }
        return bestColor;
    }

    public static Map<Color, Short> generateColorPairsMap() {
        HashMap<Color, Short> map = HashMap.newHashMap(6000);
        for (int i = 0; i < CustomMapColors.colors.length; ++i) {
            for (int j = 0; j < CustomMapColors.colors.length; ++j) {
                if (colorDistance(CustomMapColors.colors[i], CustomMapColors.colors[j]) > ALLOWED_COLOR_DISTANCE)
                    continue;

                Color mixedColor = blendAll(CustomMapColors.colors[i], CustomMapColors.colors[j]);
                map.put(mixedColor, (short) (((i + 4) << 8) + (j + 4)));
            }
        }
        LOGGER.info("Generated Colormap of size {}", map.size());
        return map;
    }
}
