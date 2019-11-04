package minecrafttransportsimulator.packs.components;

import minecrafttransportsimulator.items.core.AItemPackComponent;
import minecrafttransportsimulator.packs.objects.PackObjectInstrument;

public class PackComponentInstrument extends APackComponent<PackObjectInstrument>{
	
	public PackComponentInstrument(String packID, String name, AItemPackComponent item, PackObjectInstrument pack){
		super(packID, name, item, pack);
	}

	@Override
	public String[] getCraftingMaterials(){
		return pack.general.materials;
	}
}