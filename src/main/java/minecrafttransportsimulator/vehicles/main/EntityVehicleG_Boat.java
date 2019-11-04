package minecrafttransportsimulator.vehicles.main;

import minecrafttransportsimulator.packs.components.PackComponentVehicle;
import net.minecraft.world.World;


public final class EntityVehicleG_Boat extends EntityVehicleF_Ground{	

	public EntityVehicleG_Boat(World world){
		super(world);
	}
	
	public EntityVehicleG_Boat(World world, float posX, float posY, float posZ, float rotation, PackComponentVehicle packComponent){
		super(world, posX, posY, posZ, rotation, packComponent);
	}
	
	@Override
	protected float getDragCoefficient(){
		return 1.0F;
	}
}