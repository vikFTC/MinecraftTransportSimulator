package mts_to_mc.interfaces;

import java.io.File;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Helper class for interfacing with Minecraft.
 * This class is used for interfacing with file systems.  Maintains and stores
 * access to various file directories, such as mod and config folders.  It also
 * contains a reference to the logger that will output debug information
 * with the rest of the game log, and will run the init() method of 
 * {@link minecrafttransportsimulator.packs.PackLoader} once the directory
 * where mods are stored is known.
 *
 * @author don_bruce
 */
public class FileInterface{
	private static Logger logger;
	private static File gameDirectory;
	private static File modDirectory;
	private static File configDirectory;
	private static File musicDirectory;
	
	
	public static void init(FMLPreInitializationEvent event){
		logger = event.getModLog();
		gameDirectory = new File(event.getModConfigurationDirectory().getParent());
		modDirectory = new File(gameDirectory + File.separator + "mods");
		configDirectory = event.getModConfigurationDirectory();
		musicDirectory = new File(gameDirectory + File.separator + "mts_music");
	}
	
	public static void logError(String error){
		logger.error(error);
	}
	
	public static File getGameDirectory(){
		return gameDirectory;
	}
	
	public static File getModDirectory(){
		return modDirectory;
	}
	
	public static File getConfigDirectory(){
		return configDirectory;
	}
	
	public static File getMusicDirectory(){
		return musicDirectory;
	}

}
