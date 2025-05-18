package net.rene.custommapimages.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapPostProcessingComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static net.rene.custommapimages.CustomMapImages.LOGGER;

public class CustomMapCommand {
    private static final Logger logger = LOGGER;
    private static final int MAX_IMAGE_WIDTH = 20 * 128;
    private static final int MAX_IMAGE_HEIGHT = 20 * 128;
    private static final int CHAT_ERROR_COLOR = 16711680;
    private static final int CHAT_MESSAGE_COLOR = 16762669;
    private CustomMapCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess cRA,
                                CommandManager.RegistrationEnvironment rE) {
        dispatcher.register(literal("map")
                .then(argument("url", StringArgumentType.string())
                        .executes(context -> run(
                                context.getSource(),
                                StringArgumentType.getString(context, "url"),
                                false                                                                 // default
                        ))
                        .then(argument("advanced pixel blending", BoolArgumentType.bool())      // extra blend
                                .executes(context -> run(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "url"),
                                        BoolArgumentType.getBool(context, "advanced pixel blending")
                                ))
                        )
                )
        );
    }

    private static boolean validUrl(String url, ServerCommandSource context) {
        if (url == null || url.isEmpty()) {
            logger.error("Empty URL");
            context.sendFeedback(() -> Text.literal("Your URL can't be empty").withColor(CHAT_ERROR_COLOR), true);
            return false;
        }

        if (!url.startsWith("https://")) {
            context.sendFeedback(() -> Text.literal("Only URLs starting with \"https://\" are valid").withColor(CHAT_ERROR_COLOR), true);
            return false;
        }
        return true;
    }

    private static BufferedImage getImageData(String url, ServerCommandSource context) {
        BufferedImage img;
        try {
            img = ImageIO.read(URI.create(url).toURL());
        }
        catch (IOException e) {
            logger.error("Failed to read image from url");
            context.sendFeedback(() -> Text.literal("Can't read image").withColor(CHAT_ERROR_COLOR),
                    true);

            return null;
        }
        logger.info("Reading image worked!");


        return img;
    }

    private static ItemStack[] createMapItem(BufferedImage img, ColorHelper cH, ServerPlayerEntity player, ServerWorld serverWorld, int[] mapSize) {
        int mapHeight = (int) (Math.ceil(img.getHeight() / (double) 128));
        int mapWidth = (int) (Math.ceil(img.getWidth() / (double) 128));
        CustomMapColors customMapColors = new CustomMapColors();
        ArrayList<ItemStack> mapItems = new ArrayList<>();
        // drawing map
        for (int map_i = 0; map_i < mapWidth; ++map_i) {
            for (int map_j = 0; map_j < mapHeight; ++map_j) {
                ItemStack mapItem = FilledMapItem.createMap(player.getWorld(), 40_000_000, 40_000_000, (byte) 0, false, false);
                MapState mapState = FilledMapItem.getMapState(mapItem, serverWorld);

                assert mapState != null;
                for (int i = 0; i < 128; ++i) {
                    if (i + map_i * 128 > img.getWidth()) {
                        break;
                    }
                    for (int j = 0; j < 128; ++j) {
                        if (j + map_j * 128 > img.getHeight()) {
                            break;
                        }
                        mapState.setColor(i, j, customMapColors.bestColor(
                                        new Color(
                                                cH.getRed(i + map_i * 128, j + map_j * 128) & 0xFF,
                                                cH.getGreen(i + map_i * 128, j + map_j * 128) & 0xFF,
                                                cH.getBlue(i + map_i * 128, j + map_j * 128) & 0xFF)
                                )
                        );
                    }
                }
                mapItem.set(DataComponentTypes.MAP_POST_PROCESSING, MapPostProcessingComponent.LOCK);
                mapItems.add(mapItem);
            }
        }
        mapSize[0] = mapWidth;
        mapSize[1] = mapHeight;

        return mapItems.toArray(new ItemStack[0]);
    }

    private static void giveItemToPlayer(ItemStack item, PlayerEntity player) {
        boolean bl = player.getInventory().insertStack(item);
        ItemEntity itemEntity = player.dropItem(item, false);
        if (bl && item.isEmpty()) {
            if (itemEntity != null) {
                itemEntity.setDespawnImmediately();
            }

            player.getWorld()
                    .playSound(
                            null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            SoundEvents.ENTITY_ITEM_PICKUP,
                            SoundCategory.PLAYERS,
                            0.2F,
                            ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
                    );

            player.currentScreenHandler.sendContentUpdates();

        } else {
            if (itemEntity != null) {
                itemEntity.resetPickupDelay();
                itemEntity.setOwner(player.getUuid());
            }
        }
    }


    private static int run(ServerCommandSource context, String url, boolean apb) {
        ServerPlayerEntity player = context.getPlayer();
        ServerWorld serverWorld = context.getWorld();
        assert player != null;
        int[] mapSize = new int[2];

        // checking url
        logger.info("Player {}, requested customMapImage from URL: {}", player.getName(), url);

        if (!validUrl(url, context)) {
            return -1;
        }

        // downloading image
        BufferedImage img = getImageData(url, context);
        if (img == null) {
            context.sendFeedback(() ->
                    Text.literal("Could not fetch image").withColor(CHAT_ERROR_COLOR), true);
            return -1;
        }

        ColorHelper cH = new ColorHelper(img);
        // checking if image is too big
        logger.info("Size of image {}x{}", img.getWidth(), img.getHeight());
        if (img.getWidth() > MAX_IMAGE_WIDTH || img.getHeight() > MAX_IMAGE_HEIGHT) {
            context.sendFeedback(() ->
                    Text.literal(String.format("Your image (%dx%d) is bigger than the maximum allowed " +
                            "limit (%dx%d)", img.getWidth(), img.getHeight(), MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT))
                    .withColor(CHAT_ERROR_COLOR), true);

            return -1;
        }
        ItemStack[] mapItems = createMapItem(img, cH, player, serverWorld, mapSize);
        for (ItemStack item: mapItems) {
            giveItemToPlayer(item, player);
        }

        context.sendFeedback(() -> Text.literal(String.format("Your image-size: %dx%d Blocks", mapSize[0], mapSize[1]))
                .withColor(CHAT_MESSAGE_COLOR), true);

        return 1;
    }
}
