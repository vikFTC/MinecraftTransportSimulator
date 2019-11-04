package minecrafttransportsimulator.packs.components;

import minecrafttransportsimulator.items.core.AItemPackComponent;
import minecrafttransportsimulator.packs.objects.PackObjectItem;

public class PackComponentItem extends APackComponent<PackObjectItem>{
	
	public PackComponentItem(String packID, String name, AItemPackComponent item, PackObjectItem pack){
		super(packID, name, item, pack);
	}

	@Override
	public String[] getCraftingMaterials(){
		return pack.general.materials;
	}
}