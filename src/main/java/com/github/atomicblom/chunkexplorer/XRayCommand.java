package com.github.atomicblom.chunkexplorer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class XRayCommand extends CommandBase {
	@Override
	public String getName() {
		return "chunkxray";
	}

	@Override
	public String getUsage(ICommandSender commandSender) {
		return "chunkxray [radiusInChunks] [filter]";
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
			}

			if (radius == null) {
				radius = 0;
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
			player.sendMessage(new TextComponentString(String.format("clearing %d chunks", numberOfChunks)));
			long blocksRemoved = 0;
			float chunkProgress = 0;
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					blocksRemoved += doXRay(player.world, x, z, filter);
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

			player.sendMessage(new TextComponentString(String.format("Removed %d blocks, sending to client", blocksRemoved)));
		}
	}

	private long doXRay(World world, int chunkX, int chunkZ, String filter) {
		long blocksRemoved = 0;
		final int minX = chunkX * 16;
		final int maxX = chunkX * 16 + 16;

		final int minZ = chunkZ * 16;
		final int maxZ = chunkZ * 16 + 16;

		final MutableBlockPos blockPos = new MutableBlockPos();

		for (int x = minX; x < maxX; ++x) {
			for (int z = minZ; z < maxZ; ++z) {
				for (int y = 1; y < world.getHeight(); ++y) {
					blockPos.setPos(x, y, z);
					final IBlockState blockState = world.getBlockState(blockPos);
					final Block block = blockState.getBlock();

					String unlocalizedName = block.getRegistryName().toString().toLowerCase();

					if (filter == null || !unlocalizedName.contains(filter))
					{

						if (block == Blocks.STONE || block == Blocks.SAND || block == Blocks.GRASS || block == Blocks.GRAVEL || block == Blocks.DIRT)
						{
							boolean noLiquidFound = true;
							for (final EnumFacing neighbour : EnumFacing.VALUES)
							{
								final IBlockState neighbourBlockState = world.getBlockState(blockPos.offset(neighbour));
								final Block neighbourBlock = neighbourBlockState.getBlock();
								if (neighbourBlock instanceof BlockStaticLiquid || neighbourBlock instanceof BlockDynamicLiquid)
								{
									noLiquidFound = false;
									break;
								}
							}

							if (noLiquidFound && filter == null)
							{
								world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 0);
								blocksRemoved++;
							}
						}
					} else
					{
						world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 0);
						blocksRemoved++;
					}
				}
			}
		}

		return blocksRemoved;
	}
}

