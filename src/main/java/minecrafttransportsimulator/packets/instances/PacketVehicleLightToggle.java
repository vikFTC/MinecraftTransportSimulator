package minecrafttransportsimulator.packets.instances;

import io.netty.buffer.ByteBuf;
import mcinterface.WrapperEntityPlayer;
import mcinterface.WrapperWorld;
import minecrafttransportsimulator.packets.components.APacketVehicle;
import minecrafttransportsimulator.rendering.components.LightType;
import minecrafttransportsimulator.vehicles.main.EntityVehicleF_Physics;

/**Packet used to toggle light states.  Sent from clients to servers to
 * tell them to change the light state of a vehicle, and then sent back
 * to all clients to have them update those states.  Note that it is possible
 * for other code to override what this packet does, such as turn signals.
 * 
 * @author don_bruce
 */
public class PacketVehicleLightToggle extends APacketVehicle{
	private final LightType lightType;
	
	public PacketVehicleLightToggle(EntityVehicleF_Physics vehicle, LightType lightType){
		super(vehicle);
		this.lightType = lightType;
	}
	
	public PacketVehicleLightToggle(ByteBuf buf){
		super(buf);
		this.lightType = LightType.values()[buf.readByte()];
	}
	
	@Override
	public void writeToBuffer(ByteBuf buf){
		super.writeToBuffer(buf);
		buf.writeByte(lightType.ordinal());
	}
	
	@Override
	public boolean handle(WrapperWorld world, WrapperEntityPlayer player, EntityVehicleF_Physics vehicle){
		if(vehicle.lightsOn.contains(lightType)){
			vehicle.lightsOn.remove(lightType);
		}else{
			vehicle.lightsOn.add(lightType);
		}
		return true;
	}
}
