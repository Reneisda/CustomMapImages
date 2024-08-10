package net.rene.custommapimages.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.component.type.MapPostProcessingComponent;
import net.minecraft.datafixer.fix.ItemNbtFix;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Urls;
import net.rene.custommapimages.CustomMapImages;
import org.slf4j.Logger;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;

public class CustomMapCommand {
    private static final Logger logger = CustomMapImages.LOGGER;
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.setblock.failed"));
    private static final int MAX_IMAGE_WIDTH = 20 * 128;
    private static final int MAX_IMAGE_HEIGHT = 20 * 128;

    private static final int MAX_IMAGE_SIZE = MAX_IMAGE_WIDTH * MAX_IMAGE_HEIGHT;      // 6,553,600 Blocks // 20x20 map
    private static final int CHAT_ERROR_COLOR = 16711680;
    private static final int CHAT_MESSAGE_COLOR = 16762669;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("map")
            .then(CommandManager.argument("url", StringArgumentType.string())
                .executes(
                    context -> run(
                        context.getSource(),
                        StringArgumentType.getString(context, "url")
                    )
                )
            )
        );
    }

    private static int run(ServerCommandSource context, String url) {
        ServerPlayerEntity player = context.getPlayer();
        ServerWorld serverWorld = context.getWorld();
        assert player != null;

        // getting image
        logger.info(String.format("Player %s, requested customMapImage from URL: %s", player.getName(), url));
        BufferedImage img;
        if (url == null || url.isEmpty()) {
            logger.error("Empty URL");
            context.sendFeedback(() -> Text.literal("Your URL can't be empty").withColor(CHAT_ERROR_COLOR), true);
            return -1;
        }

        if (!url.startsWith("https://")) {
            context.sendFeedback(() -> Text.literal("Only URLs starting with \"https://\" are valid").withColor(CHAT_ERROR_COLOR), true);
            return -1;
        }

        try {
            img = ImageIO.read(URI.create(url).toURL());
        }
        catch (IOException e) {
            logger.error("Failed to read image from url");
            context.sendFeedback(() -> Text.literal("Can't read image").withColor(CHAT_ERROR_COLOR),
                    true);

            return -1;
        }
        logger.info("Reading image worked!");

        ColorHelper image = new ColorHelper(img);
        logger.info(String.format("Size of image %dx%d", img.getWidth(), img.getHeight()));
        // checking if image is too big
        if (img.getWidth() > MAX_IMAGE_WIDTH || img.getHeight() > MAX_IMAGE_HEIGHT) {
            context.sendFeedback(() -> Text.literal(String.format("Your image (%dx%d) is bigger than the maximum allowed " +
                    "limit (%dx%d)", img.getWidth(), img.getHeight(), MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT))
                    .withColor(CHAT_ERROR_COLOR), true);

            return -1;
        }
        int mapHeight = (int) (Math.ceil(img.getHeight() / (double) 128));
        int mapWidth = (int) (Math.ceil(img.getWidth() / (double) 128));
        CustomMapColors customMapColors = new CustomMapColors();
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
                                image.getRed(i + map_i * 128, j + map_j * 128) & 0xFF,
                                image.getGreen(i + map_i * 128, j + map_j * 128) & 0xFF,
                                image.getBlue(i + map_i * 128, j + map_j * 128) & 0xFF)
                            )
                        );
                    }
                }
                mapItem.set(DataComponentTypes.MAP_POST_PROCESSING, MapPostProcessingComponent.LOCK);
                logger.info("ID: " + mapItem.get(DataComponentTypes.MAP_ID));

                boolean bl = player.getInventory().insertStack(mapItem);
                ItemEntity itemEntity = player.dropItem(mapItem, false);
                if (bl && mapItem.isEmpty()) {
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
        }
        context.sendFeedback(() -> Text.literal(String.format("Your image-size: %dx%d Blocks", mapWidth, mapHeight))
                .withColor(CHAT_MESSAGE_COLOR), true);

        return 1;
    }
}
