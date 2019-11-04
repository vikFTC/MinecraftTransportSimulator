package minecrafttransportsimulator.items.parts;

import java.util.List;

import javax.annotation.Nullable;

import minecrafttransportsimulator.packs.objects.PackObjectVehicle.PackPart;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPartGroundDevicePontoon extends AItemPart{
	
	@Override
	public boolean isPartValidForPackDef(PackPart packPart){
		float width = packComponent.pack.pontoon.width;
		return packPart.minValue <= width && packPart.maxValue >= width ? super.isPartValidForPackDef(packPart) : false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltipLines, ITooltipFlag flagIn){
		tooltipLines.add(I18n.format("info.item.ground_device.diameter") + packComponent.pack.pontoon.width);
		tooltipLines.add(I18n.format("info.item.ground_device.motivefriction") + 0);
		tooltipLines.add(I18n.format("info.item.ground_device.lateralfriction") + packComponent.pack.pontoon.lateralFriction);
		tooltipLines.add(I18n.format("info.item.ground_device.rotatesonshaft_false"));
		tooltipLines.add(I18n.format("info.item.ground_device.canfloat_true"));
	}
}
