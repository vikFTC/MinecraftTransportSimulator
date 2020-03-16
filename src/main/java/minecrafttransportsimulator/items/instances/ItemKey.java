package minecrafttransportsimulator.items.instances;

import java.util.List;

import minecrafttransportsimulator.MTS;
import minecrafttransportsimulator.items.components.AItemBase;
import minecrafttransportsimulator.items.components.IItemTooltipLines;
import minecrafttransportsimulator.items.components.IItemVehicleInteractable;
import minecrafttransportsimulator.packets.general.PacketChat;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleKey;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import minecrafttransportsimulator.vehicles.parts.APart;
import minecrafttransportsimulator.vehicles.parts.PartSeat;
import minecrafttransportsimulator.wrappers.WrapperGUI;
import minecrafttransportsimulator.wrappers.WrapperNBT;
import minecrafttransportsimulator.wrappers.WrapperPlayer;

public class ItemKey extends AItemBase implements IItemTooltipLines,  IItemVehicleInteractable{
	
	@Override
	public boolean isStackable(){
		return false;
	}
		
	@Override
	public void addTooltipLines(List<String> tooltipLines, WrapperNBT data){
		for(byte i=1; i<=5; ++i){
			tooltipLines.add(WrapperGUI.translate("info.item.key.line" + String.valueOf(i)));
		}
	}
	
	@Override
	public void doVehicleInteraction(EntityVehicleE_Powered vehicle, APart<? extends EntityVehicleE_Powered> part, WrapperPlayer player, PlayerOwnerState ownerState, boolean rightClick, WrapperNBT data){
		if(rightClick){
			if(player.isSneaking()){
				//Try to change ownership of the vehicle.
				if(vehicle.ownerName.isEmpty()){
					vehicle.ownerName = player.getUUID();
					player.sendPacket(new PacketChat("interact.key.info.own"));
				}else{
					if(!ownerState.equals(PlayerOwnerState.USER)){
						vehicle.ownerName = "";
						player.sendPacket(new PacketChat("interact.key.info.unown"));
					}else{
						player.sendPacket(new PacketChat("interact.key.failure.alreadyowned"));
					}
				}
			}else{
				//Try to lock the vehicle.
				//First check to see if we need to set this key's vehicle.
				String keyVehicleUUID = data.getString("vehicle");
				String vehicleUUID = vehicle.getUniqueID().toString();
				if(keyVehicleUUID.isEmpty()){
					//Check if we are the owner before making this a valid key.
					if(!vehicle.ownerName.isEmpty() && ownerState.equals(PlayerOwnerState.USER)){
						player.sendPacket(new PacketChat("interact.key.failure.notowner"));
						return;
					}
					
					keyVehicleUUID = vehicleUUID;
					data.set("vehicle", keyVehicleUUID);
				}
				
				//Try to lock or unlock this vehicle.
				if(!keyVehicleUUID.equals(vehicleUUID)){
					player.sendPacket(new PacketChat("interact.key.failure.wrongkey"));
				}else{
					if(vehicle.locked){
						vehicle.locked = false;
						player.sendPacket(new PacketChat("interact.key.info.unlock"));
						//If we aren't in this vehicle, and we clicked a seat, start riding the vehicle.
						if(part instanceof PartSeat && !player.isRidingVehicle()){
							part.interactPart(player);
						}
					}else{
						vehicle.locked = true;
						player.sendPacket(new PacketChat("interact.key.info.lock"));
					}
					MTS.MTSNet.sendToAll(new PacketVehicleKey(vehicle));
				}
			}
		}
	}
}
