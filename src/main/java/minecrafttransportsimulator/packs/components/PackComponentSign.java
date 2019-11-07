package minecrafttransportsimulator.packs.components;

import minecrafttransportsimulator.items.core.AItemPackComponent;
import minecrafttransportsimulator.packs.objects.PackObjectSign;

public class PackComponentSign extends APackComponent<PackObjectSign>{
	
	public PackComponentSign(String packID, String name, AItemPackComponent item, PackObjectSign pack){
		super(packID, name, item, pack);
	}
	
	@Override
	public String getTranslatedName(){
		return pack.general.name;
	}
	
	@Override
	public String[] getCraftingMaterials(){
		return null;
	}
}