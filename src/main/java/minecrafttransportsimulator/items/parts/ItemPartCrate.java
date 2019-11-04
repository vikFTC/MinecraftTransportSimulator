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

public class ItemPartCrate extends AItemPart{
	
	@Override
	public boolean isPartValidForPackDef(PackPart packPart){
		int rows = packComponent.pack.crate.rows;
		return packPart.minValue <= rows && packPart.maxValue >= rows ? super.isPartValidForPackDef(packPart) : false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltipLines, ITooltipFlag flagIn){
		tooltipLines.add(I18n.format("info.item.crate.capacity") + packComponent.pack.crate.rows*9);
	}
}
