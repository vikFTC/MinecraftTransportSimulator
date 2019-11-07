package mts_to_mc.interfaces;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FileResourcePack;
import net.minecraft.client.resources.IResourcePack;
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
	private static File modDirectory;
	private static File configDirectory;
	private static File musicDirectory;
	
	
	//Static initializer.  Variables will be set the first time we reference this class.
	static{
		try{
			modDirectory = new File(FileInterface.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			//TODO change this when we get out of a dev environment.
			modDirectory = new File("D:\\MinecraftDev\\mts_workspace\\run\\mods");
			configDirectory = new File(modDirectory.getParentFile(), "config");
			musicDirectory = new File(modDirectory.getParentFile(), "mts_music");
		}catch (URISyntaxException e){
			System.err.println("ERROR: COULD NOT GET MOD DIRECTORY.  THINGS WILL BREAK!");
			e.printStackTrace();
		}
	}
	
	public static void initLog(FMLPreInitializationEvent event){
		logger = event.getModLog();
	}
	
	public static void logError(String error){
		logger.error(error);
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
	
	
    /**Call this to add packs to the Minecraft resource system.  This allows for
     * pack resources to be loaded for internal things like item textures, as well
     * as allows for us to pass-in specific resources like auto-generated
     * JSON files that normally would have to exist in the pack.**/
	public static void registerJarForResources(File packJar, String packID){
		List<IResourcePack> resourcePacks;
		try{
			Field field = Minecraft.getMinecraft().getClass().getDeclaredField("field_110449_ao");
			field.setAccessible(true);
			resourcePacks = (List<IResourcePack>) field.get(Minecraft.getMinecraft());
		}catch(Exception e){
			try{
				Field field = Minecraft.getMinecraft().getClass().getDeclaredField("defaultResourcePacks");
				field.setAccessible(true);
				resourcePacks = (List<IResourcePack>) field.get(Minecraft.getMinecraft());
			}catch(Exception e2){
				logError("ERROR: COULD NOT USE REFLECTION TO FIND MINECRAFT RESOURCE PACKS!");
				logError(e.getMessage());
				logError(e2.getMessage());
				return;
			}
		}
		
		//Add the new resource pack.
		resourcePacks.add(new PackResourcePack(packJar, packID));
	}
	
	private static class PackResourcePack extends FileResourcePack{
		private final String packID;
		
		public PackResourcePack(File jarFile, String packID){
			super(jarFile);
			this.packID = packID;
		}
		
		@Override
		public boolean hasResourceName(String name){
			return name.contains(packID) && !name.endsWith(".mcmeta") ? true : super.hasResourceName(name);
		}
		
		@Override
		protected InputStream getInputStreamByName(String name) throws IOException{
			if(name.contains("models/item")){
				String itemName = name.substring(name.lastIndexOf("/") + 1, name.length() - ".json".length());
				//See, it's EASY Forge!  No need to make us make item JSONs!
				String jsonText = "{\"parent\":\"mts:item/basic\",\"textures\":{\"layer0\": \"mts:items/" + itemName + "\"}}";
				return new ByteArrayInputStream(jsonText.getBytes());
			}else{
				return super.getInputStreamByName(name);
			}
		}
	}
}
