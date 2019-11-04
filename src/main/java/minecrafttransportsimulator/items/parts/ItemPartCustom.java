package minecrafttransportsimulator.items.parts;

import minecrafttransportsimulator.packs.objects.PackObjectVehicle.PackPart;

public class ItemPartCustom extends AItemPart{
	
	@Override
	public boolean isPartValidForPackDef(PackPart packPart){
		return packPart.customTypes != null && packPart.customTypes.contains(packComponent.pack.general.customType);
	}
}
