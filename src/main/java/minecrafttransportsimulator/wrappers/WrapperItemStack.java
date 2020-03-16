package minecrafttransportsimulator.wrappers;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**Wrapper for ItemStack.  This class is used to wrap up ItemStack instances into things
 * more helpful for use in MTS code.  In particular, this includes a set of standard
 * methods for transforming items from string-qty form to wrapper form, as well
 * as a lookup system for handling metadata values in later MC versions.
 * Most methods here are package-private, as interaction needs to be done via
 * other wrappers rather than the stack directly.  Say checking if a player has
 * materials, or what item they are holding.
 *
 * @author don_bruce
 */
public class WrapperItemStack{
	private final ItemStack stack;
	
	public WrapperItemStack(ItemStack stack){
		this.stack = stack;
	}
	
	public WrapperItemStack(Item item){
		this(item, 0, 0);
	}
	
	public WrapperItemStack(String itemName, int stackQty, int stackMeta){
		this.stack = new ItemStack(Item.getByNameOrId(itemName), stackQty, stackMeta);
	}
	
	public WrapperItemStack(Item item, int stackQty, int stackMeta){
		this.stack = new ItemStack(item, stackQty, stackMeta);
	}
	
	/**
	 *  Returns true if the wrapper's stack matches the passed-in stack.
	 */
	boolean doesStackMatch(ItemStack stackToMatch){
        return ItemStack.areItemsEqual(stack, stackToMatch);
	}
	
	/**
	 *  Returns the stack for this wrapper.
	 */
	ItemStack getStack(){
        return stack;
	}
	
	/**
	 *  Returns the item of the stack.
	 */
	Item getItem(){
        return stack.getItem();
	}
	
	/**
	 *  Returns the qty of items in the stack.
	 */
	int getQty(){
        return stack.getCount();
	}
	
	/**
	 *  Returns the metadata of the stack.
	 *  Note that this defaults to 0 in later MC releases.
	 */
	int getMetadata(){
        return stack.getMetadata();
	}
	
	/**
	 *  Returns the NBT data of the stack, in wrapper form.
	 *  If no NBT is present on this stack, one is created.
	 */
	public WrapperNBT getNBT(){
		if(!stack.hasTagCompound()){
			stack.setTagCompound(new NBTTagCompound());
		}
        return new WrapperNBT(stack.getTagCompound());
	}
}
