package minecrafttransportsimulator.packloading;

import minecrafttransportsimulator.items.core.AItemPackComponent;

/**
 * Class that is used to hold the five key things that describe a pack component.
 * 
 * 1) The packID of the component.
 * 2) The name of the component itself.
 * 3) The item instance of the component.
 * 4) The crafting materials to make this item,. 
 * 5) The class that the component spawns (vehicle/parts only).
 * 
 * All of these things are created during the initial parsing of packs, so
 * instances of these objects may be safely referenced anywhere in the code
 * once pack parsing has been completed.  Having everything together allows
 * for easy cross-referencing in operations where one part is known and
 * another is desired.  Such as removing a part from a vehicle to get the
 * item, or knowing if an instrument can be crafting with specific materials..
 * 
 * @author don_bruce
 */
public class PackComponent{
	public final String packID;
	public final String name;
	public final AItemPackComponent item;
	public final String[] craftingMaterials;
	public final Class spawningClass;
	
	public PackComponent(String packID, String name, AItemPackComponent item, String[] craftingMaterials, Class spawningClass){
		this.packID = packID;
		this.name = name;
		this.item = item;
		this.craftingMaterials = craftingMaterials;
		this.spawningClass = spawningClass;
		item.setPackComponent(this);
	}
}