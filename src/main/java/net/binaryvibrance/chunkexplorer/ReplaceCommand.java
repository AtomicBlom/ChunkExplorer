package net.binaryvibrance.chunkexplorer;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ReplaceCommand extends CommandBase {
	@Override
	public String getCommandName() {
		return "chunkreplace";
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
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
			EntityPlayerMP player = (EntityPlayerMP) commandSender;

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
				argumentNumber++;
			}

			String replaceBlock = null;
			if (arguments.length > argumentNumber) {
				replaceBlock = arguments[argumentNumber];
			}

			if (radius == null) {
				radius = 0;
			}

			Block replacementBlock;

			if (replaceBlock == null) {
				player.addChatComponentMessage(new TextComponentString(String.format("No replacement specified")));
				return;
			} else {
				replacementBlock = Block.getBlockFromName(replaceBlock);
			}

			if (replacementBlock == null) {
				List<Block> potentialBlocks = new ArrayList<Block>();
				for(Object blockObject : Block.REGISTRY) {
					Block b = (Block)blockObject;
					final String unlocalizedName = b.getUnlocalizedName();
					if (unlocalizedName.contains(replaceBlock)) {
						potentialBlocks.add(b);
					}
				}
				if (potentialBlocks.size() == 0) {
					player.addChatComponentMessage(new TextComponentString(String.format("Could not find type of replacement.")));
					return;
				} else if (potentialBlocks.size() == 1) {
					replacementBlock = potentialBlocks.get(0);
				} else {
					player.addChatComponentMessage(new TextComponentString(String.format("replacement type not specific enough, it could have been one of:")));
					for (Block b : potentialBlocks) {
						player.addChatComponentMessage(new TextComponentString(b.getUnlocalizedName()));
					}
				}

			}

			int minX = (player.chunkCoordX - radius);
			int maxX = (player.chunkCoordX + radius);

			int minZ = (player.chunkCoordZ - radius);
			int maxZ = (player.chunkCoordZ + radius);

			int numberOfChunks = (maxX - minX + 1) * (maxZ - minZ + 1);
			player.addChatComponentMessage(new TextComponentString(String.format("conducting survey over %d chunks", numberOfChunks)));
			long blocksSurveyed = 0;
			float chunkProgress = 0;
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					blocksSurveyed += doSurvey(player.worldObj, x, z, filter, replacementBlock);
					chunkProgress++;
					player.addChatComponentMessage(new TextComponentString(String.format("%3.1f percent complete", chunkProgress / numberOfChunks * 100.0f)));
				}
			}

			if (blocksSurveyed == 0) {
				player.addChatComponentMessage(new TextComponentString(String.format("No Blocks in chunk?")));
				return;
			}
			player.addChatComponentMessage(new TextComponentString(String.format("%d blocks replaced", blocksSurveyed)));
		}
	}

	private long doSurvey(World world, int chunkX, int chunkZ, String filter, Block replacement) {
		long blocksSurveyed = 0;
		int minX = chunkX * 16;
		int maxX = chunkX * 16 + 16;
		int minZ = chunkZ * 16;
		int maxZ = chunkZ * 16 + 16;

		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int x = minX; x < maxX; ++x) {
			for (int z = minZ; z < maxZ; ++z) {
				for (int y = 1; y < world.getHeight(); ++y) {
					pos.setPos(x, y, z);
					IBlockState blockState = world.getBlockState(pos);
					Block b = blockState.getBlock();

					final String unlocalizedName = b.getUnlocalizedName().toLowerCase();
					if (filter != null && !unlocalizedName.contains(filter)) {
						continue;
					}

					final IBlockState newState = replacement.getDefaultState();
					world.setBlockState(pos, newState, 2);
					//world.notifyBlockUpdate(pos, blockState, newState, 2);
					blocksSurveyed++;
				}
			}
		}

		return blocksSurveyed;
	}
}

