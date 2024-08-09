package net.rene.custommapimages.command;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.HashMap;

public class ColorHelper {
    HashMap<Color, Block> map;

    private final int width;
    private int height;
    private final boolean hasAlphaChannel;
    private int pixelLength;
    private byte[] pixels;
    private byte[] blockHeightDelta;
    public static final Color PALE_GREEN = new Color(125, 175, 55);         // slime_block
    public static final Color PALE_YELLOW = new Color(210, 198, 138);       // sandstone
    public static final Color WHITE_GRAY = new Color(168, 168, 168);        // mushroom_stem
    public static final Color BRIGHT_RED = new Color(217, 0, 0);            // redstone_block
    public static final Color WHITE = new Color(217, 217, 217);             // white_wool
    public static final Color PALE_PURPLE = new Color(136, 136, 217);       // blue_ice
    public static final Color IRON_GRAY = new Color(142, 142, 142);         // iron_block
    public static final Color DARK_GREEN = new Color(0, 104, 0);            // oak_leaves
    public static final Color LIGHT_BLUE_GRAY = new Color(139, 142, 156);   // clay
    public static final Color DIRT_BROWN = new Color(149, 107, 76);         // dirt
    public static final Color STONE_GRAY = new Color(94, 94, 94);           // stone
    public static final Color WATER_BLUE = new Color(63, 63, 251);            // water
    public static final Color OAK_TAN = new Color(121, 100, 61);            // oak_wood
    public static final Color OFF_WHITE = new Color(217, 214, 208);         // quartz_block
    public static final Color ORANGE = new Color(183, 107, 43);             // orange_wool
    public static final Color MAGENTA = new Color(151, 64, 183);            // magenta_wool
    public static final Color LIGHT_BLUE = new Color(87, 130, 183);         // light_blue_wool
    public static final Color YELLOW = new Color(194, 194, 43);             // yellow_wool
    public static final Color LIME = new Color(125, 201, 25);               // lime_wool
    public static final Color PINK = new Color(205, 107, 140);              // pink_wool
    public static final Color GRAY = new Color(64, 64, 64);                 // gray_wool
    public static final Color LIGHT_GRAY = new Color(130, 130, 130);        // light_gray_wool
    public static final Color CYAN = new Color(64, 107, 130);               // cyan_wool
    public static final Color PURPLE = new Color(107, 53, 151);             // purple_wool
    public static final Color BLUE = new Color(43, 64, 151);                // blue
    public static final Color BROWN = new Color(87, 64, 43);                // brown_wool
    public static final Color GREEN = new Color(87, 107, 43);           // green wool
    public static final Color RED = new Color(151, 50, 50);            // red_wool
    public static final Color BLACK = new Color(21, 21, 21);                // black_wool
    public static final Color GOLD = new Color(212, 202, 65);           // gold_block
    public static final Color DIAMOND_BLUE = new Color(78, 185, 180);    // diamond_block
    public static final Color LAPIS_BLUE = new Color(62, 108, 217);      // lapis_block
    public static final Color EMERALD_GREEN = new Color(0, 184, 49);     // emerald_block
    public static final Color SPRUCE_BROWN = new Color(109, 73, 41);    // spruce_wood
    public static final Color DARK_RED = new Color(94, 1, 0);        // red_nether_bricks
    public static final Color TERRACOTTA_WHITE = new Color(177, 150, 136);       // white_terracotta
    public static final Color TERRACOTTA_ORANGE = new Color(156, 81, 35);      // orange_terracotta
    public static final Color TERRACOTTA_MAGENTA = new Color(151, 64, 183);      // magenta_terracotta
    public static final Color TERRACOTTA_LIGHT_BLUE = new Color(94, 92, 117);   // light_blue_terracotta
    public static final Color TERRACOTTA_YELLOW = new Color(157, 112, 31);      // yellow_terracotta
    public static final Color TERRACOTTA_LIME = new Color(87, 89, 44);         // lime_terracotta
    public static final Color TERRACOTTA_PINK = new Color(136, 65, 66);        // pink_terracotta
    public static final Color TERRACOTTA_GRAY = new Color(48, 34, 30);         // gray_terracotta
    public static final Color TERRACOTTA_LIGHT_GRAY = new Color(114, 91, 83);   // light_gray_terracotta
    public static final Color TERRACOTTA_CYAN = new Color(74, 78, 78);         // cyan_terracotta
    public static final Color TERRACOTTA_PURPLE = new Color(120, 72, 87);       // purple_terracotta
    public static final Color TERRACOTTA_BLUE = new Color(64, 52, 78);         // blue_terracotta
    public static final Color TERRACOTTA_BROWN = new Color(64, 42, 30);        // brown_terracotta
    public static final Color TERRACOTTA_GREEN = new Color(64, 69, 35);        // green_terracotta
    public static final Color TERRACOTTA_RED = new Color(120, 50, 38);          // red_terracotta
    public static final Color TERRACOTTA_BLACK = new Color(31, 18, 13);        // black_terracotta
    public static final Color DULL_RED = new Color(186, 47, 48);               // crimson_nylium
    public static final Color DULL_PINK = new Color(125, 53, 82);               // crimson_planks
    public static final Color DARK_CRIMSON = new Color(78, 21, 25);            // stripped_crimson_hyphae
    public static final Color TEAL = new Color(18, 106, 113);                       // oxidized_copper
    public static final Color DARK_AQUA = new Color(49, 120, 118);               // warped_planks
    public static final Color DARK_DULL_PINK = new Color(85, 43, 61);          // stripped_warped_hyphae -> warped_hyphae
    public static final Color BRIGHT_TEAL = new Color(17, 153, 112);             // warped_wart_block
    public static final Color DEEPSLATE_GRAY = new Color(85, 85, 85);          // deepslate
    public static final Color RAW_IRON_PINK = new Color(183, 148, 124);          // raw_iron_block
    public static final Color LICHEN_GREEN = new Color(107, 142, 127);            // glow_lichen

    ColorHelper(BufferedImage image) {
        pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        width = image.getWidth();
        height = image.getHeight();
        hasAlphaChannel = image.getAlphaRaster() != null;
        pixelLength = 3;
        if (hasAlphaChannel) {
            pixelLength = 4;
        }
        this.map = new HashMap<Color, Block>();
        create_color_map();
        blockHeightDelta = new byte[width * height];

    }

    private void create_color_map() {
        map.put(PALE_GREEN, Blocks.SLIME_BLOCK);
        map.put(PALE_YELLOW, Blocks.SANDSTONE);
        map.put(WHITE_GRAY, Blocks.MUSHROOM_STEM);
        map.put(BRIGHT_RED, Blocks.REDSTONE_BLOCK);
        map.put(WHITE, Blocks.WHITE_WOOL);
        map.put(PALE_PURPLE, Blocks.BLUE_ICE);
        map.put(IRON_GRAY, Blocks.IRON_BLOCK);
        map.put(DARK_GREEN, Blocks.OAK_LEAVES);
        map.put(LIGHT_BLUE_GRAY, Blocks.CLAY);
        map.put(DIRT_BROWN, Blocks.DIRT);
        map.put(STONE_GRAY, Blocks.STONE);
        map.put(WATER_BLUE, Blocks.WATER);
        map.put(OAK_TAN, Blocks.OAK_WOOD);
        map.put(OFF_WHITE, Blocks.QUARTZ_BLOCK);
        map.put(ORANGE, Blocks.ORANGE_WOOL);
        map.put(MAGENTA, Blocks.MAGENTA_WOOL);
        map.put(LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL);
        map.put(YELLOW, Blocks.YELLOW_WOOL);
        map.put(LIME, Blocks.LIME_WOOL);
        map.put(PINK, Blocks.PINK_WOOL);
        map.put(GRAY, Blocks.GRAY_WOOL);
        map.put(LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL);
        map.put(CYAN, Blocks.CYAN_WOOL);
        map.put(PURPLE, Blocks.PURPLE_WOOL);
        map.put(BLUE, Blocks.BLUE_WOOL);
        map.put(BROWN, Blocks.BROWN_WOOL);
        map.put(GREEN, Blocks.GREEN_WOOL);
        map.put(RED, Blocks.RED_WOOL);
        map.put(BLACK, Blocks.BLACK_WOOL);
        map.put(GOLD, Blocks.GOLD_BLOCK);
        map.put(DIAMOND_BLUE, Blocks.DIAMOND_BLOCK);
        map.put(LAPIS_BLUE, Blocks.LAPIS_BLOCK);
        map.put(EMERALD_GREEN, Blocks.EMERALD_BLOCK);
        map.put(SPRUCE_BROWN, Blocks.SPRUCE_WOOD);
        map.put(DARK_RED, Blocks.RED_NETHER_BRICKS);
        map.put(TERRACOTTA_WHITE, Blocks.WHITE_TERRACOTTA);
        map.put(TERRACOTTA_ORANGE, Blocks.ORANGE_TERRACOTTA);
        map.put(TERRACOTTA_MAGENTA, Blocks.MAGENTA_TERRACOTTA);
        map.put(TERRACOTTA_LIGHT_BLUE, Blocks.LIGHT_BLUE_TERRACOTTA);
        map.put(TERRACOTTA_YELLOW, Blocks.YELLOW_TERRACOTTA);
        map.put(TERRACOTTA_LIME, Blocks.LIME_TERRACOTTA);
        map.put(TERRACOTTA_PINK, Blocks.PINK_TERRACOTTA);
        map.put(TERRACOTTA_GRAY, Blocks.GRAY_TERRACOTTA);
        map.put(TERRACOTTA_LIGHT_GRAY, Blocks.LIGHT_GRAY_TERRACOTTA);
        map.put(TERRACOTTA_CYAN, Blocks.CYAN_TERRACOTTA);
        map.put(TERRACOTTA_PURPLE, Blocks.PURPLE_TERRACOTTA);
        map.put(TERRACOTTA_BLUE, Blocks.BLUE_TERRACOTTA);
        map.put(TERRACOTTA_BROWN, Blocks.BROWN_TERRACOTTA);
        map.put(TERRACOTTA_GREEN, Blocks.GREEN_TERRACOTTA);
        map.put(TERRACOTTA_RED, Blocks.RED_TERRACOTTA);
        map.put(TERRACOTTA_BLACK, Blocks.BLACK_TERRACOTTA);
        map.put(DULL_RED, Blocks.CRIMSON_NYLIUM);
        map.put(DULL_PINK, Blocks.CRIMSON_PLANKS);
        map.put(DARK_CRIMSON, Blocks.STRIPPED_CRIMSON_HYPHAE);
        map.put(TEAL, Blocks.OXIDIZED_COPPER);
        map.put(DARK_AQUA, Blocks.WARPED_PLANKS);
        map.put(DARK_DULL_PINK, Blocks.WARPED_HYPHAE);
        map.put(BRIGHT_TEAL, Blocks.WARPED_WART_BLOCK);
        map.put(DEEPSLATE_GRAY, Blocks.DEEPSLATE);
        map.put(RAW_IRON_PINK, Blocks.RAW_IRON_BLOCK);
        map.put(LICHEN_GREEN, Blocks.GLOW_LICHEN);
    }

    int getRGB(int x, int y) {
        int pos = (y * pixelLength * width) + (x * pixelLength);
        int argb = -16777216; // 255 alpha
        if (hasAlphaChannel) {
            argb = (((int) pixels[pos++] & 0xff) << 24); // alpha
        }

        argb += ((int) pixels[pos++] & 0xff); // blue
        argb += (((int) pixels[pos++] & 0xff) << 8); // green
        argb += (((int) pixels[pos] & 0xff) << 16); // red
        return argb;
    }

    byte getBlue(int x, int y) {
        try {
            int pos = (y * pixelLength * width) + (x * pixelLength);
            if (hasAlphaChannel) {
                pos++;
            }
            return pixels[pos];
        } catch (IndexOutOfBoundsException e) {
            return 0;
        }
    }
    byte getGreen(int x, int y) {
        try {
            int pos = (y * pixelLength * width) + (x * pixelLength);
            if (hasAlphaChannel) {
                pos++;
            }
            return pixels[++pos];
        } catch (IndexOutOfBoundsException e) {
            return 0;
        }
    }
    byte getRed(int x, int y){
            try {
                int pos = (y * pixelLength * width) + (x * pixelLength);
                if (hasAlphaChannel) {
                    pos++;
                }
                pos += 2;
                return pixels[pos];
            } catch (IndexOutOfBoundsException e) {
                return 0;
            }
        }

    static double colorDistance(Color c1, Color c2) {
        int red1 = c1.getRed();
        int red2 = c2.getRed();
        int rmean = (red1 + red2) >> 1;
        int r = red1 - red2;
        int g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        return Math.sqrt((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8));
    }
    public Block getBestBlock(Color color) {
        double smallestDistance = Double.MAX_VALUE;
        Block bestBlock = Blocks.GLASS;
        for (Color c: map.keySet()) {
            double dist = colorDistance(c, color);
            if (dist < smallestDistance) {
                smallestDistance = dist;
                bestBlock = map.get(c);
            }
        }
        return bestBlock;
    }

}