package minecrafttransportsimulator.packs.components;

import java.util.ArrayList;
import java.util.List;

import minecrafttransportsimulator.items.core.AItemPackComponent;
import minecrafttransportsimulator.packs.objects.PackObjectVehicle;
import minecrafttransportsimulator.packs.objects.PackObjectVehicle.PackFileDefinitions;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import minecrafttransportsimulator.vehicles.main.EntityVehicleG_Blimp;
import minecrafttransportsimulator.vehicles.main.EntityVehicleG_Boat;
import minecrafttransportsimulator.vehicles.main.EntityVehicleG_Car;
import minecrafttransportsimulator.vehicles.main.EntityVehicleG_Plane;
import net.minecraft.world.World;

public class PackComponentVehicle extends APackComponent<PackObjectVehicle>{
	
	public PackComponentVehicle(String packID, String name, AItemPackComponent item, PackObjectVehicle pack){
		super(packID, name, item, pack);
	}
	
	@Override
	public String getTranslatedName(){
		for(PackFileDefinitions definition : pack.definitions){
			if(name.endsWith(definition.subName)){
				return definition.name;
			}
		}
		return "INVALID VEHICLE?";
	}
	
	@Override
	public String[] getCraftingMaterials(){
		List<String> materials = new ArrayList<String>();
		for(String material : pack.general.materials){
			materials.add(material);
		}
		for(PackFileDefinitions definition : pack.definitions){
			if(name.endsWith(definition.subName)){
				for(String material : definition.extraMaterials){
					materials.add(material);
				}
				break;
			}
		}
		return materials.toArray(new String[]{});
	}
	
	public EntityVehicleE_Powered createVehicle(World world, float posX, float posY, float posZ, float rotation, PackComponentVehicle packComponent){
		switch(pack.general.type){
			case "plane": return new EntityVehicleG_Plane(world, posX, posY, posZ, rotation, packComponent);
			case "car": return new EntityVehicleG_Car(world, posX, posY, posZ, rotation, packComponent);
			case "blimp": return new EntityVehicleG_Blimp(world, posX, posY, posZ, rotation, packComponent);
			case "boat": return new EntityVehicleG_Boat(world, posX, posY, posZ, rotation, packComponent);
			default: return null;
		}
	}
}