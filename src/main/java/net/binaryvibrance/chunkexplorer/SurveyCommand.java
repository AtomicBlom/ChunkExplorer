package net.binaryvibrance.chunkexplorer;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.*;

public class SurveyCommand extends CommandBase {
	@Override
	public String getCommandName() {
		return "chunksurvey";
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return "chunksurvey [radiusInChunks] [filter]";
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
			}

			if (radius == null) {
				radius = 0;
			}

			int minX = (player.chunkCoordX - radius);
			int maxX = (player.chunkCoordX + radius);

			int minZ = (player.chunkCoordZ - radius);
			int maxZ = (player.chunkCoordZ + radius);

			int numberOfChunks = (maxX - minX + 1) * (maxZ - minZ + 1);
			player.addChatComponentMessage(new TextComponentString(String.format("conducting survey over %d chunks", numberOfChunks)));
			Hashtable<String, List<Block>> locatedBlocks = new Hashtable<String, List<Block>>();
			long blocksSurveyed = 0;
			float chunkProgress = 0;
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					blocksSurveyed += doSurvey(player.worldObj, x, z, locatedBlocks);
					chunkProgress++;
					player.addChatComponentMessage(new TextComponentString(String.format("%3.1f percent complete", chunkProgress / numberOfChunks * 100.0f)));
				}
			}

			if (blocksSurveyed == 0) {
				player.addChatComponentMessage(new TextComponentString(String.format("No Blocks in chunk?")));
				return;
			}

			player.addChatComponentMessage(new TextComponentString("----------------"));

			SortedSet<Map.Entry<String, List<Block>>> sortedSet = new TreeSet<Map.Entry<String, List<Block>>>(new Comparator<Map.Entry<String, List<Block>>>() {
				@Override
				public int compare(Map.Entry<String, List<Block>> blockListA, Map.Entry<String, List<Block>> blockListB) {
					int numCompare = blockListB.getValue().size() - blockListA.getValue().size();
					if (numCompare != 0) return numCompare;
					return blockListB.getKey().compareToIgnoreCase(blockListA.getKey());
				}
			});
			sortedSet.addAll(locatedBlocks.entrySet());

			Style white = new Style();
			white.setColor(TextFormatting.WHITE);
			Style blockName = new Style();
			blockName.setColor(TextFormatting.BLUE);
			Style blockCountStyle = new Style();
			blockCountStyle.setColor(TextFormatting.DARK_RED);
			Style blockPercentStyle = new Style();
			blockPercentStyle.setColor(TextFormatting.DARK_RED);

			for (Map.Entry<String, List<Block>> kvp : sortedSet) {
				String name = kvp.getKey().substring(5);
				if (!name.contains(":")) {
					name = "minecraft:" + name;
				}

				if (filter != null && !name.contains(filter)) {
					continue;
				}

				int count = kvp.getValue().size();
				TextComponentString chat = new TextComponentString("");
				TextComponentString subChatComponent;
				chat.setStyle(white);
				subChatComponent = new TextComponentString(name);
				subChatComponent.setStyle(blockName);
				chat.appendSibling(subChatComponent);
				chat.appendText(" - ");
				subChatComponent = new TextComponentString(Integer.toString(count));
				subChatComponent.setStyle(blockCountStyle);
				chat.appendSibling(subChatComponent);
				chat.appendText(" blocks, ");
				subChatComponent = new TextComponentString(String.format("%3.3f", count / (float)blocksSurveyed * 100.0f));
				subChatComponent.setStyle(blockPercentStyle);
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

		BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

		for (int x = minX; x < maxX; ++x) {
			for (int z = minZ; z < maxZ; ++z) {
				for (int y = 1; y < world.getHeight(); ++y) {
					blockPos.setPos(x, y, z);
					IBlockState blockState = world.getBlockState(blockPos);
					final Block b = blockState.getBlock();
					if (b == Blocks.AIR) continue;
					String blockName = b.getUnlocalizedName();
					List<Block> blocks = locatedBlocks.get(blockName);
					if (blocks == null) {
						blocks = new ArrayList<>();
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

