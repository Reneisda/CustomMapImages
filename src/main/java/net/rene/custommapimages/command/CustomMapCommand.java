package net.rene.custommapimages.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.MapPostProcessingComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.rene.custommapimages.CustomMapImages;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

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
            context.sendFeedback(() -> Text.literal("Your URL can't be empty"), true);
            return -1;
        }
        else {
            try {
                img = ImageIO.read(URI.create(url).toURL());
            }
            catch (IOException e) {
                logger.error("Failed to read image from url");
                context.sendFeedback(() -> Text.literal("Can't read image").withColor(CHAT_ERROR_COLOR),
                        true);

                return -1;
            }
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
                ItemStack mapItem = FilledMapItem.createMap(player.getWorld(), 0, 0, (byte) 0, false, false);
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
                        mapState.putColor(i, j, customMapColors.bestColor(
                                new Color(
                                image.getRed(i + map_i * 128, j + map_j * 128) & 0xFF,
                                image.getGreen(i + map_i * 128, j + map_j * 128) & 0xFF,
                                image.getBlue(i + map_i * 128, j + map_j * 128) & 0xFF)
                            )
                        );
                    }
                }
                logger.info(Registries.ITEM.getKeys().toString());
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
        logger.info(String.valueOf(Registries.DATA_COMPONENT_TYPE.getKey()));
        context.sendFeedback(() -> Text.literal(String.format("Your image-size: %dx%d Blocks", mapWidth, mapHeight))
                .withColor(CHAT_MESSAGE_COLOR), true);

        return 1;
    }
    /*
    private static int run(ServerCommandSource context, String url) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getPlayer();
        ServerWorld serverWorld = context.getWorld();
        assert player != null;
        BlockPos pos2 = player.getBlockPos().down();
        BlockPos pos = player.getChunkPos().getStartPos().north();
        String blockColor = String.valueOf(serverWorld.getBlockState(pos2).getBlock().getDefaultMapColor().id);
        context.sendFeedback(() -> Text.literal("Cords " + pos2.getX() + " " +  pos2.getY() + " " +
                pos2.getZ() + "\nBlockColor " + blockColor), true);

        BlockPos startPos = pos;
        // + one map -> 8 chunks/ 128 Blocks
        // middle -> 4

        context.sendFeedback(() -> Text.literal(String.format("Current chunk pos: %d %d %d", pos2.getX(), pos2.getY(), pos2.getZ())), true);
        // getting
        startPos = new BlockPos(startPos.getX() + (128 - startPos.getX() % 128), startPos.getY(), startPos.getZ() + (128 - startPos.getZ() % 128));
        BlockPos finalStartPos = startPos;
        context.sendFeedback(() -> Text.literal(String.format("Current map pos: %d %d %d", finalStartPos.getX(), finalStartPos.getY(), finalStartPos.getZ())), true);

        startPos = new BlockPos(startPos.getX() - 64, startPos.getY(), startPos.getZ() + 63);
        BlockPos tmp = startPos;

        // getting image
        logger.info("URL: " + url);
        BufferedImage img;
        if (url == null || url.isEmpty()) {
            try {
                img = ImageIO.read(new File("C:\\Users\\Rene\\IdeaProjects\\custommapimages-template-1.21\\src\\client\\java\\net\\rene\\custommapimages\\command\\img.png"));
            } catch (IOException e) {
                logger.error("Failed to read image");

                throw new RuntimeException(e);
            }
        }
        else {
            try {
                img = ImageIO.read(new URL(url));
            } catch (IOException e) {
                logger.error("Failed to read image from url");
                throw new RuntimeException(e);
            }
        }
        logger.info("Reading image worked!");

        ColorHelper image = new ColorHelper(img);
        logger.info(String.format("Size of image %dx%d", img.getWidth(), img.getHeight()));
        // checking if image is too big
        if (img.getWidth() > MAX_IMAGE_WIDTH || img.getHeight() > MAX_IMAGE_HEIGHT) {
            context.sendFeedback(() -> Text.literal(String.format("Your image (%dx%d) is bigger than the maximum allowed " +
                "limit (%dx%d)", img.getWidth(), img.getHeight(), MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT)).withColor(16711680), true);

            return -1;
        }

        ItemStack mapItem = FilledMapItem.createMap(player.getWorld(), startPos.getX() + 64, startPos.getX() -63, (byte) 1, false, false);
        MapIdComponent mapId = serverWorld.increaseAndGetMapId();
        int mapHeight = (int) (Math.ceil(img.getHeight() / (double) 128));
        int mapWidth = (int) (Math.ceil(img.getWidth() / (double) 128));


        int mapCount = mapWidth * mapHeight;
        GameMode playerGameMode = player.isInCreativeMode() ? GameMode.CREATIVE : GameMode.SURVIVAL;
        playerGameMode = player.isSpectator() ? GameMode.SPECTATOR : playerGameMode;
        player.changeGameMode(GameMode.SPECTATOR);

        for (int i = 0; i < img.getHeight(); ++i) {
            for (int j = 0; j < img.getWidth(); ++j) {
                Color color = new Color(image.getRed(j, i) & 0xFF, image.getGreen(j, i) & 0xFF, image.getBlue(j, i) & 0xFF);
                Block block_ = image.getBestBlock(color);
                if (block_ == Blocks.GLOW_LICHEN) {
                    tmp = tmp.down();
                    serverWorld.setBlockState(tmp, Blocks.GOLD_BLOCK.getDefaultState(), Block.NOTIFY_LISTENERS);
                    tmp = tmp.up();
                    serverWorld.setBlockState(tmp, Blocks.GLOW_LICHEN.getDefaultState().with(Properties.DOWN, true), Block.NOTIFY_LISTENERS);
                }
                else {
                    serverWorld.setBlockState(tmp, block_.getDefaultState(), Block.NOTIFY_LISTENERS);
                }
                tmp = tmp.north();
            }
            tmp = new BlockPos(tmp.getX() + 1, startPos.getY(), startPos.getZ());
        }
        MapState mapState = FilledMapItem.getMapState(mapId, player.getWorld());
        player.changeGameMode(playerGameMode);
        player.getInventory().insertStack(mapItem);
        return 1;
    }

     */

}
