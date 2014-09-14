package net.binaryvibrance.xrayformodders;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class RegenCommand extends CommandBase {
	@Override
	public String getCommandName() {
		return "bvregen";
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return "bvregen [diameterInChunks]";
	}

	@Override
	public void processCommand(ICommandSender commandSender, String[] arguments) {
		if (commandSender instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) commandSender;

			if (!MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile())) {
				player.addChatComponentMessage(new ChatComponentText("You need to be operator to use this command."));
			}

			int argumentNumber = 0;

			Integer radius = null;
			if (arguments.length > argumentNumber) {
				try {
					radius = Integer.parseInt(arguments[argumentNumber]);
				} catch (Exception e) {
				}
			}

			if (radius == null) {
				radius = 1;
			}

			int minX = (player.chunkCoordX - ((radius - 1) / 2));
			int maxX = (player.chunkCoordX + ((radius - 1) / 2));

			int minZ = (player.chunkCoordZ - ((radius - 1) / 2));
			int maxZ = (player.chunkCoordZ + ((radius - 1) / 2));

			int numberOfChunks = (maxX - minX + 1) * (maxZ - minZ + 1);
			player.addChatComponentMessage(new ChatComponentText(String.format("regenerating %d chunks", numberOfChunks)));
			float chunkProgress = 0;

			World world = player.worldObj;
			if (!(world.getChunkProvider() instanceof ChunkProviderServer)) {
				return;
			}
			ChunkProviderServer provider = (ChunkProviderServer) world.getChunkProvider();
			for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
				for (int chunkX = minX; chunkX <= maxX; chunkX++) {
					long k = ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ);
					IChunkLoader currentChunkLoader = provider.currentChunkLoader;
					provider.currentChunkLoader = null;
					provider.loadedChunkHashMap.remove(k);
					Chunk chunk = provider.originalLoadChunk(chunkX, chunkZ);
					provider.currentChunkLoader = currentChunkLoader;
					try {
						currentChunkLoader.saveChunk(world, chunk);
					} catch (MinecraftException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					for (int z = 0; z < 16; ++z) {
						for (int x = 0; x < 16; ++x) {
							for (int y = 0; y < world.getHeight(); ++y) {
								world.markBlockForUpdate(x + chunkX * 16, y, z + chunkZ * 16);
							}
						}
					}

					chunkProgress++;
					player.addChatComponentMessage(new ChatComponentText(String.format("%3.1f percent complete", chunkProgress / numberOfChunks * 100.0f)));
				}
			}
			player.addChatComponentMessage(new ChatComponentText("Done."));
		}
	}
}

