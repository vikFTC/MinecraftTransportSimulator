package minecrafttransportsimulator.vehicles.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import mcinterface.BuilderEntity;
import mcinterface.WrapperNBT;
import mcinterface.WrapperWorld;
import minecrafttransportsimulator.MTS;
import minecrafttransportsimulator.baseclasses.Point3d;
import minecrafttransportsimulator.dataclasses.MTSRegistry;
import minecrafttransportsimulator.jsondefs.JSONVehicle.VehiclePart;
import minecrafttransportsimulator.packets.parts.PacketPartSeatRiderChange;
import minecrafttransportsimulator.systems.ConfigSystem;
import minecrafttransportsimulator.systems.RotationSystem;
import minecrafttransportsimulator.vehicles.parts.APart;
import minecrafttransportsimulator.vehicles.parts.PartBarrel;
import minecrafttransportsimulator.vehicles.parts.PartCrate;
import minecrafttransportsimulator.vehicles.parts.PartSeat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**This is the next class level above the base vehicle.
 * At this level we add methods for the vehicle's existence in the world.
 * Variables for position are defined here, but no methods for MOVING
 * this vehicle are present until later sub-classes.  This is also where we 
 * handle entity interaction with seats of this vehicle (mounting, dismounting, updates).
 * 
 * @author don_bruce
 */
abstract class EntityVehicleB_Existing extends EntityVehicleA_Base{
	
	//External state control.
	public boolean brakeOn;
	public boolean parkingBrakeOn;
	public boolean locked;
	public String ownerName = "";
	public String displayText = "";
	
	//Internal states.
	public byte prevParkingBrakeAngle;
	public byte parkingBrakeAngle;
	public double airDensity;
	public double currentMass;
	public double velocity;
	public double prevVelocity;
	public double normalizedGroundVelocity;
	public Point3d headingVector = new Point3d(0, 0, 0);
	public Point3d normalizedVelocity = new Point3d(0, 0, 0);
	public Point3d verticalVector = new Point3d(0, 0, 0);
	public Point3d sideVector = new Point3d(0, 0, 0);

	
	/**Cached map that links entity IDs to the seats riding them.  Used for mounting/dismounting functions.*/
	private final BiMap<Integer, PartSeat> riderSeats = HashBiMap.create();
	
	/**List for storage of rider linkages to seats.  Populated during NBT load and used to populate the riderSeats map after riders load.*/
	private List<Double[]> riderSeatPositions = new ArrayList<Double[]>();
			
	public EntityVehicleB_Existing(BuilderEntity builder, WrapperWorld world, WrapperNBT data){
		super(builder, world, data);
		this.locked = data.getBoolean("locked");
		this.parkingBrakeOn = data.getBoolean("parkingBrakeOn");
		this.brakeOn = data.getBoolean("brakeOn");
		this.ownerName = data.getString("ownerName");
		this.displayText = data.getString("displayText");
		if(displayText.isEmpty()){
			displayText = definition.rendering.defaultDisplayText;
		}
	}
	
	@Override
	public void update(){
		super.update();
		//Set vectors to current velocity and orientation.
		normalizedVelocity.set(motion.x, 0D, motion.z);
		normalizedGroundVelocity = normalizedVelocity.dotProduct(headingVector);
		normalizedVelocity.y = motion.y;
		prevVelocity = velocity;
		velocity = Math.abs(normalizedVelocity.dotProduct(headingVector));
		normalizedVelocity.normalize();
		verticalVector = new Point3d(0D, 1D, 0D).rotateFine(rotation);
		sideVector = headingVector.crossProduct(verticalVector);
		
		//Update mass.
		if(definition != null){
			currentMass = getCurrentMass();
			airDensity = 1.225*Math.pow(2, -position.y/(500D*world.getMaxHeight()/256D));
		}
		
		//Update parking brake angle.
		prevParkingBrakeAngle = parkingBrakeAngle;
		if(parkingBrakeOn && !locked && velocity < 0.25){
			if(parkingBrakeAngle < 30){
				prevParkingBrakeAngle = parkingBrakeAngle;
				++parkingBrakeAngle;
			}
		}else{
			if(parkingBrakeAngle > 0){
				prevParkingBrakeAngle = parkingBrakeAngle;
				--parkingBrakeAngle;
			}
		}
		
		//Check every tick to see if we still have riders in seats.
		//If we are missing a rider, dismount them off of the vehicle.
		Integer riderToRemove = -1;
		Iterator<Integer> seatIterator = riderSeats.keySet().iterator();
		while(seatIterator.hasNext()){
			Integer entityID = seatIterator.next();
			boolean passengerIsValid = false;
			for(Entity passenger : getPassengers()){
				if(passenger.getEntityId() == entityID){
					passengerIsValid = true;
					break;
				}
			}
			if(!passengerIsValid){
				Entity rider = world.getEntityByID(entityID);
				if(rider != null){
					PartSeat seat = getSeatForRider(rider);
					if(seat != null){
						riderToRemove = entityID;
					}
				}else{
					seatIterator.remove();
				}
			}
		}
		if(riderToRemove != -1){
			removeRiderFromSeat(world.getEntityByID(riderToRemove), getSeatForRider(world.getEntityByID(riderToRemove)));
		}
	}
	
	@Override
	public void updatePassenger(Entity passenger){
		PartSeat seat = this.getSeatForRider(passenger);
		if(seat != null){
			Point3d seatOffset = RotationSystem.getRotatedPoint(new Point3d(0, -seat.getHeight()/2F + passenger.getYOffset() + passenger.height, 0), seat.placementRotation.x, seat.placementRotation.y, seat.placementRotation.z);
			if(seat.parentPart != null){
				seatOffset = RotationSystem.getRotatedPoint(seatOffset, seat.parentPart.getActionRotation(0).x, seat.parentPart.getActionRotation(0).y, seat.parentPart.getActionRotation(0).z);
			}
			seatOffset = RotationSystem.getRotatedPoint(seatOffset, rotationPitch, rotationYaw, rotationRoll);
			seatOffset = seatOffset.add(seat.worldPos);
			passenger.setPosition(seatOffset.x, seatOffset.y - passenger.height, seatOffset.z);

		}else if(definition != null && !this.riderSeatPositions.isEmpty()){
			Double[] seatLocation = this.riderSeatPositions.get(this.getPassengers().indexOf(passenger));
			APart part = getPartAtLocation(seatLocation[0], seatLocation[1], seatLocation[2]);
			if(part instanceof PartSeat){
				riderSeats.put(passenger.getEntityId(), (PartSeat) part);
			}else{
				MTS.MTSLog.error("ERROR: No seat was found when trying to update seated passenger.  Did someone change the seat linking?");
				if(!world.isRemote){
					passenger.dismountRidingEntity();
				}
				return;
			}
		}
	}
	
    /**
     * Adds a rider to this vehicle and sets their seat.
     * All riders MUST be added through this method.
     */
	public void setRiderInSeat(Entity rider, PartSeat seat){
		boolean riderAlreadyInSeat = getSeatForRider(rider) != null;
		riderSeats.put(rider.getEntityId(), seat);
		rider.startRiding(this, true);
		//If we weren't riding before, set the player's yaw to the same yaw as the vehicle.
		//We do this to ensure we don't have 360+ rotations to deal with.
		if(!riderAlreadyInSeat){
			rider.rotationYaw =  (float) (this.rotationYaw + seat.placementRotation.y);
		}
		if(!world.isRemote){
			MTS.MTSNet.sendToAll(new PacketPartSeatRiderChange(seat, rider, true));
		}
	}
	
	/**
     * Removes the rider safely from this vehicle, attempting to set their dismount point in the process.
     */
	public void removeRiderFromSeat(Entity rider, PartSeat seat){
		riderSeats.remove(rider.getEntityId());
		if(!world.isRemote){
			VehiclePart packPart = this.getPackDefForLocation(seat.placementOffset.x, seat.placementOffset.y, seat.placementOffset.z);
			Point3d dismountPosition;
			if(packPart.dismountPos != null){
				dismountPosition = RotationSystem.getRotatedPoint(new Point3d(packPart.dismountPos[0], packPart.dismountPos[1], packPart.dismountPos[2]), rotationPitch, rotationYaw, rotationRoll).add(positionVector);
			}else{
				dismountPosition = RotationSystem.getRotatedPoint(seat.placementOffset.copy().add(seat.placementOffset.x > 0 ? 2D : -2D, 0D, 0D), rotationPitch, rotationYaw, rotationRoll).add(positionVector);	
			}
			rider.setPositionAndRotation(dismountPosition.x, dismountPosition.y, dismountPosition.z, rider.rotationYaw, rider.rotationPitch);
			MTS.MTSNet.sendToAll(new PacketPartSeatRiderChange(seat, rider, false));
		}
	}
	
	public Entity getRiderForSeat(PartSeat seat){
		return riderSeats.inverse().containsKey(seat) ? world.getEntityByID(riderSeats.inverse().get(seat)) : null;
	}
	
	public PartSeat getSeatForRider(Entity rider){
		return riderSeats.get(rider.getEntityId());
	}
	
	/**
	 * Call this to remove this vehicle.  This should be called when the vehicle has crashed, as it
	 * ejects all parts and damages all players.  Explosions may not occur in crashes depending on config 
	 * settings or a lack of fuel or explodable cargo.  Call only on the SERVER as this is for item-spawning 
	 * code and player damage code.
	 */
	public void destroyAtPosition(double x, double y, double z){
		this.setDead();
		//Remove all parts from the vehicle and place them as items.
		for(APart part : getVehicleParts()){
			if(part.getItemForPart() != null){
				ItemStack partStack = new ItemStack(part.getItemForPart());
				NBTTagCompound stackTag = part.getData();
				if(stackTag != null){
					partStack.setTagCompound(stackTag);
				}
				world.spawnEntity(new EntityItem(world, part.worldPos.x, part.worldPos.y, part.worldPos.z, partStack));
			}
		}
		
		//Also drop some crafting ingredients as items.
		for(ItemStack craftingStack : MTSRegistry.getMaterials(MTSRegistry.packItemMap.get(definition.packID).get(definition.systemName))){
			for(int i=0; i<craftingStack.getCount(); ++i){
				if(this.rand.nextDouble() < ConfigSystem.configObject.damage.crashItemDropPercentage.value){
					world.spawnEntity(new EntityItem(world, this.posX, this.posY, this.posZ, new ItemStack(craftingStack.getItem(), 1, craftingStack.getMetadata())));
				}
			}
		}
	}	
	
	/**
	 * Calculates the current mass of the vehicle.
	 * Includes core mass, player weight and inventory, and cargo.
	 */
	protected float getCurrentMass(){
		int currentMass = definition.general.emptyMass;
		for(APart part : parts){
			if(part instanceof PartCrate){
				currentMass += calculateInventoryWeight(((PartCrate) part).crateInventory);
			}else if(part instanceof PartBarrel){
				currentMass += ((PartBarrel) part).getFluidAmount()/50;
			}
		}
		
		//Add passenger inventory mass as well.
		for(Entity passenger : this.getPassengers()){
			if(passenger instanceof EntityPlayer){
				currentMass += 100 + calculateInventoryWeight(((EntityPlayer) passenger).inventory);
			}else{
				currentMass += 100;
			}
		}
		return currentMass;
	}
	
	/**
	 * Calculates the weight of the inventory passed in.
	 */
	private static float calculateInventoryWeight(IInventory inventory){
		float weight = 0;
		for(int i=0; i<inventory.getSizeInventory(); ++i){
			ItemStack stack = inventory.getStackInSlot(i);
			if(stack != null){
				double weightMultiplier = 1.0;
				for(String heavyItemName : ConfigSystem.configObject.general.itemWeights.weights.keySet()){
					if(stack.getItem().getRegistryName().toString().contains(heavyItemName)){
						weightMultiplier = ConfigSystem.configObject.general.itemWeights.weights.get(heavyItemName);
						break;
					}
				}
				weight += 5F*stack.getCount()/stack.getMaxStackSize()*weightMultiplier;
			}
		}
		return weight;
	}
	
	protected void updateHeadingVec(){
        double d1 = Math.cos(-Math.toRadians(rotationYaw) - Math.PI);
        double d2 = Math.sin(-Math.toRadians(rotationYaw) - Math.PI);
        double d3 = -Math.cos(-Math.toRadians(rotationPitch));
        double d4 = Math.sin(-Math.toRadians(rotationPitch));
        headingVector = new Point3d((d2 * d3), d4, (d1 * d3));
   	}
	
    @Override
	public void readFromNBT(NBTTagCompound tagCompound){
		super.readFromNBT(tagCompound);
		
		
		this.riderSeatPositions.clear();
		while(tagCompound.hasKey("Seat" + String.valueOf(riderSeatPositions.size()) + "0")){
			Double[] seatPosition = new Double[3];
			seatPosition[0] = tagCompound.getDouble("Seat" + String.valueOf(riderSeatPositions.size()) + "0");
			seatPosition[1] = tagCompound.getDouble("Seat" + String.valueOf(riderSeatPositions.size()) + "1");
			seatPosition[2] = tagCompound.getDouble("Seat" + String.valueOf(riderSeatPositions.size()) + "2");
			riderSeatPositions.add(seatPosition);
		}
	}
    
	@Override
	public void save(WrapperNBT data){
		super.save(data);
		data.setBoolean("locked", locked);
		data.setBoolean("brakeOn", brakeOn);
		data.setBoolean("parkingBrakeOn", parkingBrakeOn);
		data.setString("ownerName", ownerName);
		data.setString("displayText", displayText);
		
		//Correlate the order of passengers in the rider list with their location to save it to NBT.
		//That way riders don't get moved to other seats on world save/load.
		for(byte i=0; i<this.getPassengers().size(); ++i){
			Entity rider = this.getPassengers().get(i);
			PartSeat seat = this.getSeatForRider(rider);
			if(seat != null){
				tagCompound.setDouble("Seat" + String.valueOf(i) + "0", seat.placementOffset.x);
				tagCompound.setDouble("Seat" + String.valueOf(i) + "1", seat.placementOffset.y);
				tagCompound.setDouble("Seat" + String.valueOf(i) + "2", seat.placementOffset.z);
			}
		}
		return tagCompound;
	}
}
