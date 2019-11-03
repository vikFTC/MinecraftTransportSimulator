package minecrafttransportsimulator.items.parts;

import java.util.List;

import javax.annotation.Nullable;

import minecrafttransportsimulator.packloading.PackLoader;
import minecrafttransportsimulator.packloading.PackPartObject;
import minecrafttransportsimulator.packloading.PackVehicleObject.PackPart;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPartPropeller extends AItemPart{
	
	@Override
	public boolean isPartValidForPackDef(PackPart packPart){
		float propellerDiameter = PackLoader.partPackMap.get(component).propeller.diameter;
		return packPart.minValue <= propellerDiameter && packPart.maxValue >= propellerDiameter ? super.isPartValidForPackDef(packPart) : false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltipLines, ITooltipFlag flagIn){
		PackPartObject pack = PackLoader.partPackMap.get(component); 
		tooltipLines.add(I18n.format(pack.propeller.isDynamicPitch ? "info.item.propeller.dynamicPitch" : "info.item.propeller.staticPitch"));
		tooltipLines.add(I18n.format("info.item.propeller.numberBlades") + pack.propeller.numberBlades);
		tooltipLines.add(I18n.format("info.item.propeller.pitch") + pack.propeller.pitch);
		tooltipLines.add(I18n.format("info.item.propeller.diameter") + pack.propeller.diameter);
		tooltipLines.add(I18n.format("info.item.propeller.maxrpm") + Math.round(60*340.29/(0.0254*Math.PI*pack.propeller.diameter)));
		if(stack.hasTagCompound()){
			tooltipLines.add(I18n.format("info.item.propeller.health") + (pack.propeller.startingHealth - stack.getTagCompound().getFloat("damage")));
		}else{
			tooltipLines.add(I18n.format("info.item.propeller.health") + pack.propeller.startingHealth);
		}
	}
}
