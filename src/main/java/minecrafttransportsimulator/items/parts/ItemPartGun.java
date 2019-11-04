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

public class ItemPartGun extends AItemPart{
	
	@Override
	public boolean isPartValidForPackDef(PackPart packPart){
		float gunDiameter = packComponent.pack.gun.diameter;
		return packPart.minValue <= gunDiameter && packPart.maxValue >= gunDiameter ? super.isPartValidForPackDef(packPart) : false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltipLines, ITooltipFlag flagIn){
		tooltipLines.add(I18n.format("info.item.gun.type." + packComponent.name.substring(packComponent.pack.general.type.indexOf("_") + 1)));
		tooltipLines.add(I18n.format("info.item.gun.diameter") + packComponent.pack.gun.diameter);
		tooltipLines.add(I18n.format("info.item.gun.length") + packComponent.pack.gun.length);
		tooltipLines.add(I18n.format("info.item.gun.fireDelay") + packComponent.pack.gun.fireDelay);
		tooltipLines.add(I18n.format("info.item.gun.muzzleVelocity") + packComponent.pack.gun.muzzleVelocity);
		tooltipLines.add(I18n.format("info.item.gun.capacity") + packComponent.pack.gun.capacity);
		tooltipLines.add(I18n.format("info.item.gun.yawRange") + packComponent.pack.gun.minYaw + "-" + packComponent.pack.gun.maxYaw);
		tooltipLines.add(I18n.format("info.item.gun.pitchRange") + packComponent.pack.gun.minPitch + "-" + packComponent.pack.gun.maxPitch);
	}
}
