package minecrafttransportsimulator.vehicles.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mcinterface.BuilderEntity;
import mcinterface.InterfaceNetwork;
import mcinterface.WrapperNBT;
import mcinterface.WrapperWorld;
import minecrafttransportsimulator.MTS;
import minecrafttransportsimulator.baseclasses.Point3d;
import minecrafttransportsimulator.dataclasses.MTSRegistry;
import minecrafttransportsimulator.items.packs.parts.AItemPart;
import minecrafttransportsimulator.jsondefs.JSONPart;
import minecrafttransportsimulator.jsondefs.JSONVehicle;
import minecrafttransportsimulator.jsondefs.JSONVehicle.VehiclePart;
import minecrafttransportsimulator.packets.instances.PacketVehiclePartChange;
import minecrafttransportsimulator.systems.PackParserSystem;
import minecrafttransportsimulator.vehicles.parts.APart;

/**Base vehicle class.  All vehicle entities should extend this class.
 * It is primarily responsible for the adding and removal of parts,
 * as well as dealing with what happens when this part is killed.
 * It is NOT responsible for custom data sets, sounds, or movement.
 * That should be done in sub-classes to keep methods segregated.
 * 
 * @author don_bruce
 */
abstract class EntityVehicleA_Base extends AEntityBase{
	/**The pack definition for this vehicle.*/
	public final JSONVehicle definition;
	
	/**This list contains all parts this vehicle has.  Do NOT directly modify this list.  Instead,
	 * call {@link #addPart}, {@link #addPartFromItem}, or {@link #removePart} to ensure all sub-classed
	 * operations are performed.  Note that if you are iterating over this list when you call one of those
	 * methods, you will get a CME!  This includes removing a part from it's {@link APart#updatePart} method.*/
	public final List<APart> parts = new ArrayList<APart>();	
	
	public EntityVehicleA_Base(BuilderEntity builder, WrapperWorld world, WrapperNBT data){
		super(builder, world, data);
		//FIXME set initial position in the item JSON, not here.
		//Set position to the spot that was clicked by the player.
		//Add a -90 rotation offset so the vehicle is facing perpendicular.
		//Makes placement easier and is less likely for players to get stuck.
		this.definition = (JSONVehicle) MTSRegistry.packItemMap.get(data.getString("packID")).get(data.getString("systemName")).definition;
		
		//Add parts.
		for(int i=0; i<data.getInteger("totalParts"); ++i){
			//Use a try-catch for parts in case they've changed since this vehicle was last placed.
			//Don't want crashes due to pack updates.
			try{
				WrapperNBT partData = data.getData("part_" + i);
				VehiclePart packPart = getPackDefForLocation(partData.getPoint3d("offset"));
				JSONPart partDefinition = (JSONPart) MTSRegistry.packItemMap.get(partData.getString("packID")).get(partData.getString("systemName")).definition;
				addPart(PackParserSystem.createPart((EntityVehicleF_Physics) this, packPart, partDefinition, partData), true);
			}catch(Exception e){
				MTS.MTSLog.error("ERROR IN LOADING PART FROM NBT!");
				e.printStackTrace();
			}
		}
	}
    
    /**
	 * Adds the passed-part to this vehicle, but in this case the part is in item form
	 * with associated data rather than a fully-constructed form.  This method will check
	 * if the item-based part can go to this vehicle.  If so, it is constructed and added,
	 * and a packet is sent to all clients to inform them of this change.  Returns true
	 * if all operations completed, false if the part was not able to be added.
	 * Note that the passed-in data MAY be null if the item didn't have any.
	 */
    public boolean addPartFromItem(AItemPart partItem, WrapperNBT partData, Point3d offset){
		//Get the part to add.
		VehiclePart packPart = getPackDefForLocation(offset);
		//Check to make sure the spot is free.
		if(getPartAtLocation(offset) == null){
			//Check to make sure the part is valid.
			if(packPart.types.contains(partItem.definition.general.type)){
				//Check to make sure the part is in parameter ranges.
				if(partItem.isPartValidForPackDef(packPart)){
					//Part is valid.  Create it and add it.
					addPart(PackParserSystem.createPart((EntityVehicleF_Physics) this, packPart, partItem.definition, partData != null ? partData : new WrapperNBT()), false);
					InterfaceNetwork.sendToClientsTracking(new PacketVehiclePartChange((EntityVehicleF_Physics) this, offset, partItem.definition.packID, partItem.definition.systemName, partData), this);
					
					//If the part doesn't have NBT, it must be new and we need to add default parts.
					//Only do this if we actually have subParts for this part.
					if(partData != null && partItem.definition.subParts != null){
						addDefaultParts(partItem.definition.subParts, this);
					}
					
					//Done adding the part, return true.
					return true;
				}
			}
		}
    	return false;
    }
	
	public void addPart(APart part, boolean ignoreCollision){
		parts.add(part);
		if(!ignoreCollision){
			//Check for collision, and boost if needed.
			if(part.isPartColliding()){
				//Adjust roll first, as otherwise we could end up with a sunk vehicle.
				angles.z = 0;
				position.y += part.getHeight();
			}
			
			//Sometimes we need to do this for parts that are deeper into the ground.
			if(part.wouldPartCollide(new Point3d(0, Math.max(0, -part.placementOffset.y) + part.getHeight(), 0))){
				position.y += part.getHeight();
			}
		}
	}
	
	public void removePart(APart part, boolean playBreakSound){
		if(parts.contains(part)){
			parts.remove(part);
			if(part.isValid()){
				part.removePart();
				if(!world.isClient()){
					InterfaceNetwork.sendToClientsTracking(new PacketVehiclePartChange((EntityVehicleF_Physics) this, part.placementOffset), this);
				}
			}
			if(!world.isClient()){
				if(playBreakSound){
					//FIXME play different sound for removing things.  Preferabley through the wrapper.
					//this.playSound(SoundEvents.ITEM_SHIELD_BREAK, 2.0F, 1.0F);
				}
			}
		}
	}
	
	/**
	 * Gets the part at the specified location.
	 */
	public APart getPartAtLocation(Point3d offset){
		for(APart part : this.parts){
			if(part.placementOffset.equals(offset)){
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
	public Map<Point3d, VehiclePart> getAllPossiblePackParts(){
		Map<Point3d, VehiclePart> packParts = new HashMap<Point3d, VehiclePart>();
		//First get all the regular part spots.
		for(VehiclePart packPart : definition.parts){
			Point3d partPos = new Point3d(packPart.pos[0], packPart.pos[1], packPart.pos[2]);
			packParts.put(partPos, packPart);
			
			//Check to see if we can put a additional parts in this location.
			//If a part is present at a location that can have an additional parts, we allow them to be placed.
			if(packPart.additionalParts != null){
				for(APart part : this.parts){
					if(part.placementOffset.equals(partPos)){
						for(VehiclePart additionalPart : packPart.additionalParts){
							partPos = new Point3d(additionalPart.pos[0], additionalPart.pos[1], additionalPart.pos[2]);
							packParts.put(partPos, additionalPart);
						}
						break;
					}
				}
			}
		}
		
		//Next get any sub parts on parts that are present.
		for(APart part : this.parts){
			if(part.definition.subParts != null){
				VehiclePart parentPack = getPackDefForLocation(part.placementOffset);
				for(VehiclePart extraPackPart : part.definition.subParts){
					VehiclePart correctedPack = getPackForSubPart(parentPack, extraPackPart);
					packParts.put(new Point3d(correctedPack.pos[0], correctedPack.pos[1], correctedPack.pos[2]), correctedPack);
				}
			}
			
		}
		return packParts;
	}
	
	/**
	 * Gets the pack definition at the specified location.
	 */
	public VehiclePart getPackDefForLocation(Point3d offset){
		//Check to see if this is a main part.
		for(VehiclePart packPart : definition.parts){
			if(isPackAtPosition(packPart, offset)){
				return packPart;
			}
			
			//Not a main part.  Check if this is an additional part.
			if(packPart.additionalParts != null){
				for(VehiclePart additionalPart : packPart.additionalParts){
					if(isPackAtPosition(additionalPart, offset)){
						return additionalPart;
					}
				}
			}
		}
		
		//If this is not a main part or an additional part, check the sub-parts.
		for(APart part : this.parts){
			if(part.definition.subParts.size() > 0){
				VehiclePart parentPack = getPackDefForLocation(part.placementOffset);
				for(VehiclePart extraPackPart : part.definition.subParts){
					VehiclePart correctedPack = getPackForSubPart(parentPack, extraPackPart);
					if(isPackAtPosition(correctedPack, offset)){
						return correctedPack;
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 *Helper method to prevent casting to floats all over for position-specific tests.
	 */
	public static boolean isPackAtPosition(VehiclePart packPart, Point3d offset){
		return (float)packPart.pos[0] == (float)offset.x && (float)packPart.pos[1] == (float)offset.y && (float)packPart.pos[2] == (float)offset.z;
	}
	
	/**
	 * Returns a PackPart with the correct properties for a SubPart.  This is because
	 * subParts inherit some properties from their parent parts. 
	 */
	public VehiclePart getPackForSubPart(VehiclePart parentPack, VehiclePart subPack){
		VehiclePart correctPack = definition.new VehiclePart();
		correctPack.isSubPart = true;
		
		//Get the offset position for this part.
		//If we will be mirrored, make sure to invert the x-coords of any sub-parts.
		correctPack.pos = new double[3];
		correctPack.pos[0] = parentPack.pos[0] + (parentPack.pos[0] < 0 ^ parentPack.inverseMirroring ? -subPack.pos[0] : subPack.pos[0]);
		correctPack.pos[1] = parentPack.pos[1] + subPack.pos[1];
		correctPack.pos[2] = parentPack.pos[2] + subPack.pos[2];
		
		//Add current and parent rotation to make a total rotation for this part.
		if(parentPack.rot != null || subPack.rot != null){
			correctPack.rot = new double[3];
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
		correctPack.dismountPos = subPack.dismountPos;
		correctPack.exhaustPos = subPack.exhaustPos;
        correctPack.exhaustVelocity = subPack.exhaustVelocity;
        correctPack.intakeOffset = subPack.intakeOffset;
        correctPack.additionalParts = subPack.additionalParts;
        correctPack.treadYPoints = subPack.treadYPoints;
        correctPack.treadZPoints = subPack.treadZPoints;
        correctPack.treadAngles = subPack.treadAngles;
        correctPack.defaultPart = subPack.defaultPart;
		return correctPack;
	}
	
	/**
	 * Helper method to allow for recursion when adding default parts.
	 * This method adds all default parts for the passed-in list of parts.
	 * The part list should consist of a "parts" JSON definition.
	 * This method should only be called when the vehicle or part with the
	 * passed-in definition is first placed, not when it's being loaded from saved data.
	 */
	public static void addDefaultParts(List<VehiclePart> partsToAdd, EntityVehicleA_Base vehicle){
		for(VehiclePart packDef : partsToAdd){
			if(packDef.defaultPart != null){
				try{
					String partPackID = packDef.defaultPart.substring(0, packDef.defaultPart.indexOf(':'));
					String partSystemName = packDef.defaultPart.substring(packDef.defaultPart.indexOf(':') + 1);
					try{
						APart newPart = PackParserSystem.createPart((EntityVehicleF_Physics) vehicle, packDef, (JSONPart) MTSRegistry.packItemMap.get(partPackID).get(partSystemName).definition, new WrapperNBT());
						vehicle.addPart(newPart, true);
						
						//Check if we have an additional parts.
						//If so, we need to check that for default parts.
						if(packDef.additionalParts != null){
							addDefaultParts(packDef.additionalParts, vehicle);
						}
						
						//Check all sub-parts, if we have any.
						//We need to make sure to convert them to the right type as they're offset.
						if(newPart.definition.subParts != null){
							List<VehiclePart> subPartsToAdd = new ArrayList<VehiclePart>();
							for(VehiclePart subPartPack : newPart.definition.subParts){
								subPartsToAdd.add(vehicle.getPackForSubPart(packDef, subPartPack));
							}
							addDefaultParts(subPartsToAdd, vehicle);
						}
					}catch(NullPointerException e){
						throw new IllegalArgumentException("ERROR: Attempted to add defaultPart: " + partPackID + ":" + partSystemName + " to: " + vehicle.definition.genericName + " but that part doesn't exist in the pack item registry.");
					}
				}catch(IndexOutOfBoundsException e){
					throw new IllegalArgumentException("ERROR: Could not parse defaultPart definition: " + packDef.defaultPart + ".  Format should be \"packId:partName\"");
				}
			}
		}
	}
	
	@Override
	public void save(WrapperNBT data){
		super.save(data);
		data.setString("packID", definition.packID);
		data.setString("systemName", definition.systemName);
		
		int totalParts = 0;
		for(APart part : parts){
			//Don't save the part if it's not valid.
			if(part.isValid()){
				++totalParts;
				WrapperNBT partData = part.getData();
				//We need to set some extra data here for the part to allow this vehicle to know where it went.
				//This only gets set here during saving/loading, and is NOT returned in the item that comes from the part.
				partData.setString("packID", part.definition.packID);
				partData.setString("systemName", part.definition.systemName);
				partData.setPoint3d("offset", part.placementOffset);
				data.setData("part_" + totalParts, partData);
			}
		}
		data.setInteger("totalParts", totalParts);
	}
}
