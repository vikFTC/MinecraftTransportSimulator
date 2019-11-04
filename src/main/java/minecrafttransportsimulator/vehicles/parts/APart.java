package minecrafttransportsimulator.vehicles.parts;

import java.util.ArrayList;
import java.util.List;

import minecrafttransportsimulator.baseclasses.VehicleAxisAlignedBB;
import minecrafttransportsimulator.packs.components.PackComponentPart;
import minecrafttransportsimulator.packs.objects.PackObjectVehicle.PackPart;
import minecrafttransportsimulator.systems.RotationSystem;
import minecrafttransportsimulator.vehicles.main.EntityVehicleA_Base;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;

/**This class is the base for all parts and should be
 * extended for any vehicle-compatible parts.
 * Use {@link EntityVehicleA_Base#addPart(APart, boolean)} to add parts 
 * and {@link EntityVehicleA_Base#removePart(APart, boolean)} to remove them.
 * You may extend {@link EntityVehicleA_Base} to get more functionality with those systems.
 * If you need to keep extra data ensure it is packed into whatever NBT is returned in item form.
 * This NBT will be fed into the constructor when creating this part, so expect it and ONLY look for it there.
 * 
 * @author don_bruce
 */
public abstract class APart{	
	//Static variables.  These are set at construction time and don't change.
	public final EntityVehicleE_Powered vehicle;
	public final PackComponentPart packComponent;
	public final PackPart vehicleDefinition;
	/*This is used to locate the part on the vehicle.  This may not be the current offset for some parts.*/
	public final Vec3d baseOffset;
	public final Vec3d baseRotation;
	public final APart parentPart;
	public final List<APart> childParts = new ArrayList<APart>();
	
	//Runtime variables.  Will change depending on part actions.
	/*This is the current offset of the part.  May overlap with other parts!*/
	public Vec3d currentOffset;
	public Vec3d currentRotation;
	public Vec3d currentPosition;
			
	public APart(EntityVehicleE_Powered vehicle, PackComponentPart packComponent, PackPart vehicleDefinition, NBTTagCompound dataTag){
		this.vehicle = vehicle;
		this.packComponent = packComponent;
		this.vehicleDefinition = vehicleDefinition;
		
		this.baseOffset = new Vec3d(vehicleDefinition.pos[0], vehicleDefinition.pos[1], vehicleDefinition.pos[2]);
		this.baseRotation = vehicleDefinition.rot != null ? new Vec3d(vehicleDefinition.rot[0], vehicleDefinition.rot[1], vehicleDefinition.rot[2]) : Vec3d.ZERO; 
		
		this.currentOffset = baseOffset;
		this.currentRotation = baseRotation;
		this.currentPosition = RotationSystem.getRotatedPoint(currentOffset, vehicle.rotationPitch, vehicle.rotationYaw, vehicle.rotationRoll).add(vehicle.getPositionVector()); 

		//Check to see if we are an additional part to a part on our parent.
		for(PackPart parentPackPart : vehicle.packComponent.pack.parts){
			if(vehicleDefinition.equals(parentPackPart.additionalPart)){
				parentPart = vehicle.getPartAtLocation(parentPackPart.pos[0], parentPackPart.pos[1], parentPackPart.pos[2]);
				parentPart.childParts.add(this);
				return;
			}
		}
		
		//If we aren't an additional part, see if we are a sub-part.
		for(APart part : vehicle.parts){
			if(part.packComponent.pack.subParts != null){
				for(PackPart partSubPartPack : part.packComponent.pack.subParts){
					if((float) part.baseOffset.x + partSubPartPack.pos[0] == (float) this.baseOffset.x && (float) part.baseOffset.y + partSubPartPack.pos[1] == (float) this.baseOffset.y && (float) part.baseOffset.z + partSubPartPack.pos[2] == (float) this.baseOffset.z){
						parentPart = part;
						parentPart.childParts.add(this);
						return;
					}
				}
			}
		}
		parentPart = null;
	}

	/**Called when checking if this part can be interacted with.
	 * If a part does interactions it should do so and then return true.
	 * Call this ONLY from the server-side!  The server will handle the
	 * interaction by notifying the client via packet if appropriate.
	 */
	public boolean interactPart(EntityPlayer player){
		return false;
	}
	
	/**Called when the vehicle sees this part being attacked.  Handle damage here.
	 */
	public void attackPart(DamageSource source, float damage){}
	
	/**This gets called every tick by the vehicle after it finishes its update loop.
	 * Use this for reactions that this part can take based on its surroundings if need be.
	 */
	public void updatePart(){
		this.currentPosition = RotationSystem.getRotatedPoint(currentOffset, vehicle.rotationPitch, vehicle.rotationYaw, vehicle.rotationRoll).add(vehicle.getPositionVector());
	}
	
	/**Called when the vehicle removes this part.
	 * Allows for parts to trigger logic that happens when they are removed.
	 * The passed-in list is for additional parts that need to be removed,
	 * such as child parts that are attached to this part.  Do NOT remove those
	 * parts here as it will cause infinite loops.  This method is only to allow
	 * the part to perform actions when it is removed.
	 */
	public void removePart(List<APart> partsToRemove){
		for(APart childPart : childParts){
			childPart.removePart(partsToRemove);
		}
		partsToRemove.addAll(childParts);
		//Remove us from our parent, if we have one.
		if(this.parentPart != null){
			this.parentPart.childParts.remove(this);
		}
	}
	
	/**Return the part data in NBT form.
	 * This is called when removing the part from a vehicle to return an item.
	 * This is also called when saving this part, so ensure EVERYTHING you need to make this
	 * part back into an part again is returned in the NBT of this stack.
	 * This does not include the part offsets, as those are re-calculated every time the part is attached
	 * and are saved separately from the item NBT data in the vehicle.
	 */
	public abstract NBTTagCompound getPartNBTTag();
	
	public abstract float getWidth();
	
	public abstract float getHeight();
	
	/**Gets the item for this part.  If the part should not return an item 
	 * (either due to damage or other reasons) make this method return null.
	 */
	public Item getItemForPart(){
		return packComponent.item;
	}
	
	/**Gets the location of the model for this part. 
	 */
	public String getModelLocation(){
		return "objmodels/parts/" + (packComponent.pack.general.modelName != null ? packComponent.pack.general.modelName : packComponent.name) + ".obj"; 
	}
	
	/**Gets the location of the texture for this part.
	 */
	public String getTextureLocation(){
		return packComponent.pack.general.useVehicleTexture ? "textures/vehicles/" + vehicle.packComponent.name + ".png" : "textures/parts/" + packComponent.name + ".png";
	}
	
	public final VehicleAxisAlignedBB getAABBWithOffset(Vec3d boxOffset){
		return new VehicleAxisAlignedBB(Vec3d.ZERO.equals(boxOffset) ? currentPosition : currentPosition.add(boxOffset), currentOffset, getWidth(), getHeight(), false, false);
	}
	
	/**Gets the rotation vector for rendering.
	 * This comes from the part itself and is only
	 * changed on the client for animation purposes.
	 * Both this and partRotation are used
	 * to determine the final rotation of a part
	 * during rendering.
	 */
	public Vec3d getActionRotation(float partialTicks){
		return Vec3d.ZERO;
	}

	/**Checks to see if this part is collided with any collidable blocks.
	 * Uses a regular Vanilla check, as well as a liquid check for applicable parts.
	 * Can be given an offset vector to check for potential collisions. 
	 */
	public boolean isPartCollidingWithBlocks(Vec3d collisionOffset){
		return !vehicle.world.getCollisionBoxes(null, getAABBWithOffset(collisionOffset)).isEmpty();
    }
}
