package net.binaryvibrance.xrayformodders;

import net.minecraft.block.Block;
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
		return "bvxray [radiusInChunks]";
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
				radius = Integer.getInteger(arguments[0]);
			}

			if (radius == null) {
				radius = 3;
			}

			int blocksRemoved = doXRay(player.worldObj, player.chunkCoordX, player.chunkCoordZ, radius);
			player.addChatComponentMessage(new ChatComponentText(String.format("Removed %d blocks", blocksRemoved)));
		}
	}

	private int doXRay(World world, int chunkX, int chunkZ, int radius) {
		int blocksRemoved = 0;
		int minX = (chunkX - ((radius - 1) / 2)) * 16;
		int maxX = (chunkX + ((radius - 1) / 2)) * 16;

		int minZ = (chunkZ - ((radius - 1) / 2)) * 16;
		int maxZ = (chunkZ + ((radius - 1) / 2)) * 16;

		for (int x = minX; x < maxX; ++x) {
			for (int z = minZ; z < maxZ; ++z) {
				for (int y = 1; y < world.getHeight(); ++y) {
					Block b = world.getBlock(x, y, z);

					if (b == Blocks.stone || b == Blocks.sand || b == Blocks.grass || b == Blocks.gravel || b == Blocks.dirt) {
						boolean liquidFound = false;
						for (int[] neighbour : neighbours) {
							Block neighbourBlock = world.getBlock(x + neighbour[0], y + neighbour[1], z + neighbour[2]);
							if (neighbourBlock == Blocks.water || neighbourBlock == Blocks.lava) {
								liquidFound = true;
								break;
							}
						}

						if (!liquidFound) {
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
			{0, 0, 1},
			{1, 0, 0},
			{0, 0, -1},
			{-1, 0, 0},
			{0, 1, 0},
			{0, -1, 0}
	};
}

