package net.binaryvibrance.chunkexplorer;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = ChunkExplorer.MOD_ID, name = ChunkExplorer.MOD_NAME, version = ChunkExplorer.MOD_VERSION)
public class ChunkExplorer {
	public static final String MOD_ID = "chunkexplorer";
	public static final String MOD_NAME = "Chunk Explorer";
	public static final String MOD_VERSION = "@MOD_VERSION@";

	@Mod.Instance(ChunkExplorer.MOD_ID)
	public static ChunkExplorer instance;

	@Mod.EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new XRayCommand());
		event.registerServerCommand(new SurveyCommand());
		event.registerServerCommand(new RegenCommand());
		event.registerServerCommand(new ReplaceCommand());
	}
}
