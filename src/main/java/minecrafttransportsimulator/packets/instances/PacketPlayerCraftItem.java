package minecrafttransportsimulator.packets.instances;

import io.netty.buffer.ByteBuf;
import mcinterface.WrapperEntityPlayer;
import mcinterface.WrapperWorld;
import minecrafttransportsimulator.dataclasses.MTSRegistry;
import minecrafttransportsimulator.items.packs.AItemPack;
import minecrafttransportsimulator.jsondefs.AJSONItem;
import minecrafttransportsimulator.packets.components.APacketBase;

/**Packet used to craft items from crafting benches.  This goes to the server which verifies the
 * player has the appropriate materials.  If so, the item is crafted on the server and materials
 * are deducted if required.  Packet is not sent back to the client as MC will auto-add the item
 * into the player's inventory and will do updates for us.
 * 
 * @author don_bruce
 */
public class PacketPlayerCraftItem extends APacketBase{
	private final AItemPack<? extends AJSONItem<?>> itemToCraft;
	
	public PacketPlayerCraftItem(AItemPack<? extends AJSONItem<?>> itemToCraft){
		super(null);
		this.itemToCraft = itemToCraft;
	}
	
	public PacketPlayerCraftItem(ByteBuf buf){
		super(buf);
		this.itemToCraft = MTSRegistry.packItemMap.get(readStringFromBuffer(buf)).get(readStringFromBuffer(buf));
	}
	
	@Override
	public void writeToBuffer(ByteBuf buf){
		super.writeToBuffer(buf);
		writeStringToBuffer(itemToCraft.definition.packID, buf);
		writeStringToBuffer(itemToCraft.definition.systemName, buf);
	}
	
	@Override
	public void handle(WrapperWorld world, WrapperEntityPlayer player){
		if(player.hasMaterials(itemToCraft)){
			player.craftItem(itemToCraft);
		}
	}
}
