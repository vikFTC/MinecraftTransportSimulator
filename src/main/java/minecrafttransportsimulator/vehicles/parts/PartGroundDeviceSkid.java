package minecrafttransportsimulator.vehicles.parts;

import minecrafttransportsimulator.packs.components.PackComponentPart;
import minecrafttransportsimulator.packs.objects.PackObjectVehicle.PackPart;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import net.minecraft.nbt.NBTTagCompound;

public final class PartGroundDeviceSkid extends APartGroundDevice{
	public PartGroundDeviceSkid(EntityVehicleE_Powered vehicle, PackComponentPart packComponent, PackPart vehicleDefinition, NBTTagCompound dataTag){
		super(vehicle, packComponent, vehicleDefinition, dataTag);
	}
	
	@Override
	public NBTTagCompound getPartNBTTag(){
		return new NBTTagCompound();
	}
	
	@Override
	public float getWidth(){
		return this.packComponent.pack.skid.width;
	}
	
	@Override
	public float getHeight(){
		return this.getWidth();
	}
	
	@Override
	public float getMotiveFriction(){
		return 0;
	}
	
	@Override
	public float getLateralFriction(){
		return this.packComponent.pack.skid.lateralFriction;
	}
	
	@Override
	public float getLongPartOffset(){
		return 0;
	}
	
	@Override
	public boolean canBeDrivenByEngine(){
		return false;
	}
}
