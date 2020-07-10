package minecrafttransportsimulator.packets.instances;

import io.netty.buffer.ByteBuf;
import mcinterface.WrapperEntityPlayer;
import mcinterface.WrapperWorld;
import minecrafttransportsimulator.packets.components.APacketBase;

/**Packet used for sending the player chat messages from the server.  Mainly for informing them
 * about things they did to a vehicle they interacted with.  Do NOT send this packet to the server
 * or it will crash when it tries to display chat messages on something without a screen!
 * 
 * @author don_bruce
 */
public class PacketPlayerChatMessage extends APacketBase{
	private final String chatMessage;
	
	public PacketPlayerChatMessage(String chatMessage){
		super(null);
		this.chatMessage = chatMessage;
	}
	
	public PacketPlayerChatMessage(ByteBuf buf){
		super(buf);
		this.chatMessage = readStringFromBuffer(buf);
	}
	
	@Override
	public void writeToBuffer(ByteBuf buf){
		super.writeToBuffer(buf);
		writeStringToBuffer(chatMessage, buf);
	}
	
	@Override
	public void handle(WrapperWorld world, WrapperEntityPlayer player){
		player.displayChatMessage(chatMessage);
	}
}
