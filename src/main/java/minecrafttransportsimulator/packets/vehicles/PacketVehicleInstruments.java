package minecrafttransportsimulator.packets.vehicles;

import io.netty.buffer.ByteBuf;
import minecrafttransportsimulator.MTS;
import minecrafttransportsimulator.packs.PackLoader;
import minecrafttransportsimulator.packs.components.PackComponentInstrument;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketVehicleInstruments extends APacketVehiclePlayer{
	private byte slotToChange;
	private String instrumentToChangeToPackID;
	private String instrumentToChangeToName;

	public PacketVehicleInstruments(){}
	
	public PacketVehicleInstruments(EntityVehicleE_Powered vehicle, EntityPlayer player, byte slotToChange, PackComponentInstrument instrumentToChangeTo){
		super(vehicle, player);
		this.slotToChange = slotToChange;
		if(instrumentToChangeTo != null){
			this.instrumentToChangeToPackID = instrumentToChangeTo.packID;
			this.instrumentToChangeToName = instrumentToChangeTo.name;
		}else{
			this.instrumentToChangeToPackID = "";
			this.instrumentToChangeToName = "";
		}
	}
	
	@Override
	public void fromBytes(ByteBuf buf){
		super.fromBytes(buf);
		this.slotToChange = buf.readByte();
		this.instrumentToChangeToPackID = ByteBufUtils.readUTF8String(buf);
		this.instrumentToChangeToName = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf){
		super.toBytes(buf);
		buf.writeByte(this.slotToChange);
		ByteBufUtils.writeUTF8String(buf, this.instrumentToChangeToPackID);
		ByteBufUtils.writeUTF8String(buf, this.instrumentToChangeToName);
	}

	public static class Handler implements IMessageHandler<PacketVehicleInstruments, IMessage> {
		public IMessage onMessage(final PacketVehicleInstruments message, final MessageContext ctx){
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(new Runnable(){
				@Override
				public void run(){
					EntityVehicleE_Powered vehicle = (EntityVehicleE_Powered) getVehicle(message, ctx);
					EntityPlayer player = getPlayer(message, ctx);
					
					if(vehicle != null && player != null){
						//If we need to remove an instrument, make sure it can fit in survival player's inventories.
						if(!player.capabilities.isCreativeMode && ctx.side.isServer() && vehicle.instruments.size() > message.slotToChange && vehicle.instruments.get(message.slotToChange) != null){
							if(!player.inventory.addItemStackToInventory(new ItemStack(vehicle.instruments.get(message.slotToChange).item))){
								return;
							}
						}
						
						//Either clear the instrument, or add the new one.
						if(message.instrumentToChangeToPackID.isEmpty()){
							vehicle.instruments.set(message.slotToChange, null);
						}else{
							//If we are adding an instrument, make sure player has the instrument they are trying to put in.
							PackComponentInstrument instrumentComponent = PackLoader.getInstrumentComponentByName(message.instrumentToChangeToPackID, message.instrumentToChangeToName);
							if(!player.capabilities.isCreativeMode && ctx.side.isServer() && !message.instrumentToChangeToPackID.isEmpty()){
								if(player.inventory.hasItemStack(new ItemStack(instrumentComponent.item))){
									player.inventory.clearMatchingItems(instrumentComponent.item, -1, 1, null);
								}else{
									return;
								}
							}
							vehicle.instruments.set(message.slotToChange, instrumentComponent);
						}
						
						if(ctx.side.isServer()){
							MTS.MTSNet.sendToAllTracking(message, vehicle);
						}
					}
				}
			});
			return null;
		}
	}
}
