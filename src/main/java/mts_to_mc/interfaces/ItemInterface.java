package mts_to_mc.interfaces;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Helper class for interfacing with Minecraft.
 * This class is used for interfacing with items.  Contains
 * various methods for getting item classes from strings, testing
 * if items/stacks are equal, and making itemstacks with metadata
 * taken into account.  Designed to work with both 1.12.2 and 1.14.4
 * systems, so forward any item-based interaction here.
 *
 * @author don_bruce
 */
public class ItemInterface{
	
	public static Item getItemByName(String name){
		return Item.getByNameOrId(name);
	}
	
	public static ItemStack getStackByParams(String name, int meta, int qty){
		if(meta != -1){
			return new ItemStack(Item.getByNameOrId(name), qty, meta);
		}else{
			return new ItemStack(Item.getByNameOrId(name), qty);
		}
	}
	
	public static ItemStack getStackFromPackMaterial(String material){
		int itemQty = Integer.valueOf(material.substring(material.lastIndexOf(':') + 1));
		material = material.substring(0, material.lastIndexOf(':'));
		
		int itemMetadata = Integer.valueOf(material.substring(material.lastIndexOf(':') + 1));
		material = material.substring(0, material.lastIndexOf(':'));
		return getStackByParams(material, itemQty, itemMetadata);
	}

}
