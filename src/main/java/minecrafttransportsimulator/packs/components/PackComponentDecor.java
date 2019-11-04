package minecrafttransportsimulator.packs.components;

import minecrafttransportsimulator.items.core.AItemPackComponent;
import minecrafttransportsimulator.packs.objects.PackObjectDecor;

public class PackComponentDecor extends APackComponent<PackObjectDecor>{
	
	public PackComponentDecor(String packID, String name, AItemPackComponent item, PackObjectDecor pack){
		super(packID, name, item, pack);
	}
	
	@Override
	public String[] getCraftingMaterials(){
		return pack.general.materials;
	}
}