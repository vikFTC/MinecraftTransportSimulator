package minecrafttransportsimulator.items.parts;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class ItemPartEngineCar extends AItemPartEngine{
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void addExtraInformation(ItemStack stack, List<String> tooltipLines){
		tooltipLines.add(packComponent.pack.engine.isAutomatic ? I18n.format("info.item.engine.automatic") : I18n.format("info.item.engine.manual"));
		tooltipLines.add(I18n.format("info.item.engine.gearratios"));
		for(byte i=0; i<packComponent.pack.engine.gearRatios.length; i+=3){
			String gearRatios = String.valueOf(packComponent.pack.engine.gearRatios[i]);
			if(i+1 < packComponent.pack.engine.gearRatios.length){
				gearRatios += ",   " + String.valueOf(packComponent.pack.engine.gearRatios[i+1]);
			}
			if(i+2 < packComponent.pack.engine.gearRatios.length){
				gearRatios += ",   " + String.valueOf(packComponent.pack.engine.gearRatios[i+2]);
			}
			tooltipLines.add(gearRatios);
		}
	}
}
