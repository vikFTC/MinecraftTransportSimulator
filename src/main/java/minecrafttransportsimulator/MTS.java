package minecrafttransportsimulator;

import java.io.File;

import minecrafttransportsimulator.dataclasses.MTSRegistry;
import minecrafttransportsimulator.packs.PackLoader;
import minecrafttransportsimulator.systems.ConfigSystem;
import mts_to_mc.interfaces.FileInterface;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(modid = MTS.MODID, name = MTS.MODNAME, version = MTS.MODVER)
public class MTS {
	public static final String MODID="mts";
	public static final String MODNAME="Minecraft Transport Simulator";
	public static final String MODVER="15.5.0";
	
	@Instance(value = MTS.MODID)
	public static MTS instance;
	
	public static File minecraftDir;
	public static final SimpleNetworkWrapper MTSNet = NetworkRegistry.INSTANCE.newSimpleChannel("MTSNet");
	@SidedProxy(clientSide="minecrafttransportsimulator.ClientProxy", serverSide="minecrafttransportsimulator.CommonProxy")
	public static CommonProxy proxy;
	
	public MTS(){
		FluidRegistry.enableUniversalBucket();
		PackLoader.init();
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event){
		proxy.initConfig(event.getSuggestedConfigurationFile());
		proxy.initControls();
		minecraftDir = new File(event.getModConfigurationDirectory().getParent());
		FileInterface.initLog(event);
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event){
		MTSRegistry.init();
		ConfigSystem.initFuels();
	}
}
