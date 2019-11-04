package minecrafttransportsimulator.packs.components;

import minecrafttransportsimulator.items.core.AItemPackComponent;
import minecrafttransportsimulator.packs.objects.PackObjectPart;
import minecrafttransportsimulator.packs.objects.PackObjectVehicle.PackPart;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import minecrafttransportsimulator.vehicles.parts.APart;
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
import net.minecraft.nbt.NBTTagCompound;

public class PackComponentPart extends APackComponent<PackObjectPart>{
	
	public PackComponentPart(String packID, String name, AItemPackComponent item, PackObjectPart pack){
		super(packID, name, item, pack);
	}

	@Override
	public String[] getCraftingMaterials(){
		return pack.general.materials;
	}
	
	public APart createPart(EntityVehicleE_Powered vehicle, PackComponentPart packComponent, PackPart vehicleDefinition, NBTTagCompound dataTag){
		switch(pack.general.type){
			case "crate": return new PartCrate(vehicle, packComponent, vehicleDefinition, dataTag);
			case "barrel": return new PartBarrel(vehicle, packComponent, vehicleDefinition, dataTag);
			case "crafting_table": return new PartCraftingTable(vehicle, packComponent, vehicleDefinition, dataTag);
			case "furnace": return new PartFurnace(vehicle, packComponent, vehicleDefinition, dataTag);
			case "brewing_stand": return new PartBrewingStand(vehicle, packComponent, vehicleDefinition, dataTag);
			case "engine_aircraft": return new PartEngineAircraft(vehicle, packComponent, vehicleDefinition, dataTag);
			case "engine_jet": return new PartEngineJet(vehicle, packComponent, vehicleDefinition, dataTag);
			case "engine_car": return new PartEngineCar(vehicle, packComponent, vehicleDefinition, dataTag);
			case "engine_boat": return new PartEngineBoat(vehicle, packComponent, vehicleDefinition, dataTag);
			case "wheel": return new PartGroundDeviceWheel(vehicle, packComponent, vehicleDefinition, dataTag);
			case "skid": return new PartGroundDeviceSkid(vehicle, packComponent, vehicleDefinition, dataTag);
			case "pontoon": return new PartGroundDevicePontoon(vehicle, packComponent, vehicleDefinition, dataTag);
			case "tread": return new PartGroundDeviceTread(vehicle, packComponent, vehicleDefinition, dataTag);
			case "propeller": return new PartPropeller(vehicle, packComponent, vehicleDefinition, dataTag);
			case "seat": return new PartSeat(vehicle, packComponent, vehicleDefinition, dataTag);
			case "gun_fixed": return new PartGunFixed(vehicle, packComponent, vehicleDefinition, dataTag);
			case "gun_tripod": return new PartGunTripod(vehicle, packComponent, vehicleDefinition, dataTag);
			//Note that this case is invalid, as bullets are NOT parts that can be placed on vehicles.
			//Rather, they are items that get loaded into the gun, so they never actually become parts themselves.
			//case "bullet": return new PartBullet(vehicle, packComponent, vehicleDefinition, dataTag);
			case "custom": return new PartCustom(vehicle, packComponent, vehicleDefinition, dataTag);
			default: return null;
		}
	}
}