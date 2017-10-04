package com.github.atomicblom.chunkexplorer;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import java.io.IOException;

public class RegenCommand extends CommandBase {
	@Override
	public String getName() {
		return "chunkregen";
	}

	@Override
	public String getUsage(ICommandSender commandSender) {
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
			final EntityPlayerMP player = (EntityPlayerMP) commandSender;

			final int argumentNumber = 0;

			Integer radius = null;
			if (arguments.length > argumentNumber) {
				try {
					radius = Integer.parseInt(arguments[argumentNumber]);
				} catch (final NumberFormatException e) {
				}
			}

			if (radius == null) {
				radius = 0;
			}

			final int minX = player.chunkCoordX - radius;
			final int maxX = player.chunkCoordX + radius;

			final int minZ = player.chunkCoordZ - radius;
			final int maxZ = player.chunkCoordZ + radius;

			final int numberOfChunks = (maxX - minX + 1) * (maxZ - minZ + 1);
			player.sendMessage(new TextComponentString(String.format("regenerating %d chunks", numberOfChunks)));

			if (!(player.world instanceof WorldServer)) {
				return;
			}
			final WorldServer world = (WorldServer)player.world;

			final ChunkProviderServer provider = world.getChunkProvider();

			float chunkProgress = 0;
			for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
				for (int chunkX = minX; chunkX <= maxX; chunkX++) {
					final long chunkId = ChunkPos.asLong(chunkX, chunkZ);
					provider.id2ChunkMap.remove(chunkId);
					final Chunk chunk = provider.chunkGenerator.generateChunk(chunkX, chunkZ);
					provider.id2ChunkMap.put(chunkId, chunk);
					chunk.onLoad();
					chunk.populate(provider, provider.chunkGenerator);
					chunk.onTick(false);

					try {
						provider.chunkLoader.saveChunk(world, chunk);
					} catch (final MinecraftException | IOException e) {
					}

					chunkProgress++;
					player.sendMessage(new TextComponentString(String.format("%3.1f percent complete", chunkProgress / numberOfChunks * 100.0f)));
				}
			}

			final PlayerChunkMap playerChunkMap = world.getPlayerChunkMap();
			for (final EntityPlayer playerEntity : world.playerEntities)
			{
				if (playerEntity instanceof EntityPlayerMP)
				{
					final EntityPlayerMP mp = (EntityPlayerMP) playerEntity;
					playerChunkMap.removePlayer(mp);
					playerChunkMap.addPlayer(mp);
				}
			}
			player.sendMessage(new TextComponentString("Done."));
		}
	}
}

