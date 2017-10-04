package com.github.atomicblom.chunkexplorer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

public class SurveyCommand extends CommandBase {
	@Override
	public String getName() {
		return "chunksurvey";
	}

	@Override
	public String getUsage(ICommandSender commandSender) {
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

			final int minX = player.chunkCoordX - radius;
			final int maxX = player.chunkCoordX + radius;

			final int minZ = player.chunkCoordZ - radius;
			final int maxZ = player.chunkCoordZ + radius;

			final int numberOfChunks = (maxX - minX + 1) * (maxZ - minZ + 1);
			player.sendMessage(new TextComponentString(String.format("conducting survey over %d chunks", numberOfChunks)));
			final Map<String, List<Block>> locatedBlocks = Maps.newHashMap();
			long blocksSurveyed = 0;
			float chunkProgress = 0;
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					blocksSurveyed += doSurvey(player.world, x, z, locatedBlocks);
					chunkProgress++;
					player.sendMessage(new TextComponentString(String.format("%3.1f percent complete", chunkProgress / numberOfChunks * 100.0f)));
				}
			}

			if (blocksSurveyed == 0) {
				player.sendMessage(new TextComponentString("No Blocks in chunk?"));
				return;
			}

			player.sendMessage(new TextComponentString("----------------"));

			final SortedSet<Entry<String, List<Block>>> sortedSet = new TreeSet<>((blockListA, blockListB) -> {
				final int numCompare = blockListA.getValue().size() - blockListB.getValue().size();
				if (numCompare != 0) return numCompare;
				return blockListB.getKey().compareToIgnoreCase(blockListA.getKey());
			});
			sortedSet.addAll(locatedBlocks.entrySet());

			final Style white = new Style();
			white.setColor(TextFormatting.WHITE);
			final Style blockName = new Style();
			blockName.setColor(TextFormatting.BLUE);
			final Style blockCountStyle = new Style();
			blockCountStyle.setColor(TextFormatting.DARK_RED);
			final Style blockPercentStyle = new Style();
			blockPercentStyle.setColor(TextFormatting.DARK_RED);

			for (final Entry<String, List<Block>> kvp : sortedSet) {
				String name = kvp.getKey().substring(5);
				if (!name.contains(":")) {
					name = "minecraft:" + name;
				}

				if (filter == null || name.contains(filter))
				{
					final int count = kvp.getValue().size();
					final TextComponentString chat = new TextComponentString("");
					chat.setStyle(white);
					TextComponentString subChatComponent;
					subChatComponent = new TextComponentString(name);
					subChatComponent.setStyle(blockName);
					chat.appendSibling(subChatComponent);
					chat.appendText(" - ");
					subChatComponent = new TextComponentString(Integer.toString(count));
					subChatComponent.setStyle(blockCountStyle);
					chat.appendSibling(subChatComponent);
					chat.appendText(" blocks, ");
					subChatComponent = new TextComponentString(String.format("%3.3f", count / (float) blocksSurveyed * 100.0f));
					subChatComponent.setStyle(blockPercentStyle);
					chat.appendSibling(subChatComponent);
					chat.appendText("%");

					player.sendMessage(chat);
				}
			}
		}
	}

	private long doSurvey(World world, int chunkX, int chunkZ, Map<String, List<Block>> locatedBlocks) {
		long blocksSurveyed = 0;
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
					if (block != Blocks.AIR)
					{
						final String blockName = block.getUnlocalizedName();
						List<Block> blocks = locatedBlocks.get(blockName);
						if (blocks == null)
						{
							blocks = Lists.newArrayList();
							locatedBlocks.put(blockName, blocks);
						}
						blocks.add(block);
						blocksSurveyed++;
					}
				}
			}
		}

		return blocksSurveyed;
	}
}

