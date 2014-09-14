package net.binaryvibrance.xrayformodders;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ReplaceCommand extends CommandBase {
	@Override
	public String getCommandName() {
		return "bvreplace";
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return "bvreplace [radiusInChunks] blockToReplace blockToReplaceWith";
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
				player.addChatComponentMessage(new ChatComponentText(String.format("No replacement specified")));
				return;
			} else {
				replacementBlock = Block.getBlockFromName(replaceBlock);
			}

			if (replacementBlock == null) {
				List<Block> potentialBlocks = new ArrayList<Block>();
				for(Object blockObject : Block.blockRegistry) {
					Block b = (Block)blockObject;
					if (b.getUnlocalizedName().contains(replaceBlock)) {
						potentialBlocks.add(b);
					}
				}
				if (potentialBlocks.size() == 0) {
					player.addChatComponentMessage(new ChatComponentText(String.format("Could not find type of replacement.")));
					return;
				} else if (potentialBlocks.size() == 1) {
					replacementBlock = potentialBlocks.get(0);
				} else {
					player.addChatComponentMessage(new ChatComponentText(String.format("replacement type not specific enough, it could have been one of:")));
					for (Block b : potentialBlocks) {
						player.addChatComponentMessage(new ChatComponentText(b.getUnlocalizedName()));
					}
				}

			}

			int minX = (player.chunkCoordX - radius);
			int maxX = (player.chunkCoordX + radius);

			int minZ = (player.chunkCoordZ - radius);
			int maxZ = (player.chunkCoordZ + radius);

			int numberOfChunks = (maxX - minX + 1) * (maxZ - minZ + 1);
			player.addChatComponentMessage(new ChatComponentText(String.format("conducting survey over %d chunks", numberOfChunks)));
			long blocksSurveyed = 0;
			float chunkProgress = 0;
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					blocksSurveyed += doSurvey(player.worldObj, x, z, filter, replacementBlock);
					chunkProgress++;
					player.addChatComponentMessage(new ChatComponentText(String.format("%3.1f percent complete", chunkProgress / numberOfChunks * 100.0f)));
				}
			}

			if (blocksSurveyed == 0) {
				player.addChatComponentMessage(new ChatComponentText(String.format("No Blocks in chunk?")));
				return;
			}
			player.addChatComponentMessage(new ChatComponentText(String.format("%d blocks replaced", blocksSurveyed)));
		}
	}

	private long doSurvey(World world, int chunkX, int chunkZ, String filter, Block replacement) {
		long blocksSurveyed = 0;
		int minX = chunkX * 16;
		int maxX = chunkX * 16 + 16;
		int minZ = chunkZ * 16;
		int maxZ = chunkZ * 16 + 16;

		for (int x = minX; x < maxX; ++x) {
			for (int z = minZ; z < maxZ; ++z) {
				for (int y = 1; y < world.getHeight(); ++y) {
					Block b = world.getBlock(x, y, z);

					if (filter != null && !b.getUnlocalizedName().contains(filter)) {
						continue;
					}

					world.setBlock(x, y, z, replacement, 0, 2);
					world.markBlockForUpdate(x, y, z);
					blocksSurveyed++;
				}
			}
		}

		return blocksSurveyed;
	}
}

