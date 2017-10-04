package com.github.atomicblom.chunkexplorer;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import java.util.List;

public class ReplaceCommand extends CommandBase {
	@Override
	public String getName() {
		return "chunkreplace";
	}

	@Override
	public String getUsage(ICommandSender commandSender) {
		return "chunkreplace [radiusInChunks] blockToReplace blockToReplaceWith";
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

			int argumentNumber = 0;
			Integer radius = null;
			if (arguments.length > argumentNumber) {
				try {
					radius = Integer.parseInt(arguments[argumentNumber]);
					argumentNumber++;
				} catch (final NumberFormatException e) {
				}
			}

			String filter = null;
			if (arguments.length > argumentNumber) {
				filter = arguments[argumentNumber];
				argumentNumber++;
			}

			String replaceBlock = null;
			if (arguments.length > argumentNumber) {
				replaceBlock = arguments[argumentNumber];
			}

			if (radius == null) {
				radius = 0;
			}

			if (replaceBlock == null) {
				player.sendMessage(new TextComponentString("No replacement specified"));
				return;
			}

			Block replacementBlock = Block.getBlockFromName(replaceBlock);

			if (replacementBlock == null) {
				final List<Block> potentialBlocks = Lists.newArrayList();
				for(final Object blockObject : Block.REGISTRY) {
					final Block block = (Block)blockObject;
					final String unlocalizedName = block.getUnlocalizedName();
					if (unlocalizedName.contains(replaceBlock)) {
						potentialBlocks.add(block);
					}
				}
				if (potentialBlocks.isEmpty()) {
					player.sendMessage(new TextComponentString("Could not find type of replacement."));
					return;
				} else if (potentialBlocks.size() == 1) {
					replacementBlock = potentialBlocks.get(0);
				} else {
					player.sendMessage(new TextComponentString("replacement type not specific enough, it could have been one of:"));
					for (final Block potentialBlock : potentialBlocks) {
						player.sendMessage(new TextComponentString(potentialBlock.getUnlocalizedName()));
					}
				}
			}

			if (!(player.world instanceof WorldServer)) {
				return;
			}
			final WorldServer world = (WorldServer)player.world;

			final int minX = player.chunkCoordX - radius;
			final int maxX = player.chunkCoordX + radius;

			final int minZ = player.chunkCoordZ - radius;
			final int maxZ = player.chunkCoordZ + radius;

			final int numberOfChunks = (maxX - minX + 1) * (maxZ - minZ + 1);
			player.sendMessage(new TextComponentString(String.format("conducting survey over %d chunks", numberOfChunks)));
			long blocksSurveyed = 0;
			float chunkProgress = 0;
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					blocksSurveyed += doReplace(player.world, x, z, filter, replacementBlock);
					chunkProgress++;
					player.sendMessage(new TextComponentString(String.format("%3.1f percent complete", chunkProgress / numberOfChunks * 100.0f)));
				}
			}

			if (blocksSurveyed == 0) {
				player.sendMessage(new TextComponentString("No Blocks in chunk?"));
				return;
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

			player.sendMessage(new TextComponentString(String.format("%d blocks replaced", blocksSurveyed)));
		}
	}

	private long doReplace(World world, int chunkX, int chunkZ, String filter, Block replacement) {
		long blocksSurveyed = 0;
		final int minX = chunkX * 16;
		final int maxX = chunkX * 16 + 16;
		final int minZ = chunkZ * 16;
		final int maxZ = chunkZ * 16 + 16;

		final MutableBlockPos pos = new MutableBlockPos();
		for (int x = minX; x < maxX; ++x) {
			for (int z = minZ; z < maxZ; ++z) {
				for (int y = 1; y < world.getHeight(); ++y) {
					pos.setPos(x, y, z);
					final IBlockState blockState = world.getBlockState(pos);
					final Block block = blockState.getBlock();

					final String registryName = block.getRegistryName().toString().toLowerCase();
					if (filter == null || registryName.contains(filter))
					{
						final IBlockState newState = replacement.getDefaultState();
						world.setBlockState(pos, newState, 2);
						blocksSurveyed++;
					}
				}
			}
		}

		return blocksSurveyed;
	}
}

