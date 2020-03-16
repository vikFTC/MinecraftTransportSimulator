package minecrafttransportsimulator.packets.vehicles;

import io.netty.buffer.ByteBuf;
import minecrafttransportsimulator.MTS;
import minecrafttransportsimulator.dataclasses.MTSRegistry;
import minecrafttransportsimulator.items.packs.ItemInstrument;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import minecrafttransportsimulator.wrappers.WrapperItemStack;
import minecrafttransportsimulator.wrappers.WrapperPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketVehicleInstruments extends APacketVehiclePlayer{
	private byte slot;
	private String instrumentPackID;
	private String instrumentSystemName;

	public PacketVehicleInstruments(){}
	
	public PacketVehicleInstruments(EntityVehicleE_Powered vehicle, WrapperPlayer player, byte slot, ItemInstrument instrument){
		super(vehicle, player);
		this.slot = slot;
		if(instrument != null){
			this.instrumentPackID = instrument.definition.packID;
			this.instrumentSystemName = instrument.definition.systemName;
		}else{
			this.instrumentPackID = "";
			this.instrumentSystemName = "";
		}
	}
	
	@Override
	public void fromBytes(ByteBuf buf){
		super.fromBytes(buf);
		this.slot = buf.readByte();
		this.instrumentPackID = ByteBufUtils.readUTF8String(buf);
		this.instrumentSystemName = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf){
		super.toBytes(buf);
		buf.writeByte(this.slot);
		ByteBufUtils.writeUTF8String(buf, this.instrumentPackID);
		ByteBufUtils.writeUTF8String(buf, this.instrumentSystemName);
	}

	public static class Handler implements IMessageHandler<PacketVehicleInstruments, IMessage> {
		public IMessage onMessage(final PacketVehicleInstruments message, final MessageContext ctx){
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(new Runnable(){
				@Override
				public void run(){
					EntityVehicleE_Powered vehicle = (EntityVehicleE_Powered) getVehicle(message, ctx);
					WrapperPlayer player = getPlayer(message, ctx);
					
					if(vehicle != null && player != null){
						//Check to make sure the instrument can fit in survival player's inventories.
						if(!player.isCreative() && ctx.side.isServer() && vehicle.instruments.containsKey(message.slot)){
							if(!player.addItem(new WrapperItemStack(vehicle.instruments.get(message.slot)))){
								return;
							}
						}
						
						//If we are removing the instrument, do so now.
						//Otherwise add the instrument.
						if(message.instrumentPackID.isEmpty()){
							vehicle.instruments.remove(message.slot);
						}else{
							//Check to make sure player has the instrument they are trying to put in.
							ItemInstrument instrument = (ItemInstrument) MTSRegistry.packItemMap.get(message.instrumentPackID).get(message.instrumentSystemName);
							if(!player.isCreative() && ctx.side.isServer() && instrument != null){
								WrapperItemStack stack = new WrapperItemStack(instrument);
								if(player.hasItem(stack)){
									player.removeItem(stack);
								}else{
									return;
								}
							}
							vehicle.instruments.put(message.slot, instrument);
						}
						
						if(ctx.side.isServer()){
							MTS.MTSNet.sendToAll(message);
						}
					}
				}
			});
			return null;
		}
	}
}
