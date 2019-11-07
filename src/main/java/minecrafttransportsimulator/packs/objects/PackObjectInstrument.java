package minecrafttransportsimulator.packs.objects;

import java.util.ArrayList;
import java.util.List;

public class PackObjectInstrument{
    public PackInstrumentDefinition general = new PackInstrumentDefinition();
    public List<PackInstrumentComponent> components = new ArrayList<PackInstrumentComponent>();

    public class PackInstrumentDefinition{
    	public String name;
    	public String description;
    	public List<String> validVehicles;
    	public String[] materials;
    	public int textureXSectorStart;
    	public int textureYSectorStart;
    }
    
    public class PackInstrumentComponent{
    	public String rotationVariable;
    	public int xRotationPositionOffset;
    	public int yRotationPositionOffset;
    	public float rotationOffset;
    	public float rotationFactor;
    	
    	public String visibilityVariable;
    	public boolean dynamicVisibility;
    	public int visibleSectionHeight;
    	public float visibilityOffset;
    	public float visibilityFactor;
    	
    	public boolean lightOverlay;
    }
}