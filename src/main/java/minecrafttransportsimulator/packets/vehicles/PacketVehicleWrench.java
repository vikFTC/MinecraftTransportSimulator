package minecrafttransportsimulator.packets.vehicles;

import minecrafttransportsimulator.guis.instances.GUIInstruments;
import minecrafttransportsimulator.guis.instances.GUIVehicleEditor;
import minecrafttransportsimulator.systems.ConfigSystem;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import minecrafttransportsimulator.wrappers.WrapperGUI;
import minecrafttransportsimulator.wrappers.WrapperPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketVehicleWrench extends APacketVehiclePlayer{
	
	public PacketVehicleWrench(){}
	
	public PacketVehicleWrench(EntityVehicleE_Powered vehicle, WrapperPlayer player){
		super(vehicle, player);
	}

	public static class Handler implements IMessageHandler<PacketVehicleWrench, IMessage>{
		@Override
		public IMessage onMessage(final PacketVehicleWrench message, final MessageContext ctx){
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(new Runnable(){
				@Override
				public void run(){
					EntityVehicleE_Powered vehicle = getVehicle(message, ctx);
					WrapperPlayer player = getPlayer(message, ctx);
					
					if(vehicle != null && player != null){
						if(ConfigSystem.configObject.client.devMode.value && vehicle.world.isRemote){
							if(player.isRidingVehicle(vehicle)){
								WrapperGUI.openGUI(new GUIVehicleEditor(vehicle));
								return;
							}
						}
						WrapperGUI.openGUI(new GUIInstruments(vehicle, player));
					}
				}
			});
			return null;
		}
	}
}
