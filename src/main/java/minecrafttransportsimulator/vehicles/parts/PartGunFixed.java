package minecrafttransportsimulator.vehicles.parts;

import minecrafttransportsimulator.packs.components.PackComponentPart;
import minecrafttransportsimulator.packs.objects.PackObjectVehicle.PackPart;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import net.minecraft.nbt.NBTTagCompound;

public class PartGunFixed extends APartGun{	
		
	public PartGunFixed(EntityVehicleE_Powered vehicle, PackComponentPart packComponent, PackPart vehicleDefinition, NBTTagCompound dataTag){
		super(vehicle, packComponent, vehicleDefinition, dataTag);
	}
	
	@Override
	public float getMinYaw(){
		return 0;
	}
	
	@Override
	public float getMaxYaw(){
		return 0;
	}
	
	@Override
	public float getMinPitch(){
		return 0;
	}
	
	@Override
	public float getMaxPitch(){
		return 0;
	}
}
