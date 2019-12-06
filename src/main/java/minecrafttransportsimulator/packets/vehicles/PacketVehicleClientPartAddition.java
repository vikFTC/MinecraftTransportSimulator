package minecrafttransportsimulator.packets.vehicles;

import io.netty.buffer.ByteBuf;
import minecrafttransportsimulator.packs.PackLoader;
import minecrafttransportsimulator.packs.components.PackComponentPart;
import minecrafttransportsimulator.packs.objects.PackObjectVehicle.PackPart;
import minecrafttransportsimulator.vehicles.main.EntityVehicleA_Base;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import minecrafttransportsimulator.vehicles.parts.APart;
import mts_to_mc.interfaces.FileInterface;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketVehicleClientPartAddition extends APacketVehiclePart{
	private String partPackID;
	private String partName;
	private NBTTagCompound partTag;

	public PacketVehicleClientPartAddition(){}
	
	public PacketVehicleClientPartAddition(EntityVehicleA_Base vehicle, double offsetX, double offsetY, double offsetZ, APart part){
		super(vehicle, offsetX, offsetY, offsetZ);
		this.partPackID = part.packComponent.packID;
		this.partName = part.packComponent.name;
		this.partTag = part.getPartNBTTag();
	}
	
	@Override
	public void fromBytes(ByteBuf buf){
		super.fromBytes(buf);
		this.partPackID = ByteBufUtils.readUTF8String(buf);
		this.partName = ByteBufUtils.readUTF8String(buf);
		this.partTag = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf){
		super.toBytes(buf);
		ByteBufUtils.writeUTF8String(buf, this.partPackID);
		ByteBufUtils.writeUTF8String(buf, this.partName);
		ByteBufUtils.writeTag(buf, this.partTag);
	}

	public static class Handler implements IMessageHandler<PacketVehicleClientPartAddition, IMessage>{
		@Override
		public IMessage onMessage(final PacketVehicleClientPartAddition message, final MessageContext ctx){
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(new Runnable(){
				@Override
				public void run(){
					EntityVehicleA_Base vehicle = (EntityVehicleA_Base) getVehicle(message, ctx);
					if(vehicle != null){
						PackPart packPart = vehicle.getPackDefForLocation(message.offsetX, message.offsetY, message.offsetZ);
						PackComponentPart packComponent = PackLoader.getPartComponentByName(message.partPackID, message.partName);
						try{
							APart newPart = packComponent.createPart((EntityVehicleE_Powered) vehicle, packComponent, packPart, message.partTag);
							vehicle.addPart(newPart, false);
						}catch(Exception e){
							FileInterface.logError("ERROR SPAWING PART ON CLIENT!");
							FileInterface.logError(e.getMessage());
						}
					}
				}
			});
			return null;
		}
	}

}
