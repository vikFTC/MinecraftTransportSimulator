package mts_to_mc.interfaces;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Helper class for interfacing with Minecraft.
 * This class is used for registering things, be it items, blocks,
 * packets, or whatever.  This will be done via Forge events.
 * The actual event used, and how the system interacts with that
 * event will change depending on the version of MC that is being
 * used, but it is assured that an event of some type will do the
 * loading, therefore it is not required that this class be called
 * at all, or any init() method be present for correct function.
 *
 * @author don_bruce
 */
public class RegistryInterface{
	
	
	public static void registerItems(){
		
	}
	
	public static Item getItemByName(String name){
		return Item.getByNameOrId(name);
	}
	
	public static ItemStack getItemStackByName(String name, int meta, int qty){
		if(meta != -1){
			return new ItemStack(Item.getByNameOrId(name), qty, meta);
		}else{
			return new ItemStack(Item.getByNameOrId(name), qty);
		}
	}

}
