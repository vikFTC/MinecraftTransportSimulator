package minecrafttransportsimulator.items.instances;

import java.util.List;

import minecrafttransportsimulator.MTS;
import minecrafttransportsimulator.items.components.AItemBase;
import minecrafttransportsimulator.items.components.IItemTooltipLines;
import minecrafttransportsimulator.items.components.IItemVehicleInteractable;
import minecrafttransportsimulator.packets.general.PacketChat;
import minecrafttransportsimulator.packets.parts.PacketPartEngineLinked;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import minecrafttransportsimulator.vehicles.parts.APart;
import minecrafttransportsimulator.vehicles.parts.APartEngine;
import minecrafttransportsimulator.wrappers.WrapperGUI;
import minecrafttransportsimulator.wrappers.WrapperNBT;
import minecrafttransportsimulator.wrappers.WrapperPlayer;

public class ItemJumperCable extends AItemBase implements IItemTooltipLines, IItemVehicleInteractable{
	public static APartEngine<? extends EntityVehicleE_Powered> lastEngineClicked;
	
	@Override
	public boolean isStackable(){
		return false;
	}
	
	@Override
	public void addTooltipLines(List<String> tooltipLines, WrapperNBT data){
		for(byte i=1; i<=5; ++i){
			tooltipLines.add(WrapperGUI.translate("info.item.jumpercable.line" + String.valueOf(i)));
		}
	}
	
	@Override
	public void doVehicleInteraction(EntityVehicleE_Powered vehicle, APart<? extends EntityVehicleE_Powered> part, WrapperPlayer player, PlayerOwnerState ownerState, boolean rightClick, WrapperNBT data){
		if(rightClick){
			if(part instanceof APartEngine){
				APartEngine<? extends EntityVehicleE_Powered> engine = (APartEngine<? extends EntityVehicleE_Powered>) part;
				if(engine.linkedEngine == null){
					if(lastEngineClicked == null){
						lastEngineClicked = engine;
						player.sendPacket(new PacketChat("interact.jumpercable.firstlink"));
					}else if(!lastEngineClicked.equals(engine)){
						if(lastEngineClicked.vehicle.equals(engine.vehicle)){
							player.sendPacket(new PacketChat("interact.jumpercable.samevehicle"));
							lastEngineClicked = null;
						}else if(engine.partPos.distanceTo(lastEngineClicked.partPos) < 15){
							engine.linkedEngine = lastEngineClicked;
							lastEngineClicked.linkedEngine = engine;
							lastEngineClicked = null;
							MTS.MTSNet.sendToAll(new PacketPartEngineLinked(engine, engine.linkedEngine));
							player.sendPacket(new PacketChat("interact.jumpercable.secondlink"));	
						}else{
							player.sendPacket(new PacketChat("interact.jumpercable.toofar"));
							lastEngineClicked = null;
						}
					}
				}else{
					player.sendPacket(new PacketChat("interact.jumpercable.alreadylinked"));
				}
			}
		}
	}
}
