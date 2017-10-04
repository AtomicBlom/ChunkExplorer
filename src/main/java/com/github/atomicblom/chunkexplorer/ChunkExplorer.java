package com.github.atomicblom.chunkexplorer;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import java.util.Map;

@Mod(modid = ChunkExplorer.MOD_ID, name = ChunkExplorer.MOD_NAME, version = ChunkExplorer.MOD_VERSION)
public class ChunkExplorer {
	public static final String MOD_ID = "chunkexplorer";
	public static final String MOD_NAME = "Chunk Explorer";
	public static final String MOD_VERSION = "@MOD_VERSION@";

	@Mod.EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new XRayCommand());
		event.registerServerCommand(new SurveyCommand());
		event.registerServerCommand(new RegenCommand());
		event.registerServerCommand(new ReplaceCommand());
	}

	@NetworkCheckHandler
	public boolean checkRemoteVersions(Map<String, String> versions, Side side) {
		if (side.isClient()) {
			return true;
		}
		return true;
	}
}

