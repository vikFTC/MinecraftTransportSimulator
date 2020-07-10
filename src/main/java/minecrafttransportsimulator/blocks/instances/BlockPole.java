package minecrafttransportsimulator.blocks.instances;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mcinterface.WrapperEntityPlayer;
import mcinterface.WrapperNBT;
import mcinterface.InterfaceNetwork;
import mcinterface.WrapperWorld;
import minecrafttransportsimulator.baseclasses.BoundingBox;
import minecrafttransportsimulator.baseclasses.Point3i;
import minecrafttransportsimulator.blocks.components.ABlockBase;
import minecrafttransportsimulator.blocks.components.IBlockTileEntity;
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityPole;
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityPole_Sign;
import minecrafttransportsimulator.items.core.ItemWrench;
import minecrafttransportsimulator.items.packs.ItemPole;
import minecrafttransportsimulator.items.packs.ItemPoleComponent;
import minecrafttransportsimulator.jsondefs.JSONPoleComponent;
import minecrafttransportsimulator.packets.instances.PacketTileEntityPoleChange;

/**Pole block class.  This class allows for dynamic collision boxes and dynamic
 * placement of components on poles via the Tile Entity.
 *
 * @author don_bruce
 */
public class BlockPole extends ABlockBase implements IBlockTileEntity<JSONPoleComponent>{
	private final Map<Axis, BoundingBox> axisBounds = new HashMap<Axis, BoundingBox>();
	
	public BlockPole(){
		super(10.0F, 5.0F);
		double connectorRadius = 0.125D;
		double axialRadius = (0.5D - connectorRadius)/2D;
		double axialCenterPoint = 0.5D - axialRadius;
		axisBounds.put(Axis.NONE, new BoundingBox(0, 0, 0, connectorRadius, connectorRadius, connectorRadius));
		axisBounds.put(Axis.UP, new BoundingBox(0, axialCenterPoint, 0, connectorRadius, axialRadius, connectorRadius));
		axisBounds.put(Axis.DOWN, new BoundingBox(0, -axialCenterPoint, 0, connectorRadius, axialRadius, connectorRadius));
		axisBounds.put(Axis.NORTH, new BoundingBox(0, 0, -axialCenterPoint, connectorRadius, connectorRadius, axialRadius));
		axisBounds.put(Axis.SOUTH, new BoundingBox(0, 0, axialCenterPoint, connectorRadius, connectorRadius, axialRadius));
		axisBounds.put(Axis.EAST, new BoundingBox(axialCenterPoint, 0, 0, axialRadius, connectorRadius, connectorRadius));
		axisBounds.put(Axis.WEST, new BoundingBox(-axialCenterPoint, 0, 0, axialRadius, connectorRadius, connectorRadius));
	}
	
	@Override
	public void onPlaced(WrapperWorld world, Point3i location, WrapperEntityPlayer player){
		//If there's no NBT data, this is a new pole and needs to have its initial component added.
		if(!player.getHeldStack().hasTagCompound()){
			TileEntityPole pole = (TileEntityPole) world.getTileEntity(location);
			pole.components.put(Axis.NONE, TileEntityPole.createComponent(((ItemPoleComponent) player.getHeldStack().getItem()).definition));
		}
	}
	
	@Override
	public boolean onClicked(WrapperWorld world, Point3i location, Axis axis, WrapperEntityPlayer player){
		//Fire a packet to interact with this pole.  Will either add, remove, or allow editing of the pole.
		//Only fire packet if player is holding a pole component that's not an actual pole, a wrench,
		//or is clicking a sign with text.
		TileEntityPole pole = (TileEntityPole) world.getTileEntity(location);
		if(pole != null){
			boolean isPlayerHoldingWrench = player.isHoldingItem(ItemWrench.class);
			boolean isPlayerClickingEditableSign = pole.components.get(axis) instanceof TileEntityPole_Sign && pole.components.get(axis).definition.general.textLines != null;
			boolean isPlayerHoldingComponent = player.isHoldingItem(ItemPoleComponent.class) && !player.isHoldingItem(ItemPole.class);
			if(world.isClient()){
				if(isPlayerHoldingWrench){
					InterfaceNetwork.sendToServer(new PacketTileEntityPoleChange(pole, axis, null, null, true));
				}else if(isPlayerClickingEditableSign){
					InterfaceNetwork.sendToServer(new PacketTileEntityPoleChange(pole, axis, null, null, false));
				}else if(isPlayerHoldingComponent){
					List<String> textLines = null;
					ItemPoleComponent component = (ItemPoleComponent) player.getHeldStack().getItem();
					if(player.getHeldStack().hasTagCompound()){							
						textLines = new WrapperNBT(player.getHeldStack().getTagCompound()).getStrings("textLines", component.definition.general.textLines.length);
					}
					InterfaceNetwork.sendToServer(new PacketTileEntityPoleChange(pole, axis, component, textLines, false));	
				}else{
					return false;
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void addCollisionBoxes(WrapperWorld world, Point3i location, List<BoundingBox> collidingBoxes){
		//For every connection or component we have, return a collision box.
		TileEntityPole pole = (TileEntityPole) world.getTileEntity(location);
		if(pole != null){
			for(Axis axis : Axis.values()){
				if(world.getBlock(axis.getOffsetPoint(location)) instanceof BlockPole || world.isBlockSolid(axis.getOffsetPoint(location)) || pole.components.containsKey(axis)){
					collidingBoxes.add(axisBounds.get(axis));
				}
			}
		}else{
			super.addCollisionBoxes(world, location, collidingBoxes);
		}
	}
	
	@Override
	public TileEntityPole createTileEntity(){
		return new TileEntityPole();
	}
}
