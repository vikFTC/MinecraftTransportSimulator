package minecrafttransportsimulator.wrappers;

import net.minecraft.nbt.NBTTagCompound;

/**Wrapper for the NBT class.  Essentially a forwarder for NBT methods to make
 * it so we don't need to interface with MC classes.  Note that NBT should NEVER
 * be null here.  If this is for an item, make sure the item gets the NBT tag
 * assigned to it BEFORE it goes into this wrapper.
 *
 * @author don_bruce
 */
public class WrapperNBT{
	private final NBTTagCompound tag;
	
	public WrapperNBT(NBTTagCompound tag){
		this.tag = tag;
	}
	
	public <SetType> void set(String name, SetType value){
		if(value.getClass().equals(boolean.class)){
			tag.setBoolean(name, (boolean) value);
		}else if(value.getClass().equals(int.class)){
			tag.setInteger(name, (int) value);
		}else if(value.getClass().equals(double.class)){
			tag.setDouble(name, (double) value);
		}else if(value.getClass().equals(String.class)){
			tag.setString(name, (String) value);
		}else{
			throw new IllegalArgumentException("ERROR: Type: " + value.getClass().getName() + " is not supported for saving into NBT wrapper!");
		}
	}
	
	public boolean getBoolean(String name){
		return tag.getBoolean(name);
	}
	
	public int getInteger(String name){
		return tag.getInteger(name);
	}
	
	public double getDouble(String name){
		return tag.getDouble(name);
	}
	
	public String getString(String name){
		return tag.getString(name);
	}
}
