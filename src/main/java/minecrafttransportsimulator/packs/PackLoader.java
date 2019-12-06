package minecrafttransportsimulator.packs;

import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.gson.Gson;

import minecrafttransportsimulator.items.core.AItemPackComponent;
import minecrafttransportsimulator.items.core.ItemDecor;
import minecrafttransportsimulator.items.core.ItemInstrument;
import minecrafttransportsimulator.items.core.ItemItem;
import minecrafttransportsimulator.items.core.ItemVehicle;
import minecrafttransportsimulator.items.parts.AItemPart;
import minecrafttransportsimulator.items.parts.ItemPartBarrel;
import minecrafttransportsimulator.items.parts.ItemPartBullet;
import minecrafttransportsimulator.items.parts.ItemPartCrate;
import minecrafttransportsimulator.items.parts.ItemPartCustom;
import minecrafttransportsimulator.items.parts.ItemPartEngineAircraft;
import minecrafttransportsimulator.items.parts.ItemPartEngineBoat;
import minecrafttransportsimulator.items.parts.ItemPartEngineCar;
import minecrafttransportsimulator.items.parts.ItemPartEngineJet;
import minecrafttransportsimulator.items.parts.ItemPartGeneric;
import minecrafttransportsimulator.items.parts.ItemPartGroundDevicePontoon;
import minecrafttransportsimulator.items.parts.ItemPartGroundDeviceSkid;
import minecrafttransportsimulator.items.parts.ItemPartGroundDeviceTread;
import minecrafttransportsimulator.items.parts.ItemPartGroundDeviceWheel;
import minecrafttransportsimulator.items.parts.ItemPartGun;
import minecrafttransportsimulator.items.parts.ItemPartPropeller;
import minecrafttransportsimulator.packs.components.PackComponentDecor;
import minecrafttransportsimulator.packs.components.PackComponentInstrument;
import minecrafttransportsimulator.packs.components.PackComponentItem;
import minecrafttransportsimulator.packs.components.PackComponentPart;
import minecrafttransportsimulator.packs.components.PackComponentSign;
import minecrafttransportsimulator.packs.components.PackComponentVehicle;
import minecrafttransportsimulator.packs.objects.PackObjectDecor;
import minecrafttransportsimulator.packs.objects.PackObjectInstrument;
import minecrafttransportsimulator.packs.objects.PackObjectItem;
import minecrafttransportsimulator.packs.objects.PackObjectPart;
import minecrafttransportsimulator.packs.objects.PackObjectSign;
import minecrafttransportsimulator.packs.objects.PackObjectVehicle;
import minecrafttransportsimulator.packs.objects.PackObjectVehicle.PackFileDefinitions;
import mts_to_mc.interfaces.FileInterface;

/**
 * Class responsible for loading packs from the mod folder and parsing them out into their respective components.
 * Contains lists with all components found and loaded.  Use the info contained in the components to switch
 * between the String, Item, and operational forms of the pack objects.
 * <br>
 * <br>If you wish to hot-load objects into the mod that are generated on-the-fly and are not in JARs, do the following: 
 * <br>1) Create a new PackComponentXXXXX object for the map.  This will require you to create an item that extends {@link minecrafttransportsimulator.items.core.AItemPackComponent}
 * <br>2) Add that component to the appropriate map.
 * <br> 
 * <br>If you don't have a mtspack.info file in your mod, you will also have to:
 * <br>1) Add an entry to the packObjects map for your pack
 * <br>2) Call {@link mts_to_mc.interfaces.FileInterface#registerJarForResources(File, String)} and pass-in your JAR as a File object.
 * <br>
 * <br>These ensure that your pack will be linked for loading of resources and MUST be done before PreInit.  Failure to do this will result
 * in MTS not being able to load resources from the jar for textures and models.
 *
 * @author don_bruce
 */
public final class PackLoader{
	//Maps containing pack components that have been loaded.  Keyed by packID.
	public static final Map<String, PackInfoObject> packObjects = new HashMap<String, PackInfoObject>();
    public static final Map<String, List<PackComponentVehicle>> vehicleComponents = new HashMap<String, List<PackComponentVehicle>>();
    public static final Map<String, List<PackComponentPart>> partComponents = new HashMap<String, List<PackComponentPart>>();
    public static final Map<String, List<PackComponentInstrument>> instrumentComponents = new HashMap<String, List<PackComponentInstrument>>();
    public static final Map<String, List<PackComponentSign>> signComponents = new HashMap<String, List<PackComponentSign>>();
    public static final Map<String, List<PackComponentDecor>> decorComponents = new HashMap<String, List<PackComponentDecor>>();
    public static final Map<String, List<PackComponentItem>> itemComponents = new HashMap<String, List<PackComponentItem>>();
    
    
    /**This should be called before any pack operations are performed.
     * This will get any packs present in the mod directory and load them into the game.
     * Only goes two deep.  One will get to the "mods" directory, the other will get
     * into any version-specific directories.**/
    public static void init(){
    	for(File file : FileInterface.getModDirectory().listFiles()){
			if(file.getName().endsWith(".jar")){
				//We are a jar.  Open the jar and see if we are a pack.
				try{
					PackInfoObject packObject = null;
					ZipFile jarFile = new ZipFile(file);
					
					Enumeration<? extends ZipEntry> entries = jarFile.entries();
					while(entries.hasMoreElements()){
						ZipEntry entry = entries.nextElement();
						if(entry.getName().endsWith("mtspack.info")){
							packObject = new Gson().fromJson(new InputStreamReader(jarFile.getInputStream(entry)), PackInfoObject.class);
							break;
						}
					}
					
					if(packObject != null){
						//Set the packObject to the MTSPack.info file.
						packObjects.put(packObject.packID, packObject);
						
						//Registers the pack jarFile with the Minecraft Resource registry.
						FileInterface.registerJarForResources(file, packObject.packID);
						
						//Create lists to hold components.  Will need to sort them later.
					    List<PackComponentVehicle> packVehicleComponents = new ArrayList<PackComponentVehicle>();
					    List<PackComponentPart> packPartComponents = new ArrayList<PackComponentPart>();
					    List<PackComponentInstrument> packInstrumentComponents = new ArrayList<PackComponentInstrument>();
					    List<PackComponentSign> packSignComponents = new ArrayList<PackComponentSign>();
					    List<PackComponentDecor> packDecorComponents = new ArrayList<PackComponentDecor>();
					    List<PackComponentItem> packItemComponents = new ArrayList<PackComponentItem>();
					    
						entries = jarFile.entries();
						while(entries.hasMoreElements()){
							ZipEntry entry = entries.nextElement();
							String entryName = entry.getName();
							if(entryName.endsWith(".json") && entryName.contains("jsondefs")){
								String entryFileName = entryName.substring(entryName.lastIndexOf('/') + 1, entryName.length() - ".json".length()); 
								try{
									InputStreamReader reader = new InputStreamReader(jarFile.getInputStream(entry));
									if(entryName.contains("/vehicles/")){
										packVehicleComponents.addAll(parseVehicleJSON(packObject.packID, entryFileName, reader));
									}else if(entryName.contains("/parts/")){
										packPartComponents.add(parsePartJSON(packObject.packID, entryFileName, reader));
									}else if(entryName.contains("/instruments/")){
										packInstrumentComponents.add(parseInstrumentJSON(packObject.packID, entryFileName, reader));
									}else if(entryName.contains("/signs/")){
										packSignComponents.add(parseSignJSON(packObject.packID, entryFileName, reader));
									}else if(entryName.contains("/decors/")){
										packDecorComponents.add(parseDecorJSON(packObject.packID, entryFileName, reader));
									}else if(entryName.contains("/items/")){
										packItemComponents.add(parseItemJSON(packObject.packID, entryFileName, reader));
									}else{
										System.err.println("ERROR: No valid pack component found for " + entryFileName + " Skipping!");
									}
								}catch(Exception e){
									System.err.println("ERROR: Could not parse .json: " + entryFileName);
									e.printStackTrace();
								}
							}
						}
						
						//Sort the lists to get them to be ordered in creative tabs and crafting benches.
						Collections.sort(packVehicleComponents);
						Collections.sort(packPartComponents);
						Collections.sort(packInstrumentComponents);
						Collections.sort(packSignComponents);
						Collections.sort(packDecorComponents);
						Collections.sort(packItemComponents);
						
						//Now add the lists to the maps.
						vehicleComponents.put(packObject.packID, packVehicleComponents);
						partComponents.put(packObject.packID, packPartComponents);
						instrumentComponents.put(packObject.packID, packInstrumentComponents);
						signComponents.put(packObject.packID, packSignComponents);
						decorComponents.put(packObject.packID, packDecorComponents);
						itemComponents.put(packObject.packID, packItemComponents);
					}
					jarFile.close();
				}catch(Exception e){
					System.err.println("ERROR: Could not open .jar file for parsing: " + file.getName());
					e.printStackTrace();
				}
			}
    	}
    }
    
    /**Call this to get every item in all packs.  Most of the time you should access
     * the specific map for the pack you want, but sometimes all items are needed
     * (say for registration), in which case this helper method may be used.**/
    public static List<AItemPackComponent> getAllPackItems(){
    	List<AItemPackComponent> itemList = new ArrayList<AItemPackComponent>();
    	for(String packName : packObjects.keySet()){
    		for(PackComponentVehicle component : vehicleComponents.get(packName)){
    			itemList.add(component.item);
    		}
    		for(PackComponentPart component : partComponents.get(packName)){
    			itemList.add(component.item);
    		}
    		for(PackComponentInstrument component : instrumentComponents.get(packName)){
    			itemList.add(component.item);
    		}
    		for(PackComponentDecor component : decorComponents.get(packName)){
    			itemList.add(component.item);
    		}
    		for(PackComponentItem component : itemComponents.get(packName)){
    			itemList.add(component.item);
    		}
    	}
    	return itemList;
    }
        
    private static List<PackComponentVehicle> parseVehicleJSON(String packID, String jsonFileName, InputStreamReader jsonReader){
    	List<PackComponentVehicle> packs = new ArrayList<PackComponentVehicle>();
    	PackObjectVehicle pack = new Gson().fromJson(jsonReader, PackObjectVehicle.class);
		//Need to add a pack definition for each definition.
		for(PackFileDefinitions definition : pack.definitions){
			if(definition != null){
				packs.add(new PackComponentVehicle(packID, jsonFileName + definition.subName, new ItemVehicle(), pack));
			}
		}
		return packs;
    }
    
    private static PackComponentPart parsePartJSON(String packID, String jsonFileName, InputStreamReader jsonReader){
    	PackObjectPart pack =  new Gson().fromJson(jsonReader, PackObjectPart.class);	    	
    	AItemPart item;
		switch(pack.general.type){
			case "crate": item = new ItemPartCrate(); break;
			case "barrel": item = new ItemPartBarrel(); break;
			case "crafting_table": item = new ItemPartGeneric(); break;
			case "furnace": item =  new ItemPartGeneric(); break;
			case "brewing_stand": item = new ItemPartGeneric(); break;
			case "engine_aircraft": item = new ItemPartEngineAircraft(); break;
			case "engine_jet": item = new ItemPartEngineJet(); break;
			case "engine_car": item = new ItemPartEngineCar(); break;
			case "engine_boat": item = new ItemPartEngineBoat(); break;
			case "wheel": item = new ItemPartGroundDeviceWheel(); break;
			case "skid": item = new ItemPartGroundDeviceSkid(); break;
			case "pontoon": item = new ItemPartGroundDevicePontoon(); break;
			case "tread": item = new ItemPartGroundDeviceTread(); break;
			case "propeller": item = new ItemPartPropeller(); break;
			case "seat": item = new ItemPartGeneric(); break;
			case "gun_fixed": item = new ItemPartGun(); break;
			case "gun_tripod": item = new ItemPartGun(); break;
			case "bullet": item = new ItemPartBullet(); break;
			case "custom": item = new ItemPartCustom(); break;
			default: item = null;
		}
		return new PackComponentPart(packID, jsonFileName, item, pack);
    }
    
    private static PackComponentInstrument parseInstrumentJSON(String packID, String jsonFileName, InputStreamReader jsonReader){
	    PackObjectInstrument pack =  new Gson().fromJson(jsonReader, PackObjectInstrument.class);
    	return new PackComponentInstrument(packID, jsonFileName, new ItemInstrument(), pack);
    }
    
    private static PackComponentSign parseSignJSON(String packID, String jsonFileName, InputStreamReader jsonReader){
    	PackObjectSign pack =  new Gson().fromJson(jsonReader, PackObjectSign.class);
		return new PackComponentSign(packID, jsonFileName, null, pack);
    }
    
    private static PackComponentDecor parseDecorJSON(String packID, String jsonFileName, InputStreamReader jsonReader){
    	PackObjectDecor pack =  new Gson().fromJson(jsonReader, PackObjectDecor.class);
		return new PackComponentDecor(packID, jsonFileName, new ItemDecor(), pack);
    }
    
    private static PackComponentItem parseItemJSON(String packID, String jsonFileName, InputStreamReader jsonReader){
		PackObjectItem pack =  new Gson().fromJson(jsonReader, PackObjectItem.class);
		return new PackComponentItem(packID, jsonFileName, new ItemItem(), pack);
    }
    
    public static PackComponentVehicle getVehicleComponentByName(String packID, String name){
    	for(PackComponentVehicle component : vehicleComponents.get(packID)){
    		if(component.name.equals(name)){
    			return component;
    		}
    	}
    	return null;
    }
    
    
    public static PackComponentPart getPartComponentByName(String packID, String name){
    	for(PackComponentPart component : partComponents.get(packID)){
    		if(component.name.equals(name)){
    			return component;
    		}
    	}
    	return null;
    }
    
    public static PackComponentInstrument getInstrumentComponentByName(String packID, String name){
    	for(PackComponentInstrument component : instrumentComponents.get(packID)){
    		if(component.name.equals(name)){
    			return component;
    		}
    	}
    	return null;
    }
}
