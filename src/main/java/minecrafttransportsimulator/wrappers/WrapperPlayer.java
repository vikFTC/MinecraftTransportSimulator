package minecrafttransportsimulator.wrappers;

import minecrafttransportsimulator.MTS;
import minecrafttransportsimulator.items.components.AItemBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**Wrapper for the main player class.  This class wraps the player into a more
 * friendly instance that allows for common operations, like checking if the player
 * has an item, checking if they are OP, etc.  Also prevents the need to interact
 * with the class directly, which allows for abstraction in the code.
 *
 * @author don_bruce
 */
public class WrapperPlayer extends WrapperEntity{
	//TODO make this private when we can use wrappers everywhere.
	public final EntityPlayer player;
	
	public WrapperPlayer(EntityPlayer player){
		super(player);
		this.player = player;
	}
	
	/**
	 *  Returns the player's global UUID.  This is an ID that's unique to every player on Minecraft.
	 *  It does not change, ever.  Useful for assigning ownership where the entity ID of a player might
	 *  change between sessions.
	 */
	public String getUUID(){
		return EntityPlayer.getUUID(player.getGameProfile()).toString();
	}

	/**
	 *  Returns true if this player is OP.  Will always return true on single-player worlds.
	 */
	public boolean isOP(){
		return player.getServer() == null || player.getServer().getPlayerList().getOppedPlayers().getEntry(player.getGameProfile()) != null || player.getServer().isSinglePlayer();
	}
	
	/**
	 *  Returns true if this player is in creative mode.
	 */
	public boolean isCreative(){
		return player.capabilities.isCreativeMode;
	}
	
	/**
	 *  Returns true if this player is sneaking.
	 */
	public boolean isSneaking(){
		return player.isSneaking();
	}
	
	/**
	 *  Returns true if the item the player is holding is an instance of the
	 *  passed-in class.  Assumes main-hand for all cases.  If this item is
	 *  an instance of {@link WrapperItem}, then the test is done on the item
	 *  contained in the wrapper, not the wrapper itself.
	 */
	public boolean isHoldingItem(Class<?> itemClass){
		return player.getHeldItemMainhand().getItem() instanceof WrapperItem ? ((WrapperItem) player.getHeldItemMainhand().getItem()).item.getClass().isInstance(itemClass) : player.getHeldItemMainhand().getItem().getClass().isInstance(itemClass);
	}
	
	/**
	 *  Returns true if the player is holding the passed-in item.
	 *  Assumes main-hand for all cases.  This method allows
	 *  for string-based checking rather than class-based.
	 */
	public boolean isHoldingItem(String itemName){
		return player.getHeldItemMainhand().getItem().equals(Item.getByNameOrId(itemName));
	}
	
	/**
	 *  Returns the held stack as an instance of {@link WrapperItemStack}.
	 */
	public WrapperItemStack getHeldStack(){
		return new WrapperItemStack(player.getHeldItemMainhand());
	}
	
	/**
	 *  Returns the held MTS item  as an instance of {@link AItemBase},
	 *  or null if the player isn't holding a MTS Item.
	 */
	public AItemBase getHeldMTSItem(){
		return player.getHeldItemMainhand().getItem() instanceof WrapperItem ? ((WrapperItem) player.getHeldItemMainhand().getItem()).item : null;
	}
	
	/**
	 *  Returns the held item for this wrapper.  This method is only here
	 *  to prevent the need to re-do part code to work with part items.  That
	 *  will come in a later release once packs don't need compilation.
	 */
	public Item getHeldItem(){
		return player.getHeldItemMainhand().getItem();
	}
	
	/**
	 *  Returns true if the player has the quantity of the passed-in item in their inventory.
	 *  Note that the stack size defines the qty to find, and may well exceed 64 to find more
	 *  than 64 items.
	 */
	public boolean hasItem(WrapperItemStack stackToFind){
		int qtyToFind = stackToFind.getQty();
		for(ItemStack stack : player.inventory.mainInventory){
			if(stackToFind.doesStackMatch(stack)){
				qtyToFind -= stack.getCount();
				if(qtyToFind <= 0){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 *  Attempts to add the passed-in item(s) to the player's inventory.
	 *  Returns true if addition was successful.
	 */
	public boolean addItem(WrapperItemStack stackToAdd){
		return player.inventory.addItemStackToInventory(stackToAdd.getStack());
	}
	
	/**
	 *  Attempts to remove the passed-in item from the player's inventory.
	 *  Returns true if removal was successful.  Note that if the player
	 *  is in creative mode, then removal will not actually occur.
	 */
	public boolean removeItem(WrapperItemStack stackToRemove){
		if(isCreative()){
			return true;
		}else{
			return stackToRemove.getQty() == player.inventory.clearMatchingItems(stackToRemove.getItem(), stackToRemove.getMetadata(), stackToRemove.getQty(), stackToRemove.getActualNBT());
		}
	}
	
	/**
	 *  Sends a packet to this player over the network.
	 *  Note that this only works on the SERVER.
	 */
	public void sendPacket(IMessage packet){
		MTS.MTSNet.sendTo(packet, (EntityPlayerMP) player);
	}
}
