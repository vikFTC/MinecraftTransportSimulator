package minecrafttransportsimulator.items.components;

/**Base item class for all MTS items.  Contains multiple methods to define the item's behavior,
 * such as display name, additional text to add to the tooltip, how the item handles left and
 * right-click usage, and so on.
 * 
 * @author don_bruce
 */
public abstract class AItemBase{

	/**
	 *  Returns true if the item can be stacked, false if not.
	 */
	public abstract boolean isStackable();
}
