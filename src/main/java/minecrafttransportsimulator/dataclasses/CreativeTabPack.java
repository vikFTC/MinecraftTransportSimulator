package minecrafttransportsimulator.dataclasses;

import java.util.ArrayList;

import minecrafttransportsimulator.packs.PackInfoObject;
import minecrafttransportsimulator.packs.PackLoader;
import minecrafttransportsimulator.packs.components.PackComponentDecor;
import minecrafttransportsimulator.packs.components.PackComponentInstrument;
import minecrafttransportsimulator.packs.components.PackComponentItem;
import minecrafttransportsimulator.packs.components.PackComponentPart;
import minecrafttransportsimulator.packs.components.PackComponentVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**Pack-specific creative tab class.  One of each will be made for every pack
 * that loads into MTS.  These are held in the {@link MTSRegistry} along with the
 * core creative tab class.
 * 
 * @author don_bruce
 */
public final class CreativeTabPack extends CreativeTabs{
	private final PackInfoObject packInfo;
	
	public CreativeTabPack(PackInfoObject packInfo){
		super(packInfo.packID);
		this.packInfo = packInfo;
	}
	
	@Override
	public ItemStack getTabIconItem(){
		return new ItemStack(MTSRegistry.wrench);
	}

	@Override
    public void displayAllRelevantItems(NonNullList<ItemStack> givenList){
		//This is needed to re-sort the items here to get them in the correct order.
		//MC will re-order these by ID if we let it.
		givenList.clear();
		for(PackComponentVehicle component : PackLoader.vehicleComponents.get(packInfo.packID)){
			component.item.getSubItems(this, givenList);
		}
		for(PackComponentPart component : PackLoader.partComponents.get(packInfo.packID)){
			component.item.getSubItems(this, givenList);
		}
		for(PackComponentInstrument component : PackLoader.instrumentComponents.get(packInfo.packID)){
			component.item.getSubItems(this, givenList);
		}
		for(PackComponentDecor component : PackLoader.decorComponents.get(packInfo.packID)){
			component.item.getSubItems(this, givenList);
		}
		for(PackComponentItem component : PackLoader.itemComponents.get(packInfo.packID)){
			component.item.getSubItems(this, givenList);
		}
    }
	
	@Override
    public ItemStack getIconItemStack(){
		ArrayList<ItemStack> tabStacks = new ArrayList<ItemStack>();
		for(PackComponentVehicle component : PackLoader.vehicleComponents.get(packInfo.packID)){
			tabStacks.add(new ItemStack(component.item));
		}
		if(tabStacks.isEmpty()){
			for(PackComponentPart component : PackLoader.partComponents.get(packInfo.packID)){
				tabStacks.add(new ItemStack(component.item));
			}
		}
		return tabStacks.get((int) (Minecraft.getMinecraft().world.getTotalWorldTime()/20%tabStacks.size()));
    }
	
	@SideOnly(Side.CLIENT)
    public String getTranslatedTabLabel(){
        return packInfo.packName;
    }
}
