package net.binaryvibrance.chunkexplorer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class XRayCommand extends CommandBase {
	@Override
	public String getCommandName() {
		return "chunkxray";
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return "chunkxray [radiusInChunks] [filter]";
	}

	@Override
	public void processCommand(ICommandSender commandSender, String[] arguments) {
		if (commandSender instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) commandSender;

			if (!MinecraftServer.getServer().getConfigurationManager().canSendCommands(player.getGameProfile())) {
				player.addChatComponentMessage(new ChatComponentText("You need to be operator to use this command."));
			}

			int argumentNumber = 0;
			Integer radius = null;
			if (arguments.length > argumentNumber) {
				try {
					radius = Integer.parseInt(arguments[argumentNumber]);
					argumentNumber++;
				} catch (Exception e) {

				}
			}
			String filter = null;
			if (arguments.length > argumentNumber) {
				filter = arguments[argumentNumber];
			}

			if (radius == null) {
				radius = 0;
			}

			int minX = (player.chunkCoordX - radius);
			int maxX = (player.chunkCoordX + radius);

			int minZ = (player.chunkCoordZ - radius);
			int maxZ = (player.chunkCoordZ + radius);

			int numberOfChunks = (maxX - minX + 1) * (maxZ - minZ + 1);
			player.addChatComponentMessage(new ChatComponentText(String.format("clearing %d chunks", numberOfChunks)));
			long blocksRemoved = 0;
			float chunkProgress = 0;
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					blocksRemoved += doXRay(player.worldObj, x, z, filter);
					chunkProgress++;
					player.addChatComponentMessage(new ChatComponentText(String.format("%3.1f percent complete", chunkProgress / numberOfChunks * 100.0f)));

				}
			}

			player.addChatComponentMessage(new ChatComponentText(String.format("Removed %d blocks, sending to client", blocksRemoved)));
		}
	}

	private long doXRay(World world, int chunkX, int chunkZ, String filter) {
		long blocksRemoved = 0;
		int minX = chunkX * 16;
		int maxX = chunkX * 16 + 16;

		int minZ = chunkZ * 16;
		int maxZ = chunkZ * 16 + 16;

		for (int x = minX; x < maxX; ++x) {
			for (int z = minZ; z < maxZ; ++z) {
				for (int y = 1; y < world.getHeight(); ++y) {
					final BlockPos blockPos = new BlockPos(x, y, z);
					IBlockState blockState = world.getBlockState(blockPos);
					Block b = blockState.getBlock();

					String unlocalizedName = b.getUnlocalizedName();
					if (!unlocalizedName.contains(":")) {
						unlocalizedName = "minecraft:" + unlocalizedName;
					}

					if (filter != null && unlocalizedName.contains(filter)) {
						world.setBlockState(blockPos, Blocks.air.getDefaultState(), 0);
						world.markBlockForUpdate(blockPos);
						blocksRemoved++;
						continue;
					}

					if (b == Blocks.stone || b == Blocks.sand || b == Blocks.grass || b == Blocks.gravel || b == Blocks.dirt) {
						boolean liquidFound = false;
						for (EnumFacing neighbour : EnumFacing.VALUES) {
							IBlockState neighbourBlockState = world.getBlockState(blockPos.offset(neighbour));
							Block neighbourBlock = neighbourBlockState.getBlock();
							if (neighbourBlock instanceof BlockStaticLiquid || neighbourBlock instanceof BlockDynamicLiquid) {
								liquidFound = true;
								break;
							}
						}

						if (!liquidFound && (filter == null)) {
							world.setBlockState(blockPos, Blocks.air.getDefaultState(), 0);
							world.markBlockForUpdate(blockPos);
							blocksRemoved++;
						}
					}
				}
			}
		}

		return blocksRemoved;
	}

	static final int[][] neighbours = {
			{1, 0, 0},
			{0, 1, 0},
			{0, 0, 1},
			{-1, 0, 0},
			{0, -1, 0},
			{0, 0, -1},
	};
}

