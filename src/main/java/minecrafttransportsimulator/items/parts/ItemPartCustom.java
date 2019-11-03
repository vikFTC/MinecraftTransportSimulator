package minecrafttransportsimulator.items.parts;

import minecrafttransportsimulator.packloading.PackLoader;
import minecrafttransportsimulator.packloading.PackVehicleObject.PackPart;

public class ItemPartCustom extends AItemPart{
	
	@Override
	public boolean isPartValidForPackDef(PackPart packPart){
		return packPart.customTypes != null && packPart.customTypes.contains(PackLoader.partPackMap.get(component).general.customType);
	}
}
