package mts_to_mc.interfaces;

import java.lang.reflect.Field;

import minecrafttransportsimulator.dataclasses.MTSRegistry;
import minecrafttransportsimulator.packs.PackLoader;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
 *
 * @author don_bruce
 */
@Mod.EventBusSubscriber
public class RegistryInterface{
	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event){
		
	}
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event){
		//First register all core items.
		for(Field field : MTSRegistry.class.getFields()){
			if(field.getType().equals(Item.class)){
				try{
					Item item = (Item) field.get(null);
					String name = field.getName().toLowerCase();
					if(!name.startsWith("itemblock")){
						event.getRegistry().register(item.setRegistryName(name).setUnlocalizedName(name));
						MTSRegistry.itemList.add(item);
					}else{
						name = name.substring("itemblock".length());
						event.getRegistry().register(item.setRegistryName(name).setUnlocalizedName(name));
						MTSRegistry.itemList.add(item);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
		//Now register all items in all the packs.
		for(Item item : PackLoader.getAllPackItems()){
			event.getRegistry().register(item);
		}
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
