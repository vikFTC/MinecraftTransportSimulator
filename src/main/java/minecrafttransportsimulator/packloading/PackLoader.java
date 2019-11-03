package minecrafttransportsimulator.packloading;

import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.gson.Gson;

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
import minecrafttransportsimulator.packloading.PackVehicleObject.PackFileDefinitions;
import minecrafttransportsimulator.vehicles.main.EntityVehicleG_Blimp;
import minecrafttransportsimulator.vehicles.main.EntityVehicleG_Boat;
import minecrafttransportsimulator.vehicles.main.EntityVehicleG_Car;
import minecrafttransportsimulator.vehicles.main.EntityVehicleG_Plane;
import minecrafttransportsimulator.vehicles.parts.PartBarrel;
import minecrafttransportsimulator.vehicles.parts.PartBrewingStand;
import minecrafttransportsimulator.vehicles.parts.PartCraftingTable;
import minecrafttransportsimulator.vehicles.parts.PartCrate;
import minecrafttransportsimulator.vehicles.parts.PartCustom;
import minecrafttransportsimulator.vehicles.parts.PartEngineAircraft;
import minecrafttransportsimulator.vehicles.parts.PartEngineBoat;
import minecrafttransportsimulator.vehicles.parts.PartEngineCar;
import minecrafttransportsimulator.vehicles.parts.PartEngineJet;
import minecrafttransportsimulator.vehicles.parts.PartFurnace;
import minecrafttransportsimulator.vehicles.parts.PartGroundDevicePontoon;
import minecrafttransportsimulator.vehicles.parts.PartGroundDeviceSkid;
import minecrafttransportsimulator.vehicles.parts.PartGroundDeviceTread;
import minecrafttransportsimulator.vehicles.parts.PartGroundDeviceWheel;
import minecrafttransportsimulator.vehicles.parts.PartGunFixed;
import minecrafttransportsimulator.vehicles.parts.PartGunTripod;
import minecrafttransportsimulator.vehicles.parts.PartPropeller;
import minecrafttransportsimulator.vehicles.parts.PartSeat;
import mts_to_mc.interfaces.FileInterface;
import mts_to_mc.interfaces.ItemInterface;
import net.minecraft.item.ItemStack;

/**
 * Class responsible for loading packs from the mod folder and parsing them out into their respective systems.
 * Contains maps with data about all packs found and loaded.  Use the info contained in the maps to switch
 * between the String, Item, and operational forms of the pack objects.
 *
 * @author don_bruce
 */
public final class PackLoader{
	//Lists containing pack objects that have been loaded.
    public static final Map<PackComponent, PackVehicleObject> vehiclePackMap = new LinkedHashMap<PackComponent, PackVehicleObject>();
    public static final Map<PackComponent, PackPartObject> partPackMap = new LinkedHashMap<PackComponent, PackPartObject>();
    public static final Map<PackComponent, PackInstrumentObject> instrumentPackMap = new LinkedHashMap<PackComponent, PackInstrumentObject>();
    public static final Map<PackComponent, PackSignObject> signPackMap = new LinkedHashMap<PackComponent, PackSignObject>();
    public static final Map<PackComponent, PackDecorObject> decorPackMap = new LinkedHashMap<PackComponent, PackDecorObject>();
    public static final Map<PackComponent, PackItemObject> itemPackMap = new LinkedHashMap<PackComponent, PackItemObject>();
    
    //List of all pack components with items.  Used for iterating through pack item lists.
    public static final List<PackComponent> packComponents = new ArrayList<PackComponent>();
    
    /**This should be called before any pack operations are performed.
     * This will get any packs present in the mod directory and load them into the game.
     * Only goes two deep.  One will get to the "mods" directory, the other will get
     * into any version-specific directories.**/
    public static void init(){
    	for(File file : FileInterface.getModDirectory().listFiles()){
			if(file.getName().endsWith(".jar")){
				try{
					PackInfoObject packObject = null;
					ZipFile jarFile = new ZipFile(file);
					
					Enumeration<? extends ZipEntry> entries = jarFile.entries();
					while(entries.hasMoreElements()){
						ZipEntry entry = entries.nextElement();
						if(entry.getName().endsWith("MTSPack.info")){
							packObject = new Gson().fromJson(new InputStreamReader(jarFile.getInputStream(entry)), PackInfoObject.class);
							break;
						}
					}
					
					if(packObject != null){
						entries = jarFile.entries();
						while(entries.hasMoreElements()){
							ZipEntry entry = entries.nextElement();
							String entryName = entry.getName();
							if(entryName.endsWith(".json") && entryName.contains("jsondefs")){
								String entryFileName = entryName.substring(entryName.lastIndexOf('/') + 1, entryName.length() - ".json".length());
								InputStreamReader reader = new InputStreamReader(jarFile.getInputStream(entry));
								if(entryName.contains("/vehicles/")){
									addVehicleDefinition(reader, entryFileName, packObject.packID);
								}else if(entryName.contains("/parts/")){
									addPartDefinition(reader, entryFileName, packObject.packID);
								}else if(entryName.contains("/instruments/")){
									addInstrumentDefinition(reader, entryFileName, packObject.packID);
								}else if(entryName.contains("/signs/")){
									addSignDefinition(reader, entryFileName, packObject.packID);
								}else if(entryName.contains("/decors/")){
									addDecorDefinition(reader, entryFileName, packObject.packID);
								}else if(entryName.contains("/items/")){
									addItemDefinition(reader, entryFileName, packObject.packID);
								}else{
									FileInterface.logError("ERROR: No valid pack component found for " + entryFileName + " Skipping!");
								}
							}
						}
						
					}
					jarFile.close();
				}catch(Exception e){
					FileInterface.logError("ERROR: Could not complete parsing of: " + file.getName());
					FileInterface.logError(e.getMessage());
				}
			}
    	}
    }
        
    private static void addVehicleDefinition(InputStreamReader jsonReader, String jsonFileName, String packID){
    	try{
    		PackVehicleObject pack = new Gson().fromJson(jsonReader, PackVehicleObject.class);
    		//Need to add a pack definition for each definition.
    		for(PackFileDefinitions definition : pack.definitions){
    			if(definition != null){
    				String vehicleName = jsonFileName + ":" + definition.subName;
    				ItemVehicle item = new ItemVehicle();
    				List<String> materials = new ArrayList<String>();
    				for(String material : pack.general.materials){
    					materials.add(material);
    				}
    				for(String material : definition.extraMaterials){
    					materials.add(material);
    				}
    				Class spawningClass;
    				switch(pack.general.type){
	    				case "plane": spawningClass = EntityVehicleG_Plane.class; break;
	    				case "car": spawningClass = EntityVehicleG_Car.class; break;
	    				case "blimp": spawningClass = EntityVehicleG_Blimp.class; break;
	    				case "boat": spawningClass = EntityVehicleG_Boat.class; break;
	    				default: spawningClass = null;
    				}
    				vehiclePackMap.put(new PackComponent(packID, vehicleName, item, materials.toArray(new String[]{}), spawningClass), pack);
    			}
    		}
    	}catch(Exception e){
    		FileInterface.logError("AN ERROR WAS ENCOUNTERED WHEN TRY TO PARSE: " + packID + ":" + jsonFileName);
    		FileInterface.logError(e.getMessage());
    	}
    }
    
    private static void addPartDefinition(InputStreamReader jsonReader, String jsonFileName, String packID){
    	try{
	    	PackPartObject pack =  new Gson().fromJson(jsonReader, PackPartObject.class);	    	
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
			
			
	    	Class spawningClass;
			switch(pack.general.type){
				case "crate": spawningClass = PartCrate.class; break;
				case "barrel": spawningClass = PartBarrel.class; break;
				case "crafting_table": spawningClass = PartCraftingTable.class; break;
				case "furnace": spawningClass = PartFurnace.class; break;
				case "brewing_stand": spawningClass = PartBrewingStand.class; break;
				case "engine_aircraft": spawningClass = PartEngineAircraft.class; break;
				case "engine_jet": spawningClass = PartEngineJet.class; break;
				case "engine_car": spawningClass = PartEngineCar.class; break;
				case "engine_boat": spawningClass = PartEngineBoat.class; break;
				case "wheel": spawningClass = PartGroundDeviceWheel.class; break;
				case "skid": spawningClass = PartGroundDeviceSkid.class; break;
				case "pontoon": spawningClass = PartGroundDevicePontoon.class; break;
				case "tread": spawningClass = PartGroundDeviceTread.class; break;
				case "propeller": spawningClass = PartPropeller.class; break;
				case "seat": spawningClass = PartSeat.class; break;
				case "gun_fixed": spawningClass = PartGunFixed.class; break;
				case "gun_tripod": spawningClass = PartGunTripod.class; break;
				//Note that this case is invalid, as bullets are NOT parts that can be placed on vehicles.
				//Rather, they are items that get loaded into the gun, so they never actually become parts themselves.
				//case "bullet": return PartBullet.class;
				case "custom": spawningClass = PartCustom.class; break;
				default: spawningClass = null;
			}
			partPackMap.put(new PackComponent(packID, jsonFileName, item, pack.general.materials, spawningClass), pack);
    	}catch(Exception e){
    		FileInterface.logError("AN ERROR WAS ENCOUNTERED WHEN TRY TO PARSE: " + packID + ":" + jsonFileName);
    		FileInterface.logError(e.getMessage());
    	}
    }
    
    private static void addInstrumentDefinition(InputStreamReader jsonReader, String jsonFileName, String packID){
    	try{
	    	PackInstrumentObject pack =  new Gson().fromJson(jsonReader, PackInstrumentObject.class);
	    	ItemInstrument item = new ItemInstrument();
    		instrumentPackMap.put(new PackComponent(packID, jsonFileName, item, pack.general.materials, null), pack);
    	}catch(Exception e){
    		FileInterface.logError("AN ERROR WAS ENCOUNTERED WHEN TRY TO PARSE: " + packID + ":" + jsonFileName);
    		FileInterface.logError(e.getMessage());
    	}
    }
    
    private static void addSignDefinition(InputStreamReader jsonReader, String jsonFileName, String packID){
    	try{
	    	PackSignObject pack =  new Gson().fromJson(jsonReader, PackSignObject.class);
    		signPackMap.put(new PackComponent(packID, jsonFileName, null, null, null), pack);
    	}catch(Exception e){
    		FileInterface.logError("AN ERROR WAS ENCOUNTERED WHEN TRY TO PARSE: " + packID + ":" + jsonFileName);
    		FileInterface.logError(e.getMessage());
    	}
    }
    
    private static void addDecorDefinition(InputStreamReader jsonReader, String jsonFileName, String packID){
    	try{
	    	PackDecorObject pack =  new Gson().fromJson(jsonReader, PackDecorObject.class);
    		ItemDecor item = new ItemDecor();
    		decorPackMap.put(new PackComponent(packID, jsonFileName, item, pack.general.materials, null), pack);
    	}catch(Exception e){
    		FileInterface.logError("AN ERROR WAS ENCOUNTERED WHEN TRY TO PARSE: " + packID + ":" + jsonFileName);
    		FileInterface.logError(e.getMessage());
    	}
    }
    
    private static void addItemDefinition(InputStreamReader jsonReader, String jsonFileName, String packID){
    	try{
    		PackItemObject pack =  new Gson().fromJson(jsonReader, PackItemObject.class);
	    	ItemItem item = new ItemItem();
    		itemPackMap.put(new PackComponent(packID, jsonFileName, item, pack.general.materials, null), pack);
    	}catch(Exception e){
    		FileInterface.logError("AN ERROR WAS ENCOUNTERED WHEN TRY TO PARSE: " + packID + ":" + jsonFileName);
    		FileInterface.logError(e.getMessage());
    	}
    }
    
    
    //-----START OF GENERAL LOOKUP LOGIC-----
    public static List<ItemStack> getMaterialsForComponent(PackComponent component){
    	final List<ItemStack> materialList = new ArrayList<ItemStack>();
		for(String itemText : component.craftingMaterials){
			int itemQty = Integer.valueOf(itemText.substring(itemText.lastIndexOf(':') + 1));
			itemText = itemText.substring(0, itemText.lastIndexOf(':'));
			
			int itemMetadata = Integer.valueOf(itemText.substring(itemText.lastIndexOf(':') + 1));
			itemText = itemText.substring(0, itemText.lastIndexOf(':'));
			materialList.add(ItemInterface.getItemStackByName(itemText, itemQty, itemMetadata));
		}
    	return materialList;
    }
}
