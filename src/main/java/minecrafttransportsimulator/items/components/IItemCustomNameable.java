package minecrafttransportsimulator.items.components;

/**Interface that allows the item to display a custom name rather than the auto-generated
 * name displayed by the lang file system.  Mainly used for pack items that need their
 * names pulled from JSON.
 * 
 * @author don_bruce
 */
public interface IItemCustomNameable{
	
	/**
	 *  Returns the name of this item.  Will be displayed to the player in-game, but is NOT used
	 *  for item registration, so may change depending on item state.
	 */
	public abstract String getItemName();
}
