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

public class ItemPartGroundDeviceWheel extends AItemPart{
	
	@Override
	public boolean isPartValidForPackDef(PackPart packPart){
		float diameter = PackLoader.partPackMap.get(component).wheel.diameter;
		return packPart.minValue <= diameter && packPart.maxValue >= diameter ? super.isPartValidForPackDef(packPart) : false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltipLines, ITooltipFlag flagIn){
		PackPartObject pack = PackLoader.partPackMap.get(component); 
		tooltipLines.add(I18n.format("info.item.ground_device.diameter") + pack.wheel.diameter);
		tooltipLines.add(I18n.format("info.item.ground_device.motivefriction") + pack.wheel.motiveFriction);
		tooltipLines.add(I18n.format("info.item.ground_device.lateralfriction") + pack.wheel.lateralFriction);
		tooltipLines.add(I18n.format("info.item.ground_device.rotatesonshaft_true"));
		tooltipLines.add(I18n.format("info.item.ground_device.canfloat_false"));
	}
}
