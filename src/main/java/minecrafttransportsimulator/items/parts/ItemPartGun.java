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

public class ItemPartGun extends AItemPart{
	
	@Override
	public boolean isPartValidForPackDef(PackPart packPart){
		float gunDiameter = PackLoader.partPackMap.get(component).gun.diameter;
		return packPart.minValue <= gunDiameter && packPart.maxValue >= gunDiameter ? super.isPartValidForPackDef(packPart) : false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltipLines, ITooltipFlag flagIn){
		PackPartObject pack = PackLoader.partPackMap.get(component); 
		tooltipLines.add(I18n.format("info.item.gun.type." + component.spawningClass.getSimpleName().substring("PartGun".length()).toLowerCase()));
		tooltipLines.add(I18n.format("info.item.gun.diameter") + pack.gun.diameter);
		tooltipLines.add(I18n.format("info.item.gun.length") + pack.gun.length);
		tooltipLines.add(I18n.format("info.item.gun.fireDelay") + pack.gun.fireDelay);
		tooltipLines.add(I18n.format("info.item.gun.muzzleVelocity") + pack.gun.muzzleVelocity);
		tooltipLines.add(I18n.format("info.item.gun.capacity") + pack.gun.capacity);
		tooltipLines.add(I18n.format("info.item.gun.yawRange") + pack.gun.minYaw + "-" + pack.gun.maxYaw);
		tooltipLines.add(I18n.format("info.item.gun.pitchRange") + pack.gun.minPitch + "-" + pack.gun.maxPitch);
	}
}
