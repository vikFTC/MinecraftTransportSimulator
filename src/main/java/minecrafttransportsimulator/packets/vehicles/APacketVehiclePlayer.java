package minecrafttransportsimulator.packets.vehicles;

import io.netty.buffer.ByteBuf;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import minecrafttransportsimulator.wrappers.WrapperPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public abstract class APacketVehiclePlayer extends APacketVehicle{
	private int playerID;

	public APacketVehiclePlayer(){}
	
	public APacketVehiclePlayer(EntityVehicleE_Powered vehicle, WrapperPlayer player){
		super(vehicle);
		this.playerID = player.getID();
	}
	
	@Override
	public void fromBytes(ByteBuf buf){
		super.fromBytes(buf);
		this.playerID = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf){
		super.toBytes(buf);
		buf.writeInt(this.playerID);
	}
	
	protected static WrapperPlayer getPlayer(APacketVehiclePlayer message, MessageContext ctx){
		if(ctx.side.isServer()){
			return new WrapperPlayer((EntityPlayer) ctx.getServerHandler().player.world.getEntityByID(message.playerID));
		}else{
			return new WrapperPlayer((EntityPlayer) Minecraft.getMinecraft().world.getEntityByID(message.playerID));
		}
	}
}
