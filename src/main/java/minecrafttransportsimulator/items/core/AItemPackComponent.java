package minecrafttransportsimulator.items.core;

import minecrafttransportsimulator.MTS;
import minecrafttransportsimulator.dataclasses.CreativeTabPack;
import minecrafttransportsimulator.dataclasses.MTSRegistry;
import minecrafttransportsimulator.packs.PackLoader;
import minecrafttransportsimulator.packs.components.APackComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class AItemPackComponent<PackComponent extends APackComponent> extends Item{
	public PackComponent packComponent;
	
	public AItemPackComponent(){
		super();
	}
	
	public void setPackComponent(PackComponent component){
		packComponent = component;
		setRegistryName(MTS.MODID + ":" + component.packID + "_" + component.name);
		setUnlocalizedName(component.packID + "." + component.name);
		//Make a pack tab if we haven't already.
		if(!MTSRegistry.packTabs.containsKey(component.packID)){
			MTSRegistry.packTabs.put(component.packID, new CreativeTabPack(PackLoader.packObjects.get(component.packID)));
		}
		setCreativeTab(MTSRegistry.packTabs.get(component.packID));
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack){
		return getUnlocalizedName();
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack){
		return getUnlocalizedName();
	}
	
	@Override
	public String getUnlocalizedName(){
        return packComponent.getTranslatedName();
    }
}
