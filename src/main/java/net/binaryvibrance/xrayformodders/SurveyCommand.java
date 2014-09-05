package net.binaryvibrance.xrayformodders;

import joptsimple.util.KeyValuePair;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.*;

public class SurveyCommand extends CommandBase {
	@Override
	public String getCommandName() {
		return "bvsurvey";
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return "bvsurvey [radiusInChunks]";
	}

	@Override
	public void processCommand(ICommandSender commandSender, String[] arguments) {
		if (commandSender instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) commandSender;

			if (!MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile())) {
				player.addChatComponentMessage(new ChatComponentText("You need to be operator to use this command."));
			}

			Integer radius = null;
			if (arguments.length >= 1) {
				try {
					radius = Integer.parseInt(arguments[0]);
				} catch (Exception e) {

				}
			}

			if (radius == null) {
				radius = 3;
			}

			int minX = (player.chunkCoordX - ((radius - 1) / 2));
			int maxX = (player.chunkCoordX + ((radius - 1) / 2));

			int minZ = (player.chunkCoordZ - ((radius - 1) / 2));
			int maxZ = (player.chunkCoordZ + ((radius - 1) / 2));

			int numberOfChunks = (maxX - minX + 1) * (maxZ - minZ + 1);
			player.addChatComponentMessage(new ChatComponentText(String.format("conducting survey over %d chunks", numberOfChunks)));
			Hashtable<String, List<Block>> locatedBlocks = new Hashtable<String, List<Block>>();
			long blocksSurveyed = 0;
			float chunkProgress = 0;
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					blocksSurveyed += doSurvey(player.worldObj, x, z, locatedBlocks);
					chunkProgress++;
					player.addChatComponentMessage(new ChatComponentText(String.format("%3.1f percent complete", chunkProgress / numberOfChunks * 100.0f)));
				}
			}

			if (blocksSurveyed == 0) {
				player.addChatComponentMessage(new ChatComponentText(String.format("No Blocks in chunk?")));
				return;
			}

			for (Map.Entry<String, List<Block>> kvp : locatedBlocks.entrySet()) {
				int count = kvp.getValue().size();
				ChatComponentText chat = new ChatComponentText(String.format("%s - %d blocks, %3.2f percent", kvp.getKey(), count, count / (float)blocksSurveyed * 100.0f));
				player.addChatComponentMessage(chat);
			}
		}
	}

	private long doSurvey(World world, int chunkX, int chunkZ, Dictionary<String, List<Block>> locatedBlocks) {
		long blocksSurveyed = 0;
		int minX = chunkX * 16;
		int maxX = chunkX * 16 + 16;

		int minZ = chunkZ * 16;
		int maxZ = chunkZ * 16 + 16;

		for (int x = minX; x < maxX; ++x) {
			for (int z = minZ; z < maxZ; ++z) {
				for (int y = 1; y < world.getHeight(); ++y) {
					Block b = world.getBlock(x, y, z);
					if (b == Blocks.air) continue;
					String blockName = b.getLocalizedName();
					List<Block> blocks = locatedBlocks.get(blockName);
					if (blocks == null) {
						blocks = new ArrayList<Block>();
						locatedBlocks.put(blockName, blocks);
					}
					blocks.add(b);
					blocksSurveyed++;
				}
			}
		}

		return blocksSurveyed;
	}

	static final int[][] neighbours = {
			{0, 0, 1},
			{1, 0, 0},
			{0, 0, -1},
			{-1, 0, 0},
			{0, 1, 0},
			{0, -1, 0}
	};

	private class CountHolder {
		int count;
	}
}

