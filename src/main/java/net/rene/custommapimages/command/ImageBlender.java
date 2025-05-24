package net.rene.custommapimages.command;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    public static byte[][] blendRow(BufferedImage image, int row, Map<Integer, byte[]> colorCache) {

        int width = image.getWidth();
        byte[][] blendRow = new byte[2][width * 2];  // double the size

        for (int i = 0; i < width; ++i) {
            int index = width - 1 - i;
            Color color = new Color(image.getRGB(index, row));
            int rgb = color.getRGB();

            byte[] result = colorCache.computeIfAbsent(rgb, key -> {
                Color best = getBestColor(new Color(key));
                short s = colorPairs.get(best);
                return new byte[]{(byte) (s & 0xFF), (byte) ((s >> 8) & 0xFF)};
            });
            byte c0 = result[0];
            byte c1 = result[0];
            blendRow[0][i * 2]      = c0;
            blendRow[0][i * 2 + 1]  = c1;
            blendRow[1][i * 2]      = c1;
            blendRow[1][i * 2 + 1]  = c0;
        }

        return blendRow;
    }


    public static byte[][] blend(BufferedImage image) throws InterruptedException {
        Map<Integer, byte[]> colorCache = new ConcurrentHashMap<>();
        int width = image.getWidth();
        int height = image.getHeight();
        byte[][] raster = new byte[height * 2][width * 2];

        int numThreads = Runtime.getRuntime().availableProcessors();
        try (ExecutorService executor = Executors.newFixedThreadPool(numThreads)) {
            CountDownLatch latch = new CountDownLatch(height);

            for (int row = 0; row < height; ++row) {
                final int rowIndex = row;
                executor.submit(() -> {
                    byte[][] rowData = blendRow(image, rowIndex, colorCache);
                    raster[rowIndex * 2] = rowData[0];
                    raster[rowIndex * 2 + 1] = rowData[1];
                    latch.countDown();
                });
            }

            latch.await();
            executor.shutdown();
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
        HashMap<Color, Short> map = HashMap.newHashMap(ALLOWED_COLOR_DISTANCE / 2);     // colors 3895
        for (int i = 0; i < CustomMapColors.colors.length; ++i) {                                   // due to CD
            for (int j = 0; j < CustomMapColors.colors.length; ++j) {                               // (color-dist)
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
