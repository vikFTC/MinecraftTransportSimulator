package minecrafttransportsimulator.wrappers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import minecrafttransportsimulator.dataclasses.MTSRegistry;
import minecrafttransportsimulator.items.components.AItemBase;
import minecrafttransportsimulator.items.components.IItemCustomNameable;
import minecrafttransportsimulator.items.components.IItemTooltipLines;
import minecrafttransportsimulator.items.instances.ItemJerrycan;
import minecrafttransportsimulator.items.instances.ItemJumperCable;
import minecrafttransportsimulator.items.instances.ItemKey;
import minecrafttransportsimulator.items.instances.ItemWrench;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**Wrapper for the MC Item class.  Used to wrap MC type items into a class that we can
 * use a static set of methods on.  This wrapper also creates all items to be used
 * by MTS via events, which has the nice effect of putting ALL item-specific code
 * in this class.  Note that the constructor is private, as all items will be
 * created via the registry event and thus nothing new will need to go through
 * this class.  For a wrapper that can take ItemStacks that all the MC methods
 * spit out, see {@link WrapperItemStack}.
 *
 * @author don_bruce
 */
@Mod.EventBusSubscriber
public class WrapperItem extends Item{
	private final AItemBase item;
	
	private WrapperItem(AItemBase item){
		super();
		this.item = item;
	}
	
	/**
	 *  This is called by the main MC system to get the displayName for the item.
	 *  Normally this is a translated version of the unlocalized name, but this gets
	 *  overridden for items that implement {@link IItemCustomNameable}.
	 */
	@Override
	public String getItemStackDisplayName(ItemStack stack){
        return item instanceof IItemCustomNameable ? ((IItemCustomNameable) item).getItemName() : super.getItemStackDisplayName(stack);
	}
	
	/**
	 *  This is called by the main MC system to add tooltip lines to the item.
	 *  The ItemStack is passed-in here as it contains NBT data that may be used
	 *  to change the display of the tooltip.  Normally we don't add lines, but
	 *  we can if the item implements {@link IItemTooltipLines}. 
	 */
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltipLines, ITooltipFlag flagIn){
		if(item instanceof IItemTooltipLines){
			((IItemTooltipLines) item).addTooltipLines(tooltipLines, stack.getTagCompound());
		}
	}
	
	/**
	 * Creates and registers all items for the core mod.  This is done when Forge
	 * fires the event and ensures that all items are in their list in the registry.
	 */
	@SubscribeEvent
	public static void on(RegistryEvent.Register<Item> event){
		final List<AItemBase> itemsToRegister = new ArrayList<AItemBase>();
		itemsToRegister.add(new ItemJerrycan());
		itemsToRegister.add(new ItemJumperCable());
		itemsToRegister.add(new ItemKey());
		itemsToRegister.add(new ItemWrench());
		
		for(AItemBase item : itemsToRegister){
			WrapperItem wrapper = new WrapperItem(item);
			wrapper.setFull3D();
			wrapper.setCreativeTab(MTSRegistry.coreTab);
			if(!item.isStackable()){
				wrapper.setMaxStackSize(1);
			}
			
			String name = wrapper.item.getClass().getName().substring("Item".length()).toLowerCase();
			event.getRegistry().register(wrapper.setRegistryName(name).setUnlocalizedName(name));
			MTSRegistry.coreItems.add(wrapper);
		}
	}
}
