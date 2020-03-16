package minecrafttransportsimulator.items.instances;

import java.util.List;

import minecrafttransportsimulator.MTS;
import minecrafttransportsimulator.items.components.AItemBase;
import minecrafttransportsimulator.items.components.IItemTooltipLines;
import minecrafttransportsimulator.items.components.IItemVehicleInteractable;
import minecrafttransportsimulator.packets.general.PacketChat;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleJerrycan;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import minecrafttransportsimulator.vehicles.parts.APart;
import minecrafttransportsimulator.wrappers.WrapperGUI;
import minecrafttransportsimulator.wrappers.WrapperNBT;
import minecrafttransportsimulator.wrappers.WrapperPlayer;

public class ItemJerrycan extends AItemBase implements IItemTooltipLines, IItemVehicleInteractable{
	
	@Override
	public boolean isStackable(){
		return false;
	}
	
	@Override
	public void addTooltipLines(List<String> tooltipLines, WrapperNBT data){
		tooltipLines.add(WrapperGUI.translate("info.item.jerrycan.fill"));
		tooltipLines.add(WrapperGUI.translate("info.item.jerrycan.drain"));
		if(data.getBoolean("isFull")){
			tooltipLines.add(WrapperGUI.translate("info.item.jerrycan.contains") + data.getString("fluidName"));
		}else{
			tooltipLines.add(WrapperGUI.translate("info.item.jerrycan.empty"));
		}
	}
	
	@Override
	public void doVehicleInteraction(EntityVehicleE_Powered vehicle, APart<? extends EntityVehicleE_Powered> part, WrapperPlayer player, PlayerOwnerState ownerState, boolean rightClick, WrapperNBT data){
		if(rightClick){
			if(data.getBoolean("isFull")){
				if(vehicle.fluidName.isEmpty() || vehicle.fluidName.equals(data.getString("fluidName"))){
					if(vehicle.fuel + 1000 > vehicle.definition.motorized.fuelCapacity){
						player.sendPacket(new PacketChat("interact.jerrycan.toofull"));
					}else{
						vehicle.fluidName = data.getString("fluidName");
						vehicle.fuel += 1000;
						data.set("isFull", false);
						player.sendPacket(new PacketChat("interact.jerrycan.success"));
						MTS.MTSNet.sendToAll(new PacketVehicleJerrycan(vehicle, vehicle.fluidName));
					}
				}else{
					player.sendPacket(new PacketChat("interact.jerrycan.wrongtype"));
				}
			}else{
				player.sendPacket(new PacketChat("interact.jerrycan.empty"));
			}
		}
	}
}
