package minecrafttransportsimulator.blocks.tileentities.instances;

import minecrafttransportsimulator.blocks.components.IBlockTileEntity;
import minecrafttransportsimulator.blocks.tileentities.components.ATileEntityBase;
import minecrafttransportsimulator.blocks.tileentities.components.ATileEntityFluidTank;
import minecrafttransportsimulator.packets.instances.PacketPlayerChatMessage;
import minecrafttransportsimulator.packets.instances.PacketPumpConnection;
import minecrafttransportsimulator.rendering.blocks.ARenderTileEntityBase;
import minecrafttransportsimulator.rendering.blocks.RenderFuelPump;
import minecrafttransportsimulator.vehicles.main.EntityVehicleE_Powered;
import minecrafttransportsimulator.wrappers.WrapperNBT;
import minecrafttransportsimulator.wrappers.WrapperNetwork;

public class TileEntityFuelPump extends ATileEntityFluidTank{
    public EntityVehicleE_Powered connectedVehicle;
    public int totalTransfered;
	
	@Override
	public void update(){
		if(connectedVehicle != null){
			//Don't fuel vehicles that don't exist.
			if(connectedVehicle.isDead){
				connectedVehicle = null;
				return;
			}
			//Check distance to make sure the vehicle hasn't moved away.
			//Only check on servers, clients will get a packet to disconnect.
			if(!world.isClient() && connectedVehicle.getDistance(position.x, position.y, position.z) > 20){
				connectedVehicle = null;
				WrapperNetwork.sendToAllClients(new PacketPumpConnection(this));
				WrapperNetwork.sendToClientsNear(new PacketPlayerChatMessage("interact.fuelpump.toofar"), world.getDimensionID(), position, 25);
				return;
			}
			//If we have room for fuel, try to add it to the vehicle.
			if(connectedVehicle.definition.motorized.fuelCapacity - connectedVehicle.fuel >= 10){
				if(!getFluid().isEmpty()){
					connectedVehicle.fluidName = getFluid();
					int fuelDrained = drain(getFluid(), 10, true);
					connectedVehicle.fuel += fuelDrained;
					totalTransfered += fuelDrained;
				}else if(!world.isClient()){
					//On a server without fuel in the pump.  Disconnect vehicle.
					//Don't want to disconnect vehicle on clients as we might have packet lag.
					connectedVehicle = null;
					WrapperNetwork.sendToAllClients(new PacketPumpConnection(this));
					WrapperNetwork.sendToClientsNear(new PacketPlayerChatMessage("interact.fuelpump.empty"), world.getDimensionID(), position, 16);
				}
			}else{
				//No more room in the vehicle.  Disconnect.
				connectedVehicle = null;
				if(!world.isClient()){
					WrapperNetwork.sendToClientsNear(new PacketPlayerChatMessage("interact.fuelpump.complete"), world.getDimensionID(), position, 16);
				}
			}
		}
	}
	
	@Override
	public int getMaxLevel(){
		return 15000;
	}

	@Override
	public ARenderTileEntityBase<? extends ATileEntityBase, ? extends IBlockTileEntity> getRenderer(){
		return new RenderFuelPump();
	}
	
	@Override
	public void load(WrapperNBT data){
		super.load(data);
		totalTransfered = data.getInteger("totalTransfered");
	}
	
	@Override
	public void save(WrapperNBT data){
		super.save(data);
		data.setInteger("totalTransfered", totalTransfered);
	}
}