package minecrafttransportsimulator.items.core;

import minecrafttransportsimulator.blocks.core.TileEntityDecor;
import minecrafttransportsimulator.dataclasses.MTSRegistry;
import minecrafttransportsimulator.packs.components.PackComponentDecor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemDecor extends AItemPackComponent<PackComponentDecor>{
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		if(!world.isRemote && player.getHeldItem(hand) != null){
			ItemStack heldStack = player.getHeldItem(hand);
			if(heldStack.getItem() != null){
				//We want to spawn above this block.
				pos = pos.up();
				
				//Based on the block type and light, pick a registered block template.
				if(!packComponent.pack.general.oriented && !packComponent.pack.general.lighted){
					world.setBlockState(pos, MTSRegistry.decorBasicDark.getDefaultState());
				}else if(packComponent.pack.general.oriented && !packComponent.pack.general.lighted){
					world.setBlockState(pos, MTSRegistry.decorOrientedDark.getDefaultState());
				}else if(!packComponent.pack.general.oriented && packComponent.pack.general.lighted){
					world.setBlockState(pos, MTSRegistry.decorBasicLight.getDefaultState());
				}else if(packComponent.pack.general.oriented && packComponent.pack.general.lighted){
					world.setBlockState(pos, MTSRegistry.decorOrientedLight.getDefaultState());
				}
				
				//Get the TE and set states for it.
				TileEntityDecor decorTile = ((TileEntityDecor) world.getTileEntity(pos));
				decorTile.decorName = packComponent.name;
				if(packComponent.pack.general.oriented){
					decorTile.rotation = (byte) Math.floor(((player.rotationYawHead + 45)%360/90F));
				}
		        
				//Use up the item we used to spawn this block if we are not in creative.
				if(!player.capabilities.isCreativeMode){
					player.inventory.clearMatchingItems(heldStack.getItem(), heldStack.getItemDamage(), 1, heldStack.getTagCompound());
				}
				return EnumActionResult.SUCCESS;
			}
		}
		return EnumActionResult.FAIL;
	}
}
