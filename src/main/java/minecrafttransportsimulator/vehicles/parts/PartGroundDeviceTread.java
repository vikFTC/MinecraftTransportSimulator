package minecrafttransportsimulator.vehicles.parts;

import minecrafttransportsimulator.packs.components.PackComponentPart;
import minecrafttransportsimulator.packs.objects.PackObjectVehicle.PackPart;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import net.minecraft.nbt.NBTTagCompound;

public final class PartGroundDeviceTread extends APartGroundDevice{
	public PartGroundDeviceTread(EntityVehicleE_Powered vehicle, PackComponentPart packComponent, PackPart vehicleDefinition, NBTTagCompound dataTag){
		super(vehicle, packComponent, vehicleDefinition, dataTag);
	}	
	
	@Override
	public NBTTagCompound getPartNBTTag(){
		return new NBTTagCompound();
	}
	
	@Override
	public float getWidth(){
		return this.packComponent.pack.tread.width;
	}
	
	@Override
	public float getHeight(){
		return this.getWidth();
	}
	
	@Override
	public float getMotiveFriction(){
		return this.packComponent.pack.tread.motiveFriction;
	}
	
	@Override
	public float getLateralFriction(){
		return this.packComponent.pack.tread.lateralFriction;
	}
	
	@Override
	public float getLongPartOffset(){
		return packComponent.pack.tread.extraCollisionBoxOffset;
	}
	
	@Override
	public boolean canBeDrivenByEngine(){
		return true;
	}
}
