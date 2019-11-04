package minecrafttransportsimulator.vehicles.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minecrafttransportsimulator.MTS;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleClientInit;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleClientPartRemoval;
import minecrafttransportsimulator.packs.PackLoader;
import minecrafttransportsimulator.packs.components.PackComponentPart;
import minecrafttransportsimulator.packs.components.PackComponentVehicle;
import minecrafttransportsimulator.packs.objects.PackObjectVehicle.PackPart;
import minecrafttransportsimulator.vehicles.parts.APart;
import mts_to_mc.interfaces.FileInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**Base vehicle class.  All vehicle entities should extend this class.
 * It is primarily responsible for the adding and removal of parts,
 * as well as dealing with what happens when this part is killed.
 * It is NOT responsible for custom data sets, sounds, or movement.
 * That should be done in sub-classes to keep methods segregated.
 * 
 * @author don_bruce
 */
public abstract class EntityVehicleA_Base extends Entity{
	public PackComponentVehicle packComponent;
	
	public final List<APart> parts = new ArrayList<APart>();

	/**Cooldown byte to prevent packet spam requests during client-side loading of part packs.**/
	private byte clientPackPacketCooldown = 0;
	
	public EntityVehicleA_Base(World world){
		super(world);
	}
	
	public EntityVehicleA_Base(World world, PackComponentVehicle packComponent){
		this(world);
		this.packComponent = packComponent;
	}
	
	@Override
	public void onEntityUpdate(){
		super.onEntityUpdate();
		//We need to get pack data manually if we are on the client-side.
		///Although we could call this in the constructor, Minecraft changes the
		//entity IDs after spawning and that fouls things up.
		if(packComponent == null && world.isRemote){
			if(clientPackPacketCooldown == 0){
				clientPackPacketCooldown = 40;
				MTS.MTSNet.sendToServer(new PacketVehicleClientInit(this));
			}else{
				--clientPackPacketCooldown;
			}
		}
	}
	
    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double posX, double posY, double posZ, float yaw, float pitch, int posRotationIncrements, boolean teleport){
    	//Overridden due to stupid tracker behavior.
    	//Client-side render changes calls put in its place.
    	this.setRenderDistanceWeight(100);
    	this.ignoreFrustumCheck = true;
    }
	
	public void addPart(APart part, boolean ignoreCollision){
		parts.add(part);
		if(!ignoreCollision){
			//Check for collision, and boost if needed.
			if(part.isPartCollidingWithBlocks(Vec3d.ZERO)){
				this.setPositionAndRotation(posX, posY + part.getHeight(), posZ, rotationYaw, rotationPitch);
			}
			
			//Sometimes we need to do this for parts that are deeper into the ground.
			if(part.isPartCollidingWithBlocks(new Vec3d(0, Math.max(0, -part.currentOffset.y) + part.getHeight(), 0))){
				this.setPositionAndRotation(posX, posY +  part.getHeight(), posZ, rotationYaw, rotationPitch);
			}
		}
	}
	
	/**
	 * Removes the specified part from the vehicle.  Calls the parts
	 * remove function to allow it to perform removal logic.  The
	 * passed-in list will be populated with any parts that need
	 * to be removed in addition to the part being removed.
	 * 
	 * Once all parts are populated, remove them and drop the
	 * items they spawn, if any.
	 */
	public void removePart(APart part, boolean dropItem){
		List<APart> partsToRemove = new ArrayList<APart>();
		part.removePart(partsToRemove);
		partsToRemove.add(part);
		if(!world.isRemote && dropItem){
			for(APart removedPart : partsToRemove){
				Item droppedItem = part.getItemForPart();
				if(droppedItem != null){
					ItemStack droppedStack = new ItemStack(droppedItem);
					droppedStack.setTagCompound(part.getPartNBTTag());
					world.spawnEntity(new EntityItem(world, part.currentPosition.x, part.currentPosition.y, part.currentPosition.z, droppedStack));
				}				
				if(!this.isDead){
					MTS.MTSNet.sendToAllTracking(new PacketVehicleClientPartRemoval(this, part.baseOffset.x, part.baseOffset.y, part.baseOffset.z), this);
				}
				parts.remove(removedPart);
			}
		}
	}
	
	/**
	 * Gets the part at the specified location.
	 */
	public APart getPartAtLocation(double offsetX, double offsetY, double offsetZ){
		for(APart part : this.parts){
			if(part.baseOffset.x == offsetX && part.baseOffset.y == offsetY && part.baseOffset.z == offsetZ){
				return part;
			}
		}
		return null;
	}
	
	/**
	 * Gets all possible pack parts.  This includes additional parts on the vehicle
	 * and extra parts of parts on other parts.  Map returned is the position of the
	 * part positions and the part pack information at those positions.
	 * Note that additional parts will not be added if no part is present
	 * in the primary location.
	 */
	public Map<Vec3d, PackPart> getAllPossiblePackParts(){
		Map<Vec3d, PackPart> packParts = new HashMap<Vec3d, PackPart>();
		//First get all the regular part spots.
		for(PackPart packPart : packComponent.pack.parts){
			Vec3d partPos = new Vec3d(packPart.pos[0], packPart.pos[1], packPart.pos[2]);
			packParts.put(partPos, packPart);
			
			//Check to see if we can put an additional part in this location.
			//If a part is present at a location that can have an additional part, we allow it to be placed.
			while(packPart.additionalPart != null){
				boolean foundPart = false;
				for(APart part : this.parts){
					if(part.baseOffset.equals(partPos)){
						partPos = new Vec3d(packPart.additionalPart.pos[0], packPart.additionalPart.pos[1], packPart.additionalPart.pos[2]);
						packPart = packPart.additionalPart;
						packParts.put(partPos, packPart);
						foundPart = true;
						break;
					}
				}
				if(!foundPart){
					break;
				}
			}
		}
		
		//Next get any sub parts on parts that are present.
		for(APart part : this.parts){
			if(part.packComponent.pack.subParts != null){
				PackPart parentPack = getPackDefForLocation(part.baseOffset.x, part.baseOffset.y, part.baseOffset.z);
				for(PackPart extraPackPart : part.packComponent.pack.subParts){
					PackPart correctedPack = getPackForSubPart(parentPack, extraPackPart);
					packParts.put(new Vec3d(correctedPack.pos[0], correctedPack.pos[1], correctedPack.pos[2]), correctedPack);
				}
			}
			
		}
		return packParts;
	}
	
	/**
	 * Gets the pack definition at the specified location.
	 */
	public PackPart getPackDefForLocation(double offsetX, double offsetY, double offsetZ){
		//Check to see if this is a main part.
		for(PackPart packPart : packComponent.pack.parts){
			if(packPart.pos[0] == offsetX && packPart.pos[1] == offsetY && packPart.pos[2] == offsetZ){
				return packPart;
			}
			
			//Not a main part.  Check if this is an additional part.
			while(packPart.additionalPart != null){
				if(packPart.additionalPart.pos[0] == offsetX && packPart.additionalPart.pos[1] == offsetY && packPart.additionalPart.pos[2] == offsetZ){
					return packPart.additionalPart;
				}else{
					packPart = packPart.additionalPart;
				}
			}
		}
		
		//If this is not a main part or an additional part, check the sub-parts.
		for(APart part : this.parts){
			if(part.packComponent.pack.subParts.size() > 0){
				PackPart parentPack = getPackDefForLocation(part.baseOffset.x, part.baseOffset.y, part.baseOffset.z);
				for(PackPart extraPackPart : part.packComponent.pack.subParts){
					PackPart correctedPack = getPackForSubPart(parentPack, extraPackPart);
					if(correctedPack.pos[0] == offsetX && correctedPack.pos[1] == offsetY && correctedPack.pos[2] == offsetZ){
						return correctedPack;
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Returns a PackPart with the correct properties for a SubPart.  This is because
	 * subParts inherit some properties from their parent parts. 
	 */
	private PackPart getPackForSubPart(PackPart parentPack, PackPart subPack){
		PackPart correctPack = this.packComponent.pack.new PackPart();
		correctPack.pos = new float[3];
		correctPack.pos[0] = parentPack.pos[0] + subPack.pos[0];
		correctPack.pos[1] = parentPack.pos[1] + subPack.pos[1];
		correctPack.pos[2] = parentPack.pos[2] + subPack.pos[2];
		
		if(parentPack.rot != null || subPack.rot != null){
			correctPack.rot = new float[3];
		}
		if(parentPack.rot != null){
			correctPack.rot[0] += parentPack.rot[0];
			correctPack.rot[1] += parentPack.rot[1];
			correctPack.rot[2] += parentPack.rot[2];
		}
		if(subPack.rot != null){
			correctPack.rot[0] += subPack.rot[0];
			correctPack.rot[1] += subPack.rot[1];
			correctPack.rot[2] += subPack.rot[2];
		}
		
		correctPack.turnsWithSteer = parentPack.turnsWithSteer;
		correctPack.isController = subPack.isController;
		correctPack.inverseMirroring = subPack.inverseMirroring;
		correctPack.types = subPack.types;
		correctPack.customTypes = subPack.customTypes;
		correctPack.minValue = subPack.minValue;
		correctPack.maxValue = subPack.maxValue;
		return correctPack;
	}
			
    @Override
	public void readFromNBT(NBTTagCompound tagCompound){
		super.readFromNBT(tagCompound);
		this.packComponent = PackLoader.getVehicleComponentByName(tagCompound.getString("vehiclePack"), tagCompound.getString("vehicleName"));
		if(this.parts.size() == 0){
			NBTTagList partTagList = tagCompound.getTagList("Parts", 10);
			for(byte i=0; i<partTagList.tagCount(); ++i){
				try{
					NBTTagCompound partTag = partTagList.getCompoundTagAt(i);
					PackPart packPart = getPackDefForLocation(partTag.getDouble("offsetX"), partTag.getDouble("offsetY"), partTag.getDouble("offsetZ"));
					PackComponentPart partComponent = PackLoader.getPartComponentByName(tagCompound.getString("partPack"), tagCompound.getString("partName"));
					APart savedPart = partComponent.createPart((EntityVehicleE_Powered) this, partComponent, packPart, partTag);
					addPart(savedPart, true);
				}catch(Exception e){
					FileInterface.logError("ERROR IN LOADING PART FROM NBT!");
					FileInterface.logError(e.getMessage());
				}
			}
		}
	}
    
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound){
		super.writeToNBT(tagCompound);
		tagCompound.setString("vehiclePack", packComponent.packID);
		tagCompound.setString("vehicleName", packComponent.name);
		
		NBTTagList partTagList = new NBTTagList();
		for(APart part : parts){
			NBTTagCompound partTag = part.getPartNBTTag();
			//We need to set some extra data here for the part to allow this vehicle to know where it went.
			//This only gets set here during saving/loading, and is NOT returned in the item that comes from the part.
			partTag.setString("partPack", part.packComponent.packID);
			partTag.setString("partName", part.packComponent.name);
			partTag.setDouble("offsetX", part.baseOffset.x);
			partTag.setDouble("offsetY", part.baseOffset.y);
			partTag.setDouble("offsetZ", part.baseOffset.z);
			partTagList.appendTag(partTag);
		}
		tagCompound.setTag("Parts", partTagList);
		return tagCompound;
	}
	
	//Junk methods, forced to pull in.
	protected void entityInit(){}
	protected void readEntityFromNBT(NBTTagCompound p_70037_1_){}
	protected void writeEntityToNBT(NBTTagCompound p_70014_1_){}
}
