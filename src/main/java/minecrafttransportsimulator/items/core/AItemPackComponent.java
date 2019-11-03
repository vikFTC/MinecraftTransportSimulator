package minecrafttransportsimulator.items.core;

import java.util.List;

import javax.annotation.Nullable;

import minecrafttransportsimulator.MTS;
import minecrafttransportsimulator.dataclasses.CreativeTabPack;
import minecrafttransportsimulator.dataclasses.MTSRegistry;
import minecrafttransportsimulator.packloading.PackComponent;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class AItemPackComponent extends Item{
	public PackComponent component;
	
	public AItemPackComponent(){
		super();
	}
	
	public void setPackComponent(PackComponent component){
		setRegistryName(MTS.MODID + ":" + component.packID + "." + component.name);
		setUnlocalizedName(component.packID + "." + component.name);
		//Make a pack tab if we haven't already.
		if(!MTSRegistry.packTabs.containsKey(component.packID)){
			MTSRegistry.packTabs.put(component.packID, new CreativeTabPack(component.packID));
		}
		setCreativeTab(MTSRegistry.packTabs.get(component.packID));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltipLines, ITooltipFlag flagIn){
		tooltipLines.add(I18n.format(getUnlocalizedName(stack) + ".description"));
	}
}
