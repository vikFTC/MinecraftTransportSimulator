package minecrafttransportsimulator.packs.components;

import minecrafttransportsimulator.items.core.AItemPackComponent;

/**
 * Class that is used to hold the four key things that describe a pack component.
 * 
 * 1) The packID of the component.
 * 2) The name of the component itself.
 * 3) The item instance of the component.
 * 4) The PackObject that this component is for. 
 * 
 * All of these things are created during the initial parsing of packs, so
 * instances of these objects may be safely referenced anywhere in the code
 * once pack parsing has been completed.  Having everything together allows
 * for easy cross-referencing in operations where one part is known and
 * another is desired.  Such as removing a part from a vehicle to get the
 * item, or knowing if an instrument can be crafting with specific materials.
 * 
 * Do note that the PackComponent class is generic, and should be
 * extended to include the actual pack definition.  This allows for direct
 * reference to different object types.
 * 
 * @author don_bruce
 */
@SuppressWarnings("rawtypes")
public abstract class APackComponent<PackObject extends Object> implements Comparable<APackComponent>{
	public final String packID;
	public final String name;
	public final AItemPackComponent item;
	//This is not final as it allows us to modify the pack definition during runtime.
	public PackObject pack;
	
	public APackComponent(String packID, String name, AItemPackComponent item, PackObject pack){
		this.packID = packID;
		this.name = name;
		this.item = item;
		this.pack = pack;
		if(item != null){
			item.setPackComponent(this);
		}
	}
	
	@Override
	public int compareTo(APackComponent otherComponent){
		return name.compareTo(otherComponent.name);
	}
	
	public abstract String getTranslatedName();
		
	public abstract String[] getCraftingMaterials();
}