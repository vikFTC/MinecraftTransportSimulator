package mcinterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import minecrafttransportsimulator.baseclasses.BoundingBox;
import minecrafttransportsimulator.baseclasses.Damage;
import minecrafttransportsimulator.baseclasses.Point3d;
import minecrafttransportsimulator.baseclasses.Point3i;
import minecrafttransportsimulator.blocks.components.ABlockBase;
import minecrafttransportsimulator.blocks.components.ABlockBase.Axis;
import minecrafttransportsimulator.blocks.components.IBlockTileEntity;
import minecrafttransportsimulator.blocks.tileentities.components.ATileEntityBase;
import minecrafttransportsimulator.items.packs.AItemPack;
import minecrafttransportsimulator.jsondefs.AJSONItem;
import minecrafttransportsimulator.vehicles.main.AEntityBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

/**Wrapper for the world class.  This wrapper contains many common methods that 
 * MC has seen fit to change over multiple versions (such as lighting) and as such
 * provides a single point of entry to the world to interface with it.  This class
 * should be used whenever possible to replace the normal world object reference
 * with methods that re-direct to this wrapper.  This wrapper is normally created
 * from an instance of an {@link World} object passed-in to the constructor, so this
 * means you'll need something to get an instance of the MC world beforehand.
 * Note that other wrappers may access the world variable directly for things
 * that are specific to their classes (such as blocks getting states).
 *
 * @author don_bruce
 */
public class WrapperWorld{
	
	final World world;

	public WrapperWorld(World world){
		this.world = world;
	}
	
	/**
	 *  Returns true if this is a client world, false if we're on the server.
	 */
	public boolean isClient(){
		return world.isRemote;
	}
	
	/**
	 *  Returns the ID of the current dimension.
	 *  0 for overworld.
	 *  1 for the End.
	 *  -1 for the Nether.
	 *  Mods may add other values for their dims, so this list is not inclusive.
	 */
	public int getDimensionID(){
		return world.provider.getDimension();
	}
	
	/**
	 *  Returns the current world time, in ticks.  Useful when you need to sync
	 *  operations.  For animations, just use the system time.
	 */
	public long getTime(){
		return world.getTotalWorldTime();
	}
		
	/**
	 *  Returns the max build height for the world.  Note that entities may move and be saved
	 *  above this height, and moving above this height will result in rendering oddities.
	 */
	public long getMaxHeight(){
		return world.getHeight();
	}
	
	/**
	 *  Returns the entity that has the passed-in ID.
	 */
	public WrapperEntity getEntity(int id){
		return new WrapperEntity(world.getEntityByID(id));
	}
	
	/**
	 *  Returns the player with the passed-in ID.
	 */
	public WrapperPlayer getPlayer(int id){
		return new WrapperPlayer((EntityPlayer) world.getEntityByID(id));
	}
	
	/**
	 *  Attacks all entities that are in the passed-in damage range.  If the
	 *  passed-in entity is not null, then any entity riding the passed-in
	 *  entity that is inside the bounding box will not be attacked, nor will
	 *  the passed-in entity be attacked.  Useful for vehicles, where you don't 
	 *  want players firing weapons to hit themselves or the vehicle.
	 *  Note that if this is called on clients, then this method will not attack
	 *  any entities. Instead, it will return a map of all entities that could have
	 *  been attacked with the bounding box attacked if they are of type 
	 *  {@link BuilderEntity} as the value to the entity key.
	 *  This is because attacking cannot be done on clients, but it may be useful to 
	 *  know what entities could have been attacked should the call have been made on a server.
	 */
	public Map<WrapperEntity, BoundingBox> attackEntities(Damage damage, AEntityBase damageSource){
		AxisAlignedBB mcBox = new AxisAlignedBB(
				damage.box.globalCenter.x - damage.box.widthRadius,
				damage.box.globalCenter.y - damage.box.heightRadius,
				damage.box.globalCenter.z - damage.box.depthRadius,
				damage.box.globalCenter.x + damage.box.widthRadius,
				damage.box.globalCenter.y + damage.box.heightRadius,
				damage.box.globalCenter.z + damage.box.depthRadius
			);
		List<Entity> collidedEntities = world.getEntitiesWithinAABB(Entity.class, mcBox);
		if(!collidedEntities.isEmpty()){
			if(damageSource != null){
				//Iterate over all entities.  If the entity is the passed-in source, or riding the source, remove it.
				Iterator<Entity> iterator = collidedEntities.iterator();
				while(iterator.hasNext()){
					Entity entity = iterator.next();
					if(entity instanceof BuilderEntity){
						AEntityBase testSource = ((BuilderEntity) entity).entity;
						if(damageSource.equals(testSource)){
							iterator.remove();
						}
					}else if(entity.getRidingEntity() instanceof BuilderEntity){
						AEntityBase testSource = ((BuilderEntity) entity.getRidingEntity()).entity;
						if(damageSource.equals(testSource)){
							iterator.remove();
						}
					}
				}
			}
			
			//Now that all entities have been filtered out, attack all the ones that are left.
			//If we are a client, don't attack.  Simply return the entities.
			if(isClient()){
				Map<WrapperEntity, BoundingBox> entities = new HashMap<WrapperEntity, BoundingBox>();
				for(Entity entity : collidedEntities){
					if(entity instanceof BuilderEntity){
						//Need to check which box we hit for this entity.
						for(BoundingBox box : ((BuilderEntity) entity).entity.collisionBoxes){
							if(box.intersects(damage.box)){
								entities.put(new WrapperEntity(entity), box);
								break;
							}
						}
					}else{
						entities.put(new WrapperEntity(entity), null);
					}
				}
				return entities;
			}
			for(Entity entity : collidedEntities){
				WrapperEntity.attack(entity, damage);
			}
		}
		return null;
	}
	
	/**
	 *  Returns the block wrapper at the passed-in location, or null if the block is air.
	 */
	public WrapperBlock getWrapperBlock(Point3i point){
		return isAir(point) ? null : new WrapperBlock(world, new BlockPos(point.x, point.y, point.z));
	}
	
	/**
	 *  Returns the block at the passed-in location, or null if it doesn't exist in the world.
	 *  Only valid for blocks of type {@link ABlockBase} others will return null.
	 */
	public ABlockBase getBlock(Point3i point){
		Block block = world.getBlockState(new BlockPos(point.x, point.y, point.z)).getBlock();
		return block instanceof BuilderBlock ? ((BuilderBlock) block).block : null;
	}
	
	/**
	 *  Returns true if the block at the passed-in location is solid.  Solid means
	 *  that said block can be collided with, is a cube, and is generally able to have
	 *  things placed or connected to it.
	 */
	public boolean isBlockSolid(Point3i point){
		IBlockState offsetMCState = world.getBlockState(new BlockPos(point.x, point.y, point.z));
		Block offsetMCBlock = offsetMCState.getBlock();
        return offsetMCBlock != null ? !offsetMCBlock.equals(Blocks.BARRIER) && offsetMCState.getMaterial().isOpaque() && offsetMCState.isFullCube() && offsetMCState.getMaterial() != Material.GOURD : false;
	}
	
	/**
	 *  Returns true if the block is liquid.
	 */
	public boolean isBlockLiquid(Point3i point){
		IBlockState offsetMCState = world.getBlockState(new BlockPos(point.x, point.y, point.z));
        return offsetMCState.getMaterial().isLiquid();
	}
	
	/**
	 *  Returns true if the block at the passed-in location is a slab, but only the
	 *  bottom portion of the slab.  May be used to adjust renders to do half-block
	 *  rendering to avoid floating blocks.
	 */
	public boolean isBlockBottomSlab(Point3i point){
		IBlockState state = world.getBlockState(new BlockPos(point.x, point.y, point.z));
		Block block = state.getBlock();
		return block instanceof BlockSlab && !((BlockSlab) block).isDouble() && state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.BOTTOM;
	}
	
	/**
	 *  Returns true if the block at the passed-in location is a slab, but only the
	 *  top portion of the slab.  May be used to adjust renders to do half-block
	 *  rendering to avoid floating blocks.
	 */
	public boolean isBlockTopSlab(Point3i point){
		IBlockState state = world.getBlockState(new BlockPos(point.x, point.y, point.z));
		Block block = state.getBlock();
		return block instanceof BlockSlab && !((BlockSlab) block).isDouble() && state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP;
	}
	
	/**
	 * Updates the blocks and depths of collisions for the passed-in BoundingBox to the box's internal variables.
	 * This is done as it allows for re-use of the variables by the calling object to avoid excess object creation.
	 * Note that if the offset value passed-in for an axis is 0, then no collision checks will be performed on that axis.
	 * This prevents excess calculations when trying to do movement calculations for a single axis.
	 */
	public void updateBoundingBoxCollisions(BoundingBox box, Point3d collisionMotion){
		AxisAlignedBB mcBox = new AxisAlignedBB(
			box.globalCenter.x - box.widthRadius,
			box.globalCenter.y - box.heightRadius,
			box.globalCenter.z - box.depthRadius,
			box.globalCenter.x + box.widthRadius,
			box.globalCenter.y + box.heightRadius,
			box.globalCenter.z + box.depthRadius
		);
		List<AxisAlignedBB> collidingAABBs = new ArrayList<AxisAlignedBB>(); 
		box.currentCollisionDepth.set(0D, 0D, 0D);
		for(int i = (int) Math.floor(mcBox.minX); i < Math.floor(mcBox.maxX + 1); ++i){
    		for(int j = (int) Math.floor(mcBox.minY); j < Math.floor(mcBox.maxY + 1); ++j){
    			for(int k = (int) Math.floor(mcBox.minZ); k < Math.floor(mcBox.maxZ + 1); ++k){
    				BlockPos pos = new BlockPos(i, j, k);
    				if(world.isBlockLoaded(pos)){
	    				IBlockState state = world.getBlockState(pos);
	    				if(state.getBlock().canCollideCheck(state, false) && state.getCollisionBoundingBox(world, pos) != null){
	    					state.addCollisionBoxToList(world, pos, mcBox, collidingAABBs, null, false);
	    					box.collidingBlocks.add(new WrapperBlock(world, pos));
	    				}
						if(box.collidesWithLiquids && state.getMaterial().isLiquid()){
							collidingAABBs.add(state.getBoundingBox(world, pos).offset(pos));
							box.collidingBlocks.add(new WrapperBlock(world, pos));
						}
    				}
    			}
    		}
    	}
		
		for(AxisAlignedBB colBox : collidingAABBs){
			if(collisionMotion.x > 0){
				double testDepthX = mcBox.maxX - colBox.minX;
				if(testDepthX < Math.abs(collisionMotion.x)){
					box.currentCollisionDepth.x = Math.max(box.currentCollisionDepth.x, testDepthX);
				}
			}else if(collisionMotion.x < 0){
				double testDepthX = colBox.maxX - mcBox.minX;
				if(testDepthX < Math.abs(collisionMotion.x)){
					box.currentCollisionDepth.x = Math.max(box.currentCollisionDepth.x, testDepthX);
				}
			}
			if(collisionMotion.y > 0){
				double testDepthY = mcBox.maxY - colBox.minY;
				if(testDepthY < Math.abs(collisionMotion.y)){
					box.currentCollisionDepth.y = Math.max(box.currentCollisionDepth.y, testDepthY);
				}
			}else if(collisionMotion.y < 0){
				double testDepthY = colBox.maxY - mcBox.minY;
				if(testDepthY < Math.abs(collisionMotion.y)){
					box.currentCollisionDepth.y = Math.max(box.currentCollisionDepth.y, testDepthY);
				}
			}
			if(collisionMotion.z > 0){
				double testDepthZ = colBox.maxZ - mcBox.minZ;
				if(testDepthZ < Math.abs(collisionMotion.z)){
					box.currentCollisionDepth.z = Math.max(box.currentCollisionDepth.z, testDepthZ);
				}
			}else if(collisionMotion.z < 0){
				double testDepthZ = colBox.maxZ - mcBox.minZ;
				if(testDepthZ < Math.abs(collisionMotion.z)){
					box.currentCollisionDepth.z = Math.max(box.currentCollisionDepth.z, testDepthZ);
				}
			}
		}
	}
	
	/**
	 *  Returns the current redstone power at the passed-in position.
	 */
	public int getRedstonePower(Point3i point){
		return world.getStrongPower(new BlockPos(point.x, point.y, point.z));
	}

	/**
	 *  Returns the rain strength at the passed-in position.
	 *  0 is no rain, 1 is rain, and 2 is a thunderstorm.
	 */
	public float getRainStrength(Point3i point){
		return world.isRainingAt(new BlockPos(point.x, point.y, point.z)) ? world.getRainStrength(1.0F) + world.getThunderStrength(1.0F) : 0.0F;
	}
	
	/**
	 *  Returns the current temperature at the passed-in position.
	 *  Dependent on biome, and likely modified by mods that add new boimes.
	 */
	public float getTemperature(Point3i point){
		BlockPos pos = new BlockPos(point.x, point.y, point.z);
		return world.getBiome(pos).getTemperature(pos);
	}

    /**
	 *  Has the player place the passed-in block at the point specified.
	 *  Returns true if the block was placed, false if not.
	 */
    @SuppressWarnings("unchecked")
	public <JSONDefinition extends AJSONItem<? extends AJSONItem<?>.General>> boolean setBlock(ABlockBase block, Point3i location, WrapperPlayer player, Axis axis){
    	if(!world.isRemote){
	    	BuilderBlock wrapper = BuilderBlock.blockWrapperMap.get(block);
	    	ItemStack stack = player.getHeldStack();
	    	BlockPos pos = new BlockPos(location.x, location.y, location.z);
	    	EnumFacing facing = EnumFacing.valueOf(axis.name());
	    	if(!world.getBlockState(pos).getBlock().isReplaceable(world, pos)){
	            pos = pos.offset(facing);
	            location.add(facing.getFrontOffsetX(), facing.getFrontOffsetY(), facing.getFrontOffsetZ());
	        }
	    	if(!stack.isEmpty() && player.player.canPlayerEdit(pos, facing, stack) && world.mayPlace(wrapper, pos, false, facing, null)){
	            IBlockState newState = wrapper.getStateForPlacement(world, pos, facing, 0, 0, 0, 0, player.player, EnumHand.MAIN_HAND);
	            if(world.setBlockState(pos, newState, 11)){
	            	//Block is set.  See if we need to set TE data.
	            	if(block instanceof IBlockTileEntity){
	            		ATileEntityBase<JSONDefinition> tile = (ATileEntityBase<JSONDefinition>) getTileEntity(location);
	            		if(stack.hasTagCompound()){
	            			tile.load(new WrapperNBT(stack.getTagCompound()));
	            		}else{
	            			tile.setDefinition(((AItemPack<JSONDefinition>) stack.getItem()).definition);
	            		}
	            	}
	            	//Send place event to block class, and also send initial update cheeck.
	            	block.onPlaced(this, location, player);
	                stack.shrink(1);
	            }
	            return true;
	        }
    	}
    	return false;
    }
	
	/**
	 *  Returns the tile entity at the passed-in location, or null if it doesn't exist in the world.
	 *  Only valid for TEs of type {@link ATileEntityBase} others will return null.
	 */
	public ATileEntityBase<?> getTileEntity(Point3i point){
		TileEntity tile = world.getTileEntity(new BlockPos(point.x, point.y, point.z));
		return tile instanceof BuilderTileEntity ? ((BuilderTileEntity<?>) tile).tileEntity : null;
	}
	
	/**
	 *  Flags the tile entity at the passed-in point for saving.  This means the TE's
	 *  NBT data will be saved to disk when the chunk unloads so it will maintain its state.
	 */
	public void markTileEntityChanged(Point3i point){
		world.getTileEntity(new BlockPos(point.x, point.y, point.z)).markDirty();
	}
	
	/**
	 *  Gets the brightness at this point, as a value between 0.0-1.0. Calculated from the
	 *  sun brightness, and possibly the block brightness if calculateBlock is true.
	 */
	public float getLightBrightness(Point3i point, boolean calculateBlock){
		BlockPos pos = new BlockPos(point.x, point.y, point.z);
		float sunLight = world.getSunBrightness(0)*(world.getLightFor(EnumSkyBlock.SKY, pos) - world.getSkylightSubtracted())/15F;
		float blockLight = calculateBlock ? world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, pos)/15F : 0.0F;
		return Math.max(sunLight, blockLight);
	}
	
	/**
	 *  Updates the brightness of the block at this point.  Only works if the block
	 *  is a dynamic-brightness block that implements {@link ITileEntityProvider}. 
	 */
	public void updateLightBrightness(Point3i point){
		ATileEntityBase<?> tile = getTileEntity(point);
		if(tile != null){
			BlockPos pos = new BlockPos(point.x, point.y, point.z);
			//This needs to get fired manually as even if we update the blockstate the light value won't change
			//as the actual state of the block doesn't change, so MC doesn't think it needs to do any lighting checks.
			world.checkLight(pos);
		}
	}
	
	/**
	 *  Sets a fake light block at the passed-in position.
	 *  Only sets the fake light if the block at the passed-in position is air.
	 *  Make sure you track this position and remove the light when it's not in-use! 
	 */
	public void setFakeLight(Point3i point){
		BlockPos pos = new BlockPos(point.x, point.y, point.z);
		if(world.isAirBlock(pos)){
			world.setBlockState(pos, BuilderBlockFakeLight.instance.getDefaultState());
		}
	}
	
	/**
	 *  Destroys the block at the position, dropping it as whatever drop it drops as.
	 *  This does no sanity checks, so make sure you're
	 *  actually allowed to do such a thing before calling.
	 */
	public void destroyBlock(Point3i point){
		world.destroyBlock(new BlockPos(point.x, point.y, point.z), true);
	}
	
	/**
	 *  Returns true if the block at this point is air.
	 */
	public boolean isAir(Point3i point){
		BlockPos pos = new BlockPos(point.x, point.y, point.z);
		IBlockState state = world.getBlockState(pos); 
		Block block = state.getBlock();
		return block.isAir(state, world, pos);
	}
	
	/**
	 *  Sets the block at the passed-in position to air. 
	 *  This does no sanity checks, so make sure you're
	 *  actually allowed to do such a thing before calling.
	 */
	public void setToAir(Point3i point){
		world.setBlockToAir(new BlockPos(point.x, point.y, point.z));
	}
	
	/**
	 *  Returns true if the block at this point is fire.
	 *  Note: this will return true on vanilla fire, as well as
	 *  any other blocks made of fire from other mods.
	 */
	public boolean isFire(Point3i point){
		BlockPos pos = new BlockPos(point.x, point.y, point.z);
		IBlockState state = world.getBlockState(pos); 
		return state.getMaterial().equals(Material.FIRE);
	}
	
	/**
	 *  Sets the block at the passed-in position to fire. 
	 *  This does no sanity checks, so make sure you're
	 *  actually allowed to do such a thing before calling.
	 */
	public void setToFire(Point3i point){
		world.setBlockState(new BlockPos(point.x, point.y, point.z), Blocks.FIRE.getDefaultState());
	}
	
	/**
	 *  Spawns the passed-in ItemStack as an item entity at the passed-in point.
	 *  This should be called only on servers, as spawning items on clients
	 *  leads to phantom items that can't be picked up. 
	 */
	public void spawnItemStack(ItemStack stack, WrapperNBT data, Point3d point){
		//TODO this goes away when we get wrapper ItemStacks.
		stack.setTagCompound(data.tag);
		world.spawnEntity(new EntityItem(world, point.x, point.y, point.z, stack));
	}
	
	/**
	 *  Spawns an explosion of the specified strength at the passed-in point.
	 *  Explosion in this case is from an entity.
	 */
	public void spawnExplosion(AEntityBase source, Point3d location, double strength, boolean flames){
		world.newExplosion(source.builder, location.x, location.y, location.z, (float) strength, flames, true);
	}
	
	/**
	 *  Spawns an explosion of the specified strength at the passed-in point.
	 *  Explosion in this case is from the player.
	 */
	public void spawnExplosion(WrapperPlayer player, Point3d location, double strength, boolean flames){
		world.newExplosion(player.player, location.x, location.y, location.z, (float) strength, flames, true);
	}
}