package net.binaryvibrance.xrayformodders;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = XRay.MOD_ID, name = XRay.MOD_NAME, version = XRay.MOD_VERSION)
public class XRay {
	public static final String MOD_ID = "xrayformodders";
	public static final String MOD_NAME = "XRay for Modders";
	public static final String MOD_VERSION = "@MOD_VERSION@";

	@Mod.Instance(XRay.MOD_ID)
	public static XRay instance;

	@Mod.EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new XRayCommand());
		event.registerServerCommand(new SurveyCommand());
	}
}

