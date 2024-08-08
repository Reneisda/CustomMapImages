package net.rene.custommapimages.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.rene.custommapimages.CustomMapImages;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class CustomMapCommand {
    private static final Logger logger = CustomMapImages.LOGGER;
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.setblock.failed"));
    private String url;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("map")
                .requires(source -> source.hasPermissionLevel(2))
                        .executes(
                                context-> run(context.getSource(), null))
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


        return 1;
    }

    private static int execute(ServerCommandSource source, BlockStateArgument block) throws CommandSyntaxException {
        ServerWorld serverWorld = source.getWorld();


        BlockPos pos = source.getPlayer().getBlockPos().down();

        // Replace the block under the player
        BlockEntity blockEntity = serverWorld.getBlockEntity(pos);
        if (blockEntity != null) {
            Clearable.clear(blockEntity);
        }

        boolean blockSetSuccessfully = block.setBlockState(serverWorld, pos, Block.NOTIFY_LISTENERS);
        if (!blockSetSuccessfully) {
            throw FAILED_EXCEPTION.create();
        }

        serverWorld.updateNeighbors(pos, block.getBlockState().getBlock());
        source.sendFeedback(() ->Text.literal("HALLO :D, your cords are " + pos.getX() + " " +  pos.getY() + " " +
                pos.getZ() + "\nYou are standing on a block "), true);

        return 1;
    }


}
