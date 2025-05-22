package net.rene.custommapimages.command;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ColorHelper {
    private final int width;
    private final int height;
    private final boolean hasAlphaChannel;
    private int pixelLength;
    private final byte[] pixels;
    protected static final Color[] colors = {
            new Color(88, 124, 39),
            new Color(108, 151, 47),
            new Color(125, 176, 55),
            new Color(66, 93, 29),
            new Color(172, 162, 114),
            new Color(210, 199, 138),
            new Color(244, 230, 161),
            new Color(128, 122, 85),
            new Color(138, 138, 138),
            new Color(169, 169, 169),
            new Color(197, 197, 197),
            new Color(104, 104, 104),
            new Color(178, 0, 0),
            new Color(217, 0, 0),
            new Color(252, 0, 0),
            new Color(133, 0, 0),
            new Color(111, 111, 178),
            new Color(136, 136, 217),
            new Color(158, 158, 252),
            new Color(83, 83, 133),
            new Color(116, 116, 116),
            new Color(142, 142, 142),
            new Color(165, 165, 165),
            new Color(87, 87, 87),
            new Color(0, 86, 0),
            new Color(0, 105, 0),
            new Color(0, 123, 0),
            new Color(0, 64, 0),
            new Color(178, 178, 178),
            new Color(217, 217, 217),
            new Color(252, 252, 252),
            new Color(133, 133, 133),
            new Color(114, 117, 127),
            new Color(139, 142, 156),
            new Color(162, 166, 182),
            new Color(85, 87, 96),
            new Color(105, 75, 53),
            new Color(128, 93, 65),
            new Color(149, 108, 76),
            new Color(78, 56, 40),
            new Color(78, 78, 78),
            new Color(95, 95, 95),
            new Color(111, 111, 111),
            new Color(58, 58, 58),
            new Color(44, 44, 178),
            new Color(54, 54, 217),
            new Color(63, 63, 252),
            new Color(33, 33, 133),
            new Color(99, 83, 49),
            new Color(122, 101, 61),
            new Color(141, 118, 71),
            new Color(74, 62, 38),
            new Color(178, 175, 170),
            new Color(217, 214, 208),
            new Color(252, 249, 242),
            new Color(133, 131, 127),
            new Color(150, 88, 36),
            new Color(184, 108, 43),
            new Color(213, 125, 50),
            new Color(113, 66, 27),
            new Color(124, 52, 150),
            new Color(151, 64, 184),
            new Color(176, 75, 213),
            new Color(93, 40, 113),
            new Color(71, 107, 150),
            new Color(87, 130, 184),
            new Color(101, 151, 213),
            new Color(53, 80, 113),
            new Color(159, 159, 36),
            new Color(195, 195, 43),
            new Color(226, 226, 50),
            new Color(120, 120, 27),
            new Color(88, 142, 17),
            new Color(108, 174, 21),
            new Color(125, 202, 25),
            new Color(66, 107, 13),
            new Color(168, 88, 115),
            new Color(205, 108, 140),
            new Color(239, 125, 163),
            new Color(126, 66, 86),
            new Color(52, 52, 52),
            new Color(64, 64, 64),
            new Color(75, 75, 75),
            new Color(40, 40, 40),
            new Color(107, 107, 107),
            new Color(130, 130, 130),
            new Color(151, 151, 151),
            new Color(80, 80, 80),
            new Color(52, 88, 107),
            new Color(64, 108, 130),
            new Color(75, 125, 151),
            new Color(40, 66, 80),
            new Color(88, 43, 124),
            new Color(108, 53, 151),
            new Color(125, 62, 176),
            new Color(66, 33, 93),
            new Color(36, 52, 124),
            new Color(43, 64, 151),
            new Color(50, 75, 176),
            new Color(27, 40, 93),
            new Color(71, 52, 36),
            new Color(87, 64, 43),
            new Color(101, 75, 50),
            new Color(53, 40, 27),
            new Color(71, 88, 36),
            new Color(87, 108, 43),
            new Color(101, 125, 50),
            new Color(53, 66, 27),
            new Color(107, 36, 36),
            new Color(130, 43, 43),
            new Color(151, 50, 50),
            new Color(80, 27, 27),
            new Color(17, 17, 17),
            new Color(21, 21, 21),
            new Color(25, 25, 25),
            new Color(13, 13, 13),
            new Color(174, 166, 53),
            new Color(212, 203, 65),
            new Color(247, 235, 76),
            new Color(130, 125, 40),
            new Color(63, 152, 148),
            new Color(78, 186, 181),
            new Color(91, 216, 210),
            new Color(47, 114, 111),
            new Color(51, 89, 178),
            new Color(62, 109, 217),
            new Color(73, 126, 252),
            new Color(39, 66, 133),
            new Color(0, 151, 40),
            new Color(0, 185, 49),
            new Color(0, 214, 57),
            new Color(0, 113, 30),
            new Color(90, 59, 34),
            new Color(110, 73, 41),
            new Color(127, 85, 48),
            new Color(67, 44, 25),
            new Color(78, 1, 0),
            new Color(95, 1, 0),
            new Color(111, 2, 0),
            new Color(58, 1, 0),
            new Color(145, 123, 112),
            new Color(178, 150, 136),
            new Color(206, 175, 159),
            new Color(109, 92, 84),
            new Color(111, 56, 25),
            new Color(135, 69, 31),
            new Color(157, 81, 36),
            new Color(83, 42, 19),
            new Color(104, 60, 75),
            new Color(126, 74, 92),
            new Color(147, 86, 107),
            new Color(77, 45, 56),
            new Color(78, 75, 96),
            new Color(95, 92, 118),
            new Color(111, 107, 136),
            new Color(58, 56, 72),
            new Color(129, 92, 25),
            new Color(158, 113, 31),
            new Color(184, 131, 36),
            new Color(97, 69, 19),
            new Color(71, 81, 37),
            new Color(87, 99, 44),
            new Color(102, 116, 52),
            new Color(53, 60, 28),
            new Color(111, 53, 54),
            new Color(136, 65, 66),
            new Color(158, 76, 77),
            new Color(83, 40, 40),
            new Color(40, 28, 24),
            new Color(48, 35, 30),
            new Color(56, 40, 35),
            new Color(30, 21, 18),
            new Color(94, 74, 68),
            new Color(115, 91, 83),
            new Color(133, 106, 97),
            new Color(70, 55, 50),
            new Color(60, 63, 63),
            new Color(74, 78, 78),
            new Color(86, 91, 91),
            new Color(45, 47, 47),
            new Color(85, 50, 61),
            new Color(104, 61, 74),
            new Color(121, 72, 87),
            new Color(63, 38, 45),
            new Color(52, 42, 63),
            new Color(64, 52, 78),
            new Color(75, 61, 91),
            new Color(40, 32, 47),
            new Color(52, 35, 24),
            new Color(64, 42, 30),
            new Color(75, 49, 35),
            new Color(40, 26, 18),
            new Color(52, 56, 29),
            new Color(64, 69, 36),
            new Color(75, 81, 41),
            new Color(40, 42, 22),
            new Color(99, 41, 32),
            new Color(121, 50, 39),
            new Color(140, 59, 45),
            new Color(74, 31, 24),
            new Color(26, 15, 11),
            new Color(31, 18, 13),
            new Color(37, 22, 16),
            new Color(19, 11, 8),
            new Color(131, 33, 34),
            new Color(161, 40, 41),
            new Color(187, 47, 48),
            new Color(99, 25, 25),
            new Color(103, 43, 67),
            new Color(125, 53, 82),
            new Color(146, 62, 96),
            new Color(77, 33, 50),
            new Color(63, 17, 20),
            new Color(78, 21, 25),
            new Color(91, 25, 29),
            new Color(47, 13, 15),
            new Color(15, 87, 93),
            new Color(18, 107, 114),
            new Color(22, 125, 132),
            new Color(11, 65, 69),
            new Color(40, 99, 97),
            new Color(49, 121, 119),
            new Color(57, 140, 138),
            new Color(30, 74, 73),
            new Color(59, 31, 42),
            new Color(73, 37, 52),
            new Color(85, 43, 61),
            new Color(44, 23, 32),
            new Color(14, 125, 92),
            new Color(17, 153, 113),
            new Color(20, 178, 131),
            new Color(10, 94, 69),
            new Color(69, 69, 69),
            new Color(85, 85, 85),
            new Color(99, 99, 99),
            new Color(51, 51, 51),
            new Color(150, 122, 102),
            new Color(184, 148, 125),
            new Color(213, 173, 145),
            new Color(113, 91, 76),
            new Color(88, 116, 104),
            new Color(108, 142, 127),
            new Color(125, 165, 148),
            new Color(66, 87, 78),
    };


    ColorHelper(BufferedImage image) {
        pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        width = image.getWidth();
        height = image.getHeight();
        hasAlphaChannel = image.getAlphaRaster() != null;
        pixelLength = 3;
        if (hasAlphaChannel) {
            pixelLength = 4;
        }
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
        int rMean = (red1 + red2) >> 1;
        int r = red1 - red2;
        int g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        return (((512 + rMean) * r * r) >> 8) + 4 * g * g + (double) (((767 - rMean) * b * b) >> 8);
    }
}
