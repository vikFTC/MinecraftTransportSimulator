package minecrafttransportsimulator.dataclasses;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import minecrafttransportsimulator.MTS;
import minecrafttransportsimulator.blocks.core.BlockRotatable;
import minecrafttransportsimulator.items.blocks.ItemBlockRotatable;
import minecrafttransportsimulator.items.core.ItemWrench;
import minecrafttransportsimulator.packets.control.AileronPacket;
import minecrafttransportsimulator.packets.control.BrakePacket;
import minecrafttransportsimulator.packets.control.ElevatorPacket;
import minecrafttransportsimulator.packets.control.FlapPacket;
import minecrafttransportsimulator.packets.control.HornPacket;
import minecrafttransportsimulator.packets.control.LightPacket;
import minecrafttransportsimulator.packets.control.ReverseThrustPacket;
import minecrafttransportsimulator.packets.control.RudderPacket;
import minecrafttransportsimulator.packets.control.ShiftPacket;
import minecrafttransportsimulator.packets.control.SirenPacket;
import minecrafttransportsimulator.packets.control.SteeringPacket;
import minecrafttransportsimulator.packets.control.ThrottlePacket;
import minecrafttransportsimulator.packets.control.TrailerPacket;
import minecrafttransportsimulator.packets.control.TrimPacket;
import minecrafttransportsimulator.packets.general.PacketBulletHit;
import minecrafttransportsimulator.packets.general.PacketChat;
import minecrafttransportsimulator.packets.general.PacketManualPageUpdate;
import minecrafttransportsimulator.packets.general.PacketPackReload;
import minecrafttransportsimulator.packets.general.PacketPlayerCrafting;
import minecrafttransportsimulator.packets.parts.PacketPartEngineDamage;
import minecrafttransportsimulator.packets.parts.PacketPartEngineLinked;
import minecrafttransportsimulator.packets.parts.PacketPartEngineSignal;
import minecrafttransportsimulator.packets.parts.PacketPartGroundDeviceWheelFlat;
import minecrafttransportsimulator.packets.parts.PacketPartGunReload;
import minecrafttransportsimulator.packets.parts.PacketPartGunSignal;
import minecrafttransportsimulator.packets.parts.PacketPartSeatRiderChange;
import minecrafttransportsimulator.packets.tileentities.PacketFuelPumpConnection;
import minecrafttransportsimulator.packets.tileentities.PacketFuelPumpFillDrain;
import minecrafttransportsimulator.packets.tileentities.PacketSignChange;
import minecrafttransportsimulator.packets.tileentities.PacketTileEntityClientServerHandshake;
import minecrafttransportsimulator.packets.tileentities.PacketTrafficSignalControllerChange;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleAttacked;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleClientInit;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleClientInitResponse;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleClientPartAddition;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleClientPartRemoval;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleDeltas;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleInstruments;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleInteracted;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleJerrycan;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleKey;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleNameTag;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleWindowBreak;
import minecrafttransportsimulator.packets.vehicles.PacketVehicleWindowFix;
import minecrafttransportsimulator.vehicles.main.EntityVehicleG_Blimp;
import minecrafttransportsimulator.vehicles.main.EntityVehicleG_Boat;
import minecrafttransportsimulator.vehicles.main.EntityVehicleG_Car;
import minecrafttransportsimulator.vehicles.main.EntityVehicleG_Plane;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

/**Main registry class.  This class should be referenced by any class looking for
 * MTS items or blocks.  Adding new items and blocks is a simple as adding them
 * as a field; the {@link mts_to_mc.interfaces.RegistryInterface} will handle the rest.
 * All entries will be ordered according to the order in which they were declared,
 * so use this to sort your entries.
 * 
 * @author don_bruce
 */
@Mod.EventBusSubscriber
public final class MTSRegistry{
	/**All registered core items are stored in this list as they are added.  Used to sort items in the creative tab.**/
	public static List<Item> itemList = new ArrayList<Item>();
	
	/**Maps rotatable blocks to their items.  Used to return the correct item when they are broken.*/
	public static Map<BlockRotatable, ItemBlockRotatable> rotatableItemMap = new LinkedHashMap<BlockRotatable, ItemBlockRotatable>();
	
	/**Core creative tab for base MTS items**/
	public static final CreativeTabCore coreTab = new CreativeTabCore();
	
	/**Map of creative tabs for packs.  Keyed by pack IDs.  Populated by the {@link PackParserSystem}**/
	public static final Map<String, CreativeTabPack> packTabs = new HashMap<String, CreativeTabPack>();

	//Vehicle interaction items.
	//public static final Item manual = new ItemManual().setCreativeTab(coreTab);
	public static final Item wrench = new ItemWrench().setCreativeTab(coreTab);
	//public static final Item key = new ItemKey().setCreativeTab(coreTab);
	//public static final Item jumperCable = new ItemJumperCable().setCreativeTab(coreTab);
	//public static final Item jerrycan = new ItemJerrycan().setCreativeTab(coreTab);
	
	//Crafting benches.
	//public static final Item vehicleBench = new ItemBlockBench("plane", "car", "blimp", "boat").createBlocks();
	//public static final Item propellerBench = new ItemBlockBench("propeller").createBlocks();
	//public static final Item engineBench = new ItemBlockBench("engine_aircraft", "engine_jet", "engine_car", "engine_boat").createBlocks();
	//public static final Item wheelBench = new ItemBlockBench("wheel", "skid", "pontoon", "tread").createBlocks();
	//public static final Item seatBench = new ItemBlockBench("seat", "crate", "barrel", "crafting_table", "furnace", "brewing_stand").createBlocks();
	//public static final Item gunBench = new ItemBlockBench("gun_fixed", "gun_tripod").createBlocks();
	//public static final Item customBench = new ItemBlockBench("custom").createBlocks();
	//public static final Item instrumentBench = new ItemBlockBench("instrument").createBlocks();
	//public static final Item componentBench = new ItemBlockBench("item").createBlocks();
	
	//Fuel pump.
	//public static final Item fuelPump = new ItemBlockFuelPump().createBlocks();
	
	//Traffic Controller
	///public static final Item trafficSignalController = new ItemBlockTrafficSignalController().createBlocks();
	
	//Pole-based blocks.
	//public static final Block pole = new BlockPoleNormal(0.125F);
	//public static final Item itemBlockPole = new ItemBlock(pole);
	//public static final Block poleBase = new BlockPoleWallConnector(0.125F);
	//public static final Item itemBlockPoleBase = new ItemBlock(poleBase);
	//public static final Block trafficSignal = new BlockPoleAttachment(0.125F);
	//public static final Item itemBlockTrafficSignal = new ItemBlock(trafficSignal);
	//public static final Block streetLight = new BlockPoleAttachment(0.125F);
	//public static final Item itemBlockStreetLight = new ItemBlock(streetLight);
	//public static final Block trafficSign = new BlockPoleSign(0.125F);
	//public static final Item itemBlockTrafficSign = new ItemBlock(trafficSign);
		
	//Decor blocks.
	//public static final Block decorBasicDark = new BlockDecor(false, false);
	//public static final Block decorOrientedDark = new BlockDecor(true, false);
	//public static final Block decorBasicLight = new BlockDecor(false, true);
	//public static final Block decorOrientedLight = new BlockDecor(true, true);
	
	//Counters for registry systems.
	private static int entityNumber = 0;
	private static int packetNumber = 0;
	
	
	/**All run-time things go here.**/
	public static void init(){
		initEntities();
		initPackets();
	}
	
	/**
	 * Registers all blocks present in this class, as well as blocks for rotatable items.
	 * Also adds the respective TileEntity if the block has one.
	 */
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event){
		//Need to keep track of which TE classes we've registered so we don't double-register them for blocks that use the same TE.
		List<Class<? extends TileEntity>> registeredTileEntityClasses = new ArrayList<Class<? extends TileEntity>>();
		for(Field field : MTSRegistry.class.getFields()){
			if(field.getType().equals(Block.class)){
				try{
					Block block = (Block) field.get(null);
					String name = field.getName().toLowerCase();
					event.getRegistry().register(block.setRegistryName(name).setUnlocalizedName(name));
					if(block instanceof ITileEntityProvider){
						Class<? extends TileEntity> tileEntityClass = ((ITileEntityProvider) block).createNewTileEntity(null, 0).getClass();
						if(!registeredTileEntityClasses.contains(tileEntityClass)){
							GameRegistry.registerTileEntity(tileEntityClass, new ResourceLocation(MTS.MODID, tileEntityClass.getSimpleName()));
							registeredTileEntityClasses.add(tileEntityClass);
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}else if(field.getType().equals(Item.class)){
				try{
					if(field.get(null) instanceof ItemBlockRotatable){
						ItemBlockRotatable item = (ItemBlockRotatable) field.get(null);
						for(byte i=0; i<item.blocks.length; ++i){
							BlockRotatable block = item.blocks[i];
							String name = field.getName().toLowerCase() + "_" + i;
							event.getRegistry().register(block.setRegistryName(name).setUnlocalizedName(name));
							rotatableItemMap.put(block, item);
							if(block instanceof ITileEntityProvider){
								Class<? extends TileEntity> tileEntityClass = ((ITileEntityProvider) block).createNewTileEntity(null, 0).getClass();
								if(!registeredTileEntityClasses.contains(tileEntityClass)){
									GameRegistry.registerTileEntity(tileEntityClass, new ResourceLocation(MTS.MODID, tileEntityClass.getSimpleName()));
									registeredTileEntityClasses.add(tileEntityClass);
								}
							}
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Registers all entities with the entity registry.
	 * For vehicles we only register the main classes as
	 * the pack data stored in NBT is what makes for different vehicles.
	 */
	private static void initEntities(){
		EntityRegistry.registerModEntity(new ResourceLocation(MTS.MODID, EntityVehicleG_Car.class.getSimpleName().substring(6).toLowerCase()), EntityVehicleG_Car.class, "vehiclecar", entityNumber++, MTS.MODID, 256, 5, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MTS.MODID, EntityVehicleG_Boat.class.getSimpleName().substring(6).toLowerCase()), EntityVehicleG_Boat.class, "vehicleboat", entityNumber++, MTS.MODID, 256, 5, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MTS.MODID, EntityVehicleG_Plane.class.getSimpleName().substring(6).toLowerCase()), EntityVehicleG_Plane.class, "vehicleplane", entityNumber++, MTS.MODID, 256, 5, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MTS.MODID, EntityVehicleG_Blimp.class.getSimpleName().substring(6).toLowerCase()), EntityVehicleG_Blimp.class, "vehicleblimp", entityNumber++, MTS.MODID, 256, 5, false);
	}
	
	private static void initPackets(){
		//Packets in packets.control
		registerPacket(AileronPacket.class, AileronPacket.Handler.class, true, true);
		registerPacket(BrakePacket.class, BrakePacket.Handler.class, true, true);
		registerPacket(ElevatorPacket.class, ElevatorPacket.Handler.class, true, true);
		registerPacket(FlapPacket.class, FlapPacket.Handler.class, true, true);
		registerPacket(HornPacket.class, HornPacket.Handler.class, true, true);
		registerPacket(LightPacket.class, LightPacket.Handler.class, true, true);
		registerPacket(ReverseThrustPacket.class, ReverseThrustPacket.Handler.class, true, true);
		registerPacket(RudderPacket.class, RudderPacket.Handler.class, true, true);
		registerPacket(SirenPacket.class, SirenPacket.Handler.class, true, true);
		registerPacket(ShiftPacket.class, ShiftPacket.Handler.class, true, true);
		registerPacket(SteeringPacket.class, SteeringPacket.Handler.class, true, true);
		registerPacket(ThrottlePacket.class, ThrottlePacket.Handler.class, true, true);
		registerPacket(TrailerPacket.class, TrailerPacket.Handler.class, true, true);
		registerPacket(TrimPacket.class, TrimPacket.Handler.class, true, true);
		
		//Packets in packets.general
		registerPacket(PacketBulletHit.class, PacketBulletHit.Handler.class, true, true);
		registerPacket(PacketChat.class, PacketChat.Handler.class, true, false);
		registerPacket(PacketPartGunReload.class, PacketPartGunReload.Handler.class, true, false);
		registerPacket(PacketManualPageUpdate.class, PacketManualPageUpdate.Handler.class, false, true);
		registerPacket(PacketPackReload.class, PacketPackReload.Handler.class, false, true);
		registerPacket(PacketPlayerCrafting.class, PacketPlayerCrafting.Handler.class, false, true);
		
		//Packets in packets.tileentity
		registerPacket(PacketFuelPumpConnection.class, PacketFuelPumpConnection.Handler.class, true, false);
		registerPacket(PacketFuelPumpFillDrain.class, PacketFuelPumpFillDrain.Handler.class, true, false);
		registerPacket(PacketSignChange.class, PacketSignChange.Handler.class, true, true);
		registerPacket(PacketTileEntityClientServerHandshake.class, PacketTileEntityClientServerHandshake.Handler.class, true, true);
		registerPacket(PacketTrafficSignalControllerChange.class, PacketTrafficSignalControllerChange.Handler.class, true, true);
		
		//Packets in packets.vehicles.
		registerPacket(PacketVehicleAttacked.class, PacketVehicleAttacked.Handler.class, false, true);
		registerPacket(PacketVehicleClientInit.class, PacketVehicleClientInit.Handler.class, false, true);
		registerPacket(PacketVehicleClientInitResponse.class, PacketVehicleClientInitResponse.Handler.class, true, false);
		registerPacket(PacketVehicleClientPartAddition.class, PacketVehicleClientPartAddition.Handler.class, true, false);
		registerPacket(PacketVehicleClientPartRemoval.class, PacketVehicleClientPartRemoval.Handler.class, true, false);
		registerPacket(PacketVehicleDeltas.class, PacketVehicleDeltas.Handler.class, true, false);
		registerPacket(PacketVehicleInstruments.class, PacketVehicleInstruments.Handler.class, true, true);
		registerPacket(PacketVehicleInteracted.class, PacketVehicleInteracted.Handler.class, false, true);
		registerPacket(PacketVehicleJerrycan.class, PacketVehicleJerrycan.Handler.class, true, false);
		registerPacket(PacketVehicleKey.class, PacketVehicleKey.Handler.class, true, false);
		registerPacket(PacketVehicleNameTag.class, PacketVehicleNameTag.Handler.class, true, false);
		registerPacket(PacketVehicleWindowBreak.class, PacketVehicleWindowBreak.Handler.class, true, false);
		registerPacket(PacketVehicleWindowFix.class, PacketVehicleWindowFix.Handler.class, true, false);
		
		//Packets in packets.parts
		registerPacket(PacketPartEngineDamage.class, PacketPartEngineDamage.Handler.class, true, false);
		registerPacket(PacketPartEngineLinked.class, PacketPartEngineLinked.Handler.class, true, false);
		registerPacket(PacketPartEngineSignal.class, PacketPartEngineSignal.Handler.class, true, true);
		registerPacket(PacketPartGroundDeviceWheelFlat.class, PacketPartGroundDeviceWheelFlat.Handler.class, true, false);
		registerPacket(PacketPartGunSignal.class, PacketPartGunSignal.Handler.class, true, true);
		registerPacket(PacketPartSeatRiderChange.class, PacketPartSeatRiderChange.Handler.class, true, false);
	}

	/**
	 * Registers a packet and its handler on the client and/or the server.
	 * @param packetClass
	 * @param handlerClass
	 * @param client
	 * @param server
	 */
	private static <REQ extends IMessage, REPLY extends IMessage> void registerPacket(Class<REQ> packetClass, Class<? extends IMessageHandler<REQ, REPLY>> handlerClass, boolean client, boolean server){
		if(client)MTS.MTSNet.registerMessage(handlerClass, packetClass, ++packetNumber, Side.CLIENT);
		if(server)MTS.MTSNet.registerMessage(handlerClass, packetClass, ++packetNumber, Side.SERVER);
	}
}
