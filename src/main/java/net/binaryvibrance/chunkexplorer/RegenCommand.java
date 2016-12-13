package net.binaryvibrance.chunkexplorer;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;

import java.io.IOException;

public class RegenCommand extends CommandBase {
	@Override
	public String getCommandName() {
		return "chunkregen";
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return "chunkregen [radiusInChunks]";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 3;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender commandSender, String[] arguments) {
		if (commandSender instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) commandSender;

			int argumentNumber = 0;

			Integer radius = null;
			if (arguments.length > argumentNumber) {
				try {
					radius = Integer.parseInt(arguments[argumentNumber]);
				} catch (Exception e) {
				}
			}

			if (radius == null) {
				radius = 0;
			}

			int minX = (player.chunkCoordX - radius);
			int maxX = (player.chunkCoordX + radius);

			int minZ = (player.chunkCoordZ - radius);
			int maxZ = (player.chunkCoordZ + radius);

			int numberOfChunks = (maxX - minX + 1) * (maxZ - minZ + 1);
			player.addChatComponentMessage(new TextComponentString(String.format("regenerating %d chunks", numberOfChunks)));
			float chunkProgress = 0;

			World world = player.worldObj;
			if (!(world.getChunkProvider() instanceof ChunkProviderServer)) {
				return;
			}
			ChunkProviderServer provider = (ChunkProviderServer) world.getChunkProvider();

			BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

			for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
				for (int chunkX = minX; chunkX <= maxX; chunkX++) {
					IChunkLoader currentChunkLoader = provider.chunkLoader;
					provider.chunkLoader = null;
					final Chunk existingChunk = provider.getLoadedChunk(chunkX, chunkZ);
					if (existingChunk != null)
					{
						provider.id2ChunkMap.remove(existingChunk);
					}
					Chunk chunk = provider.loadChunk(chunkX, chunkZ);
					provider.chunkLoader = currentChunkLoader;

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
								pos.setPos(x + chunkX * 16, y, z + chunkZ * 16);
								world.markBlockForUpdate(pos);
							}
						}
					}

					chunkProgress++;
					player.addChatComponentMessage(new TextComponentString(String.format("%3.1f percent complete", chunkProgress / numberOfChunks * 100.0f)));
				}
			}
			player.addChatComponentMessage(new TextComponentString("Done."));
		}
	}
}

