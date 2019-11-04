package minecrafttransportsimulator.vehicles.parts;

import minecrafttransportsimulator.packs.components.PackComponentPart;
import minecrafttransportsimulator.packs.objects.PackObjectVehicle.PackPart;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class PartEngineBoat extends APartEngine{

	public PartEngineBoat(EntityVehicleE_Powered vehicle, PackComponentPart packComponent, PackPart vehicleDefinition, NBTTagCompound dataTag){
		super(vehicle, packComponent, vehicleDefinition, dataTag);
	}
	
	@Override
	public void updatePart(){
		super.updatePart();
		//Boat engines are similar to airplane engines, except the propellers are built-in and are set to 25 degree pitches.
		//This pitch is only used when the engine is turned off and not providing power, so it's not really critical.
		//Gear ratio is assumed to be 1, as it'll be a straight-shaft connection.
		if(state.running){
			double engineTargetRPM = vehicle.throttle/100F*(packComponent.pack.engine.maxRPM - engineStartRPM*1.25 - hours) + engineStartRPM*1.25;
			
			//Check 1 block down for liquid.  If we are in liquid, then we should provide power.
			if(vehicle.world.getBlockState(new BlockPos(currentPosition).down()).getMaterial().isLiquid()){
				//PropellerFeedback can't make an engine stall, but hours can.
				RPM += (engineTargetRPM - engineStartRPM*0.15F - RPM)/10;
			}else{
				RPM += (engineTargetRPM - RPM)/10;
			}
		}else{
			RPM = Math.max(RPM + (vehicle.velocity - 0.0254*25*RPM/60/20)*15 - 10, 0);
		}
		
		engineRotationLast = engineRotation;
		engineRotation += 360D*RPM/1200D;
		engineDriveshaftRotationLast = engineDriveshaftRotation;
		engineDriveshaftRotation += 360D*RPM/1200D;
	}
	
	@Override
	public double getForceOutput(){
		return state.running ? RPM/packComponent.pack.engine.maxRPM*5*packComponent.pack.engine.fuelConsumption : 0;
	}
}
