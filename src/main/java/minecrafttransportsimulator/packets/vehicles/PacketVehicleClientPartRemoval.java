package minecrafttransportsimulator.packets.vehicles;

import minecrafttransportsimulator.vehicles.main.EntityVehicleF_Physics;
import minecrafttransportsimulator.vehicles.parts.APart;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketVehicleClientPartRemoval extends APacketVehiclePart{

	public PacketVehicleClientPartRemoval(){}
	
	public PacketVehicleClientPartRemoval(EntityVehicleF_Physics vehicle, double offsetX, double offsetY, double offsetZ){
		super(vehicle, offsetX, offsetY, offsetZ);
	}

	public static class Handler implements IMessageHandler<PacketVehicleClientPartRemoval, IMessage>{
		@Override
		public IMessage onMessage(final PacketVehicleClientPartRemoval message, final MessageContext ctx){
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(new Runnable(){
				@Override
				public void run(){
					EntityVehicleF_Physics vehicle = getVehicle(message, ctx);
					if(vehicle != null){
						APart partToRemove = vehicle.getPartAtLocation(message.offsetX, message.offsetY, message.offsetZ);
						vehicle.removePart(partToRemove, false);
					}
				}
			});
			return null;
		}
	}

}
