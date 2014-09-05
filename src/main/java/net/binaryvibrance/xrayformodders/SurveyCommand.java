package net.binaryvibrance.xrayformodders;

import joptsimple.util.KeyValuePair;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import java.util.*;

public class SurveyCommand extends CommandBase {
	@Override
	public String getCommandName() {
		return "bvsurvey";
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return "bvsurvey [radiusInChunks] [filter]";
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

			String filter = null;
			if (arguments.length >= 2) {
				filter = arguments[1];
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

			player.addChatComponentMessage(new ChatComponentText("----------------"));

			SortedSet<Map.Entry<String, List<Block>>> sortedSet = new TreeSet<Map.Entry<String, List<Block>>>(new Comparator<Map.Entry<String, List<Block>>>() {
				@Override
				public int compare(Map.Entry<String, List<Block>> blockListA, Map.Entry<String, List<Block>> blockListB) {
					return blockListB.getValue().size() - blockListA.getValue().size();
				}
			});
			sortedSet.addAll(locatedBlocks.entrySet());

			ChatStyle white = new ChatStyle();
			white.setColor(EnumChatFormatting.WHITE);
			ChatStyle blockName = new ChatStyle();
			blockName.setColor(EnumChatFormatting.BLUE);
			ChatStyle blockCountStyle = new ChatStyle();
			blockCountStyle.setColor(EnumChatFormatting.DARK_RED);
			ChatStyle blockPercentStyle = new ChatStyle();
			blockPercentStyle.setColor(EnumChatFormatting.DARK_RED);

			for (Map.Entry<String, List<Block>> kvp : sortedSet) {
				String name = kvp.getKey().substring(5);
				if (filter != null && !name.contains(filter)) {
					continue;
				}

				int count = kvp.getValue().size();
				ChatComponentText chat = new ChatComponentText("");
				ChatComponentText subChatComponent;
				chat.setChatStyle(white);
				subChatComponent = new ChatComponentText(kvp.getKey().substring(5));
				subChatComponent.setChatStyle(blockName);
				chat.appendSibling(subChatComponent);
				chat.appendText(" - ");
				subChatComponent = new ChatComponentText(Integer.toString(count));
				subChatComponent.setChatStyle(blockCountStyle);
				chat.appendSibling(subChatComponent);
				chat.appendText(" blocks, ");
				subChatComponent = new ChatComponentText(String.format("%3.3f", count / (float)blocksSurveyed * 100.0f));
				subChatComponent.setChatStyle(blockPercentStyle);
				chat.appendSibling(subChatComponent);
				chat.appendText("%");

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
					String blockName = b.getUnlocalizedName();
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
}

