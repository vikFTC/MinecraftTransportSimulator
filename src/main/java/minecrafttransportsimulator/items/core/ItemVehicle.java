package minecrafttransportsimulator.items.core;

import minecrafttransportsimulator.baseclasses.VehicleAxisAlignedBB;
import minecrafttransportsimulator.packs.PackLoader;
import minecrafttransportsimulator.packs.components.PackComponentInstrument;
import minecrafttransportsimulator.packs.components.PackComponentPart;
import minecrafttransportsimulator.packs.components.PackComponentVehicle;
import minecrafttransportsimulator.packs.objects.PackObjectVehicle.PackCollisionBox;
import minecrafttransportsimulator.packs.objects.PackObjectVehicle.PackPart;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import minecrafttransportsimulator.vehicles.parts.APart;
import mts_to_mc.interfaces.FileInterface;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemVehicle extends AItemPackComponent<PackComponentVehicle>{
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		if(!world.isRemote && player.getHeldItem(hand) != null){
			ItemStack heldStack = player.getHeldItem(hand);
			if(heldStack.getItem() != null){
				//We want to spawn above this block.
				pos = pos.up();
				try{
					//First construct the class.
					EntityVehicleE_Powered newVehicle = packComponent.createVehicle(world, pos.getX(), pos.getY(), pos.getZ(), player.rotationYaw, packComponent);
					
					//Now that the class exists, use the NTB data from this item to add back components.
					if(heldStack.hasTagCompound()){
						NBTTagCompound tagCompound = heldStack.getTagCompound();
						//A-level
						NBTTagList partTagList = tagCompound.getTagList("Parts", 10);
						for(byte i=0; i<partTagList.tagCount(); ++i){
							try{
								NBTTagCompound partTag = partTagList.getCompoundTagAt(i);
								PackPart packPart = newVehicle.getPackDefForLocation(partTag.getDouble("offsetX"), partTag.getDouble("offsetY"), partTag.getDouble("offsetZ"));
								PackComponentPart partComponent = PackLoader.getPartComponentByName(tagCompound.getString("partPack"), tagCompound.getString("partName"));
								APart savedPart = partComponent.createPart(newVehicle, partComponent, packPart, partTag);
								newVehicle.addPart(savedPart, true);
							}catch(Exception e){
								FileInterface.logError("ERROR IN LOADING PART FROM NBT!");
								FileInterface.logError(e.getMessage());
							}
						}
						
						//B-level
						newVehicle.locked=tagCompound.getBoolean("locked");
						newVehicle.brokenWindows=tagCompound.getByte("brokenWindows");
						newVehicle.ownerName=tagCompound.getString("ownerName");
						newVehicle.displayText=tagCompound.getString("displayText");
						
						//C-level
						
						//D-level
						newVehicle.parkingBrakeOn=tagCompound.getBoolean("parkingBrakeOn");
						
						//E-level
						newVehicle.fuel=tagCompound.getDouble("fuel");
						newVehicle.electricPower=tagCompound.getDouble("electricPower");
						for(byte i = 0; i<newVehicle.packComponent.pack.motorized.instruments.size(); ++i){
							if(tagCompound.hasKey("instrumentInSlot" + i)){
								if(tagCompound.hasKey("instrumentInSlot" + i)){
									PackComponentInstrument instrument = PackLoader.getInstrumentComponentByName(tagCompound.getString("instrumentInSlot" + i + "_pack"), tagCompound.getString("instrumentInSlot" + i + "_name"));
									//Check to prevent loading of faulty instruments for the wrong vehicle due to updates.
									if(instrument != null && instrument.pack.general.validVehicles.contains(packComponent.pack.general.type)){
										newVehicle.instruments.set(i, instrument);
									}
								}
							}
						}
					}
					
					//Get how far above the ground the vehicle needs to be, and move it to that position.
					//First boost Y based on collision boxes.
					double minHeight = 0;
					for(PackCollisionBox collisionBox : newVehicle.packComponent.pack.collision){
						minHeight = Math.min(collisionBox.pos[1] - collisionBox.height/2F, minHeight);
					}
					
					//Next, boost based on parts.
					for(APart part : newVehicle.parts){
						minHeight = Math.min(part.currentOffset.x - part.getHeight()/2F, minHeight);
					}
					
					//Apply the boost, and check collisions.
					//If the core collisions are colliding, set the vehicle as dead and abort.
					newVehicle.posY += -minHeight;
					for(VehicleAxisAlignedBB coreBox : newVehicle.getCurrentCollisionBoxes()){
						if(world.collidesWithAnyBlock(coreBox)){
							newVehicle.setDead();
							return EnumActionResult.FAIL;
						}
					}
					
					//If we didn't collide with anything, let the vehicle remain in the world.
					world.spawnEntity(newVehicle);
					if(!player.capabilities.isCreativeMode){
						player.inventory.clearMatchingItems(heldStack.getItem(), heldStack.getItemDamage(), 1, heldStack.getTagCompound());
					}
				}catch(Exception e){
					FileInterface.logError("ERROR SPAWING VEHICLE ENTITY!");
					FileInterface.logError(e.getMessage());
				}
			}
		}
		return EnumActionResult.SUCCESS;
	}
}
