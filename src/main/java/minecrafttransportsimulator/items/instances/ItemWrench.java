package minecrafttransportsimulator.items.instances;

import java.util.List;

import minecrafttransportsimulator.dataclasses.MTSRegistry;
import minecrafttransportsimulator.items.components.AItemBase;
import minecrafttransportsimulator.items.components.IItemTooltipLines;
import minecrafttransportsimulator.items.components.IItemVehicleInteractable;
import minecrafttransportsimulator.packets.general.PacketChat;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleWrench;
import minecrafttransportsimulator.systems.ConfigSystem;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import minecrafttransportsimulator.vehicles.parts.APart;
import minecrafttransportsimulator.wrappers.WrapperGUI;
import minecrafttransportsimulator.wrappers.WrapperNBT;
import minecrafttransportsimulator.wrappers.WrapperPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemWrench extends AItemBase implements IItemTooltipLines,  IItemVehicleInteractable{
	
	@Override
	public boolean isStackable(){
		return false;
	}
	
	@Override
	public void addTooltipLines(List<String> tooltipLines, WrapperNBT data){
		tooltipLines.add(WrapperGUI.translate("info.item.wrench.use"));
		tooltipLines.add(WrapperGUI.translate("info.item.wrench.attack"));
		tooltipLines.add(WrapperGUI.translate("info.item.wrench.sneakattack"));
		if(ConfigSystem.configObject.client.devMode.value){
			tooltipLines.add("Use while seated in a vehicle to activate the DevMode editor.");
		}
	}
	
	@Override
	public void doVehicleInteraction(EntityVehicleE_Powered vehicle, APart<? extends EntityVehicleE_Powered> part, WrapperPlayer player, PlayerOwnerState ownerState, boolean rightClick, WrapperNBT data){
		//If the player isn't the owner of the vehicle, they can't interact with it.
		if(!ownerState.equals(PlayerOwnerState.USER)){
			if(rightClick){
				player.sendPacket(new PacketVehicleWrench(vehicle, player));
			}else{
				if(part != null && !player.isSneaking()){
					//Player can remove part.  Spawn item in the world and remove part.
					//Make sure to remove the part before spawning the item.  Some parts
					//care about this order and won't spawn items unless they've been removed.
					vehicle.removePart(part, false);
					Item droppedItem = part.getItemForPart();
					if(droppedItem != null){
						ItemStack droppedStack = new ItemStack(droppedItem);
						droppedStack.setTagCompound(part.getPartNBTTag());
						vehicle.world.spawnEntity(new EntityItem(vehicle.world, part.partPos.x, part.partPos.y, part.partPos.z, droppedStack));
					}
				}else if(player.isSneaking()){
					//Attacker is a sneaking player with a wrench.
					//Remove this vehicle if possible.
					if(!ConfigSystem.configObject.general.opPickupVehiclesOnly.value || ownerState.equals(PlayerOwnerState.ADMIN)){
						ItemStack vehicleStack = new ItemStack(MTSRegistry.packItemMap.get(vehicle.definition.packID).get(vehicle.definition.systemName));
						NBTTagCompound stackTag = vehicle.writeToNBT(new NBTTagCompound());
						vehicleStack.setTagCompound(stackTag);
						vehicle.world.spawnEntity(new EntityItem(vehicle.world, vehicle.posX, vehicle.posY, vehicle.posZ, vehicleStack));
						vehicle.setDead();
					}
				}
			}
		}else{
			player.sendPacket(new PacketChat("interact.failure.vehicleowned"));
		}
	}
}
