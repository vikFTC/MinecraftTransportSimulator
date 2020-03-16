package minecrafttransportsimulator.items.components;

import java.util.List;

import minecrafttransportsimulator.wrappers.WrapperNBT;

/**Interface that allows the item to display lines via tooltips.  Normally items don't do this
 * and only display their names.
 * 
 * @author don_bruce
 */
public interface IItemTooltipLines{
	
	
	/**
	 *  Called when the item tooltip is being displayed.  The passed-in list will contain
	 *  all the lines in the tooltip, so add or remove lines as you see fit.  If you don't
	 *  want to add any lines just leave this method blank. NBT is passed-in to allow for
	 *  state-based tooltip lines to be added.
	 */
	public abstract void addTooltipLines(List<String> tooltipLines, WrapperNBT data);
}
