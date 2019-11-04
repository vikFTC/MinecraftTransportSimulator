package minecrafttransportsimulator.items.parts;

import java.util.Map.Entry;

import minecrafttransportsimulator.MTS;
import minecrafttransportsimulator.baseclasses.VehicleAxisAlignedBB;
import minecrafttransportsimulator.items.core.AItemPackComponent;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleClientPartAddition;
import minecrafttransportsimulator.packs.components.PackComponentPart;
import minecrafttransportsimulator.packs.objects.PackObjectVehicle.PackPart;
import minecrafttransportsimulator.systems.RotationSystem;
import minecrafttransportsimulator.vehicles.main.EntityVehicleC_Colliding;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import minecrafttransportsimulator.vehicles.parts.APart;
import mts_to_mc.interfaces.FileInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class AItemPart extends AItemPackComponent<PackComponentPart>{
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand){
        //Only try to add parts on the server and send packets to the client if we are successful.
    	if(!world.isRemote){
	    	for(Entity entity : world.loadedEntityList){
				if(entity instanceof EntityVehicleC_Colliding){
					EntityVehicleC_Colliding vehicle = (EntityVehicleC_Colliding) entity;
					//If we are riding this vehicle, we cannot add parts to it.
					if(!vehicle.equals(player.getRidingEntity())){
						//If this item is a part, find if we are right-clicking a valid part area.
						//If so, add the part and notify all clients.
			    		Vec3d lookVec = player.getLook(1.0F);
        				Vec3d clickedVec = player.getPositionVector().addVector(0, entity.getEyeHeight(), 0);
			    		for(float f=1.0F; f<4.0F; f += 0.1F){
			    			for(Entry<Vec3d, PackPart> packPartEntry : vehicle.getAllPossiblePackParts().entrySet()){
		    					//If we are a custom part, use the custom hitbox.  Otherwise use the regular one.
		    					VehicleAxisAlignedBB partBox;
								if(packPartEntry.getValue().types.contains("custom") && packComponent.pack.general.type.equals("custom")){
									Vec3d offset = RotationSystem.getRotatedPoint(packPartEntry.getKey(), vehicle.rotationPitch, vehicle.rotationYaw, vehicle.rotationRoll);
									partBox = new VehicleAxisAlignedBB(vehicle.getPositionVector().add(offset), packPartEntry.getKey(), packComponent.pack.custom.width, packComponent.pack.custom.height, false, false);		
								}else{
									Vec3d offset = RotationSystem.getRotatedPoint(packPartEntry.getKey().addVector(0, 0.25F, 0), vehicle.rotationPitch, vehicle.rotationYaw, vehicle.rotationRoll);
									partBox = new VehicleAxisAlignedBB(vehicle.getPositionVector().add(offset), packPartEntry.getKey().addVector(0, 0.5F, 0), 0.75F, 1.75F, false, false);
								}
		    					
		    					if(partBox.contains(clickedVec)){
		    						//Check to make sure the spot is free.
		    						if(vehicle.getPartAtLocation(packPartEntry.getKey().x, packPartEntry.getKey().y, packPartEntry.getKey().z) == null){
		    							//Check to make sure the part is valid.
		    							if(packPartEntry.getValue().types.contains(packComponent.pack.general.type)){
		    								//Check to make sure the part is in parameter ranges.
		    								if(isPartValidForPackDef(packPartEntry.getValue())){
		        								//Try to add the part.
		    									try{
		    										ItemStack heldStack = player.getHeldItem(hand);
		    										APart newPart = packComponent.createPart((EntityVehicleE_Powered) vehicle, packComponent, packPartEntry.getValue(), heldStack.hasTagCompound() ? heldStack.getTagCompound() : new NBTTagCompound());
		    										vehicle.addPart(newPart, false);
		    										MTS.MTSNet.sendToAll(new PacketVehicleClientPartAddition(vehicle, packPartEntry.getKey().x, packPartEntry.getKey().y, packPartEntry.getKey().z, heldStack));
		    										if(!player.capabilities.isCreativeMode){
		    											player.inventory.clearMatchingItems(this, heldStack.getItemDamage(), 1, heldStack.getTagCompound());
		    										}
		    									}catch(Exception e){
		    										FileInterface.logError("ERROR SPAWING PART ON SERVER!");
		    										FileInterface.logError(e.getMessage());
		    									}
		        							}
		    							}
		    						}
	        					}
		    				}
        					clickedVec = clickedVec.addVector(lookVec.x*0.1F, lookVec.y*0.1F, lookVec.z*0.1F);
        				}
					}
	    		}
	    	}
    	}
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
	
	public boolean isPartValidForPackDef(PackPart packPart){
		if(packPart.customTypes == null){
			return packComponent.pack.general.customType == null;
		}else if(packComponent.pack.general.customType == null){
			return packPart.customTypes == null;
		}else{
			return packPart.customTypes.contains(packComponent.pack.general.customType);
		}
	}
}
