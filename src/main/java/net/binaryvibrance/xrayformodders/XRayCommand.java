package net.binaryvibrance.xrayformodders;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class XRayCommand extends CommandBase {
	@Override
	public String getCommandName() {
		return "bvxray";
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return "bvxray [radiusInChunks] [filter]";
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
			}

			if (radius == null) {
				radius = 1;
			}

			int minX = (player.chunkCoordX - ((radius - 1) / 2));
			int maxX = (player.chunkCoordX + ((radius - 1) / 2));

			int minZ = (player.chunkCoordZ - ((radius - 1) / 2));
			int maxZ = (player.chunkCoordZ + ((radius - 1) / 2));

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
					Block b = world.getBlock(x, y, z);

					String unlocalizedName = b.getUnlocalizedName();
					if (!unlocalizedName.contains(":")) {
						unlocalizedName = "minecraft:" + unlocalizedName;
					}

					if (filter != null && unlocalizedName.contains(filter)) {
						world.setBlock(x, y, z, Blocks.air, 0, 0);
						world.markBlockForUpdate(x, y, z);
						blocksRemoved++;
						continue;
					}

					if (b == Blocks.stone || b == Blocks.sand || b == Blocks.grass || b == Blocks.gravel || b == Blocks.dirt) {
						boolean liquidFound = false;
						for (int[] neighbour : neighbours) {
							Block neighbourBlock = world.getBlock(x + neighbour[0], y + neighbour[1], z + neighbour[2]);
							if (neighbourBlock instanceof BlockStaticLiquid || neighbourBlock instanceof BlockDynamicLiquid) {
								liquidFound = true;
								break;
							}
						}

						if (!liquidFound && (filter == null)) {
							world.setBlock(x, y, z, Blocks.air, 0, 0);
							world.markBlockForUpdate(x, y, z);
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

