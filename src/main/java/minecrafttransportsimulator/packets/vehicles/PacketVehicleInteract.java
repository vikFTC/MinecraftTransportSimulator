package minecrafttransportsimulator.packets.vehicles;

import io.netty.buffer.ByteBuf;
import minecrafttransportsimulator.MTS;
import minecrafttransportsimulator.items.components.AItemBase;
import minecrafttransportsimulator.items.components.IItemVehicleInteractable;
import minecrafttransportsimulator.items.components.IItemVehicleInteractable.PlayerOwnerState;
import minecrafttransportsimulator.items.packs.parts.AItemPart;
import minecrafttransportsimulator.packets.general.PacketChat;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import minecrafttransportsimulator.vehicles.parts.APart;
import minecrafttransportsimulator.wrappers.WrapperItemStack;
import minecrafttransportsimulator.wrappers.WrapperPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketVehicleInteract extends APacketVehiclePlayer{
	private double hitX;
	private double hitY;
	private double hitZ;
	private PacketVehicleInteractType type;
	
	public PacketVehicleInteract(){}
	
	public PacketVehicleInteract(EntityVehicleE_Powered vehicle, WrapperPlayer player, double hitX, double hitY, double hitZ, PacketVehicleInteractType type){
		super(vehicle, player);
		this.hitX = hitX;
		this.hitY = hitY;
		this.hitZ = hitZ;
		this.type = type;
	}
	
	@Override
	public void fromBytes(ByteBuf buf){
		super.fromBytes(buf);
		this.hitX = buf.readDouble();
		this.hitY = buf.readDouble();
		this.hitZ = buf.readDouble();
		this.type = PacketVehicleInteractType.values()[buf.readByte()];
	}

	@Override
	public void toBytes(ByteBuf buf){
		super.toBytes(buf);
		buf.writeDouble(this.hitX);
		buf.writeDouble(this.hitY);
		buf.writeDouble(this.hitZ);
		buf.writeByte(this.type.ordinal());
	}

	public static class Handler implements IMessageHandler<PacketVehicleInteract, IMessage>{
		@Override
		public IMessage onMessage(final PacketVehicleInteract message, final MessageContext ctx){
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(new Runnable(){
				@Override
				public void run(){
					EntityVehicleE_Powered vehicle = getVehicle(message, ctx);
					WrapperPlayer player = getPlayer(message, ctx);
					boolean canPlayerEditVehicle = player.isOP() || vehicle.ownerName.isEmpty() || player.getUUID().equals(vehicle.ownerName);
					PlayerOwnerState ownerState = player.isOP() ? PlayerOwnerState.ADMIN : (canPlayerEditVehicle ? PlayerOwnerState.OWNER : PlayerOwnerState.USER);
					
					if(vehicle != null && player != null && !vehicle.isDead){
						APart<? extends EntityVehicleE_Powered> part = vehicle.getPartAtLocation(message.hitX, message.hitY, message.hitZ);
						WrapperItemStack heldStack = player.getHeldStack();
						AItemBase heldMTSItem = player.getHeldMTSItem();
						
						//If we clicked with with an item that can interact with a part or vehicle, perform that interaction.
						//Otherwise, try to do part-based interaction.
						if(heldMTSItem instanceof IItemVehicleInteractable){
							((IItemVehicleInteractable) heldMTSItem).doVehicleInteraction(vehicle, part, player, ownerState, message.type.isRightClick(), heldStack.getNBT());
						}else if(player.isHoldingItem("minecraft:name_tag") && message.type.isRightClick()){
							//Special case as this is a MC item.
							vehicle.displayText = heldStack.getDisplayText().length() > vehicle.definition.rendering.displayTextMaxLength ? heldStack.getDisplayText().substring(0, vehicle.definition.rendering.displayTextMaxLength - 1) : heldStack.getDisplayText();
							MTS.MTSNet.sendToAll(new PacketVehicleNameTag(vehicle));
						}else{
							//Not holding an item that can interact with a vehicle.  Try to interact with parts or slots.
							if(message.type.equals(PacketVehicleInteractType.PART_RIGHTCLICK)){
								part.interactPart(player.player);
							}else if(message.type.equals(PacketVehicleInteractType.PART_SLOT_RIGHTCLICK)){
								//Only owners can add vehicle parts.
								if(!canPlayerEditVehicle){
									player.sendPacket(new PacketChat("interact.failure.vehicleowned"));
								}else{
									//Attempt to add the part.  Vehicle is responsible for callback packet here.
									if(player.isHoldingItem(AItemPart.class)){
										if(vehicle.addPartFromItem((AItemPart) heldStack.getItem(), heldStack.getNBT(), message.hitX, message.hitY, message.hitZ)){				
											player.removeItem(new WrapperItemStack(heldStack, 1));
										}
									}
								}
							}
						}
					}
				}
			});
			return null;
		}
	}
	
	public static enum PacketVehicleInteractType{
		COLLISION_RIGHTCLICK,
		COLLISION_LEFTCLICK,
		PART_RIGHTCLICK,
		PART_LEFTCLICK,
		PART_SLOT_RIGHTCLICK;
		
		private boolean isRightClick(){
			return this.name().endsWith("RIGHTCLICK");
		}
	}
}
