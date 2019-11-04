package minecrafttransportsimulator.vehicles.parts;

import minecrafttransportsimulator.packs.components.PackComponentPart;
import minecrafttransportsimulator.packs.objects.PackObjectVehicle.PackPart;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import net.minecraft.nbt.NBTTagCompound;

public final class PartCustom extends APart{
	
	public PartCustom(EntityVehicleE_Powered vehicle, PackComponentPart packComponent, PackPart vehicleDefinition, NBTTagCompound dataTag){
		super(vehicle, packComponent, vehicleDefinition, dataTag);
	}
	
	@Override
	public NBTTagCompound getPartNBTTag(){
		return new NBTTagCompound();
	}
	
	@Override
	public float getWidth(){
		return packComponent.pack.custom.width;
	}

	@Override
	public float getHeight(){
		return packComponent.pack.custom.height;
	}
}
