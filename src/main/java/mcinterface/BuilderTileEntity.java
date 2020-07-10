package mcinterface;

import javax.annotation.Nullable;

import minecrafttransportsimulator.baseclasses.Point3i;
import minecrafttransportsimulator.blocks.tileentities.components.ATileEntityBase;
import minecrafttransportsimulator.blocks.tileentities.components.ITileEntityTickable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**Builder for the MC Tile Entity class (called BlockEntity in later MC versions cause
 * the people who maintain the mappings like to make life difficult through constant
 * re-naming of things).  This class interfaces with all the MC-specific code, and is 
 * constructed by feeding it an instance of {@link ATileEntityBase}.  This constructor
 * is package-private, as it should only be used by {@link BuilderBlock} to return
 * a Tile Entity for Minecraft to use.  Note that MC re-constructs this class with an
 * empty constructor, so the TE variable may be null for a bit after construction.
 * 
 * If ticking functionality is needed, have the tile entity implement {@link ITileEntityTickable}.
 * This will make the built TE call the {@link ITileEntityTickable#update()} method
 * each tick.
 *
 * @author don_bruce
 */
public class BuilderTileEntity<TileEntityType extends ATileEntityBase<?>> extends TileEntity{
	protected TileEntityType tileEntity;
	
	public BuilderTileEntity(){
		//Blank constructor for MC.  We set the TE variable in NBT instead.
	}
	
	BuilderTileEntity(TileEntityType tileEntity){
		this.tileEntity = tileEntity;
	}
	
	@Override
	public void setWorld(World world){
        super.setWorld(world);
        //Need to set the world wrapper here of the actual TE.
        tileEntity.world = new WrapperWorld(world);
    }
	
	@Override
	public void setPos(BlockPos pos){
		super.setPos(pos);
		//Need to set the position here of the actual TE.
		tileEntity.position = new Point3i(pos.getX(), pos.getY(), pos.getZ());
	}
	
	@Override
	public NBTTagCompound getUpdateTag(){
		//Gets called when the server sends this TE over as NBT data.
		//Get the full NBT tag, not just the position!
        return this.writeToNBT(new NBTTagCompound());
    }
	
	@Override
	@Nullable
    public SPacketUpdateTileEntity getUpdatePacket(){
		//Gets called when we do a blockstate update for this TE.
		//Done during initial placedown so we need to get the full data for inital state. 
		NBTTagCompound tag = new NBTTagCompound();
		tileEntity.save(new WrapperNBT(tag));
	    return new SPacketUpdateTileEntity(getPos(), -1, tag);
    }
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		//Called when the client gets a TE update packet.
		//We load the server-sent data here.
		tileEntity.load(new WrapperNBT(pkt.getNbtCompound()));
	}
	
	@Override
	public boolean shouldRenderInPass(int pass){
		//We can render in all passes.
        return true;
    }
	
	@Override
	public AxisAlignedBB getRenderBoundingBox(){
		//Return a box of size 16x16 here to ensure this entity doesn't disappear when we aren't looking at it exactly.
		return new AxisAlignedBB(pos).grow(8);
	}
	
	@Override
	@SuppressWarnings("unchecked")
    public void readFromNBT(NBTTagCompound tag){
		super.readFromNBT(tag);
		if(tileEntity == null){
			//Get the block that makes this TE and restore it from saved state.
			tileEntity = (TileEntityType) BuilderBlock.tileEntityMap.get(tag.getString("teid")).createTileEntity();
		}
		tileEntity.position = new Point3i(pos.getX(), pos.getY(), pos.getZ());
        tileEntity.load(new WrapperNBT(tag));
    }
    
	@Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag){
		super.writeToNBT(tag);
		tileEntity.save(new WrapperNBT(tag));
		//Also save the class ID so we know what to construct when we load from the world.
		tag.setString("teid", tileEntity.getClass().getSimpleName());
        return tag;
    }
	
	/**Tickable builder for {@link BuilderTileEntity}.
    *
    * @author don_bruce
    */
	public static class Tickable<TickableTileEntity extends ATileEntityBase<?>> extends BuilderTileEntity<TickableTileEntity> implements ITickable{
	    
		public Tickable(){
			//Blank constructor for MC.  We set the TE variable in NBT instead.
		}
	    
		Tickable(TickableTileEntity tileEntity){
			super(tileEntity);
		}
		
		@Override
		public void update(){
			((ITileEntityTickable) tileEntity).update();
		}
	}
}
