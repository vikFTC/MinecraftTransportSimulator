package minecrafttransportsimulator.guis.instances;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import mcinterface.BuilderGUI;
import mcinterface.InterfaceNetwork;
import minecrafttransportsimulator.baseclasses.Point3i;
import minecrafttransportsimulator.blocks.tileentities.components.ATileEntityBase;
import minecrafttransportsimulator.blocks.tileentities.components.ATileEntityPole_Component;
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityPole;
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityPole_StreetLight;
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntityPole_TrafficSignal;
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntitySignalController;
import minecrafttransportsimulator.blocks.tileentities.instances.TileEntitySignalController.OpMode;
import minecrafttransportsimulator.guis.components.AGUIBase;
import minecrafttransportsimulator.guis.components.GUIComponentButton;
import minecrafttransportsimulator.guis.components.GUIComponentItem;
import minecrafttransportsimulator.guis.components.GUIComponentLabel;
import minecrafttransportsimulator.guis.components.GUIComponentTextBox;
import minecrafttransportsimulator.packets.instances.PacketTileEntitySignalControllerChange;

public class GUISignalController extends AGUIBase{
	
	//Buttons.
	private GUIComponentButton orientationButton;
	private GUIComponentButton modeButton;
	private GUIComponentButton confirmButton;
	
	//Input boxes
	private GUIComponentTextBox scanDistanceText;
	private GUIComponentTextBox greenMainTimeText;
	private GUIComponentTextBox greenCrossTimeText;
	private GUIComponentTextBox yellowMainTimeText;
	private GUIComponentTextBox yellowCrossTimeText;
	private GUIComponentTextBox allRedTimeText;
	
	//Labels and items for scan results.
	private byte trafficSignals;
	private byte streetLights;
	
	private GUIComponentLabel trafficSignalCount;
	private GUIComponentLabel streetLightCount;
	private GUIComponentItem trafficSignalItem;
	private GUIComponentItem streetLightItem;
	
	//These are only used to save the item names until we create the item components.
	private String trafficSignalItemNameTemp;
	private String streetLightItemNameTemp;
	
	
	//Internal controller locations point list.
	//We set this to prevent the player from seeing changes on their clients.
	//If we did set the actual locations here, there'd be de-syncs if the player didn't hit confirm.
	private final Set<Point3i> componentLocations = new HashSet<Point3i>();
	
	private final TileEntitySignalController controller;
	
	public GUISignalController(TileEntitySignalController controller){
		this.controller = controller;
		
		//Set initial signal counts.
		for(Point3i location : controller.componentLocations){
			ATileEntityBase<?> tile = controller.world.getTileEntity(location);
			if(tile instanceof TileEntityPole){
				for(ATileEntityPole_Component component : ((TileEntityPole) tile).components.values()){
					if(component instanceof TileEntityPole_TrafficSignal){
						trafficSignalItemNameTemp = component.definition.packID + ":" + component.definition.systemName;
						++trafficSignals;
						componentLocations.add(location);
					}else if(component instanceof TileEntityPole_StreetLight){
						streetLightItemNameTemp = component.definition.packID + ":" + component.definition.systemName;
						++streetLights;
						componentLocations.add(location);
					}
				}
			}
		}
	}
	
	@Override 
	public void setupComponents(int guiLeft, int guiTop){
		addButton(new GUIComponentButton(guiLeft + 25, guiTop + 15, 200, BuilderGUI.translate("gui.trafficsignalcontroller.scan")){
			public void onClicked(){
				trafficSignals = 0;
				streetLights = 0;
				componentLocations.clear();
				int scanDistance = Integer.valueOf(scanDistanceText.getText());
				for(int i=controller.position.x-scanDistance; i<=controller.position.x+scanDistance; ++i){
					for(int j=controller.position.y-scanDistance; j<=controller.position.y+scanDistance; ++j){
						for(int k=controller.position.z-scanDistance; k<=controller.position.z+scanDistance; ++k){
							Point3i location = new Point3i(i, j, k);
							ATileEntityBase<?> tile = controller.world.getTileEntity(location);
							if(tile instanceof TileEntityPole){
								for(ATileEntityPole_Component component : ((TileEntityPole) tile).components.values()){
									if(component instanceof TileEntityPole_TrafficSignal){
										trafficSignalItem.itemName = component.definition.packID + ":" + component.definition.systemName;
										++trafficSignals;
										componentLocations.add(location);
									}else if(component instanceof TileEntityPole_StreetLight){
										streetLightItem.itemName = component.definition.packID + ":" + component.definition.systemName;
										++streetLights;
										componentLocations.add(location);
									}
								}
							}
						}
					}
				}
			}
		});
		addTextBox(scanDistanceText = new GUIComponentTextBox(guiLeft + 120, guiTop + 40, 40, "25", 10, Color.WHITE, Color.BLACK, 2));
		addLabel(new GUIComponentLabel(guiLeft + 30, scanDistanceText.y, Color.WHITE, BuilderGUI.translate("gui.trafficsignalcontroller.scandistance")).setBox(scanDistanceText));
		addLabel(new GUIComponentLabel(guiLeft + 30, guiTop + 55, Color.WHITE, BuilderGUI.translate("gui.trafficsignalcontroller.scanfound")));
		
		//Traffic signal scan results.
		addItem(trafficSignalItem = new GUIComponentItem(guiLeft + 120, guiTop + 52, 1.0F, trafficSignalItemNameTemp, 1, -1));
		addLabel(trafficSignalCount = new GUIComponentLabel(guiLeft + 135, guiTop + 56, Color.WHITE, " X" + trafficSignals));
		
		//Street lamp scan results.
		addItem(streetLightItem = new GUIComponentItem(guiLeft + 200, guiTop + 52, 1.0F, streetLightItemNameTemp, 1, -1));
		addLabel(streetLightCount = new GUIComponentLabel(guiLeft + 215, guiTop + 56, Color.WHITE, " X" + streetLights));
		
		
		addButton(orientationButton = new GUIComponentButton(guiLeft + 125, guiTop + 70, 100, controller.mainDirectionXAxis ? "X" : "Z", 15, true){
			@Override
			public void onClicked(){
				if(controller.mainDirectionXAxis){
					controller.mainDirectionXAxis = false;
					this.text = "Z";
				}else{
					controller.mainDirectionXAxis = true;
					this.text = "X";
				}
			}
		});
		addLabel(new GUIComponentLabel(guiLeft + 30, orientationButton.y + 5, Color.WHITE, BuilderGUI.translate("gui.trafficsignalcontroller.primary")).setButton(orientationButton));
		
		addButton(modeButton = new GUIComponentButton(guiLeft + 125, guiTop + 85, 100, BuilderGUI.translate("gui.trafficsignalcontroller." + (controller.currentOpMode.equals(OpMode.VEHICLE_TRIGGER) ? "modetrigger" : (controller.currentOpMode.equals(OpMode.TIMED_CYCLE) ? "modetime" : "moderemote"))), 15, true){
			@Override
			public void onClicked(){
				if(controller.currentOpMode.equals(OpMode.VEHICLE_TRIGGER)){
					controller.currentOpMode = OpMode.TIMED_CYCLE;
					this.text = BuilderGUI.translate("gui.trafficsignalcontroller.modetime");
				}else{
					controller.currentOpMode = OpMode.VEHICLE_TRIGGER;
					this.text = BuilderGUI.translate("gui.trafficsignalcontroller.modetrigger");
				}
			}
		});
		addLabel(new GUIComponentLabel(guiLeft + 30, modeButton.y + 5, Color.WHITE, BuilderGUI.translate("gui.trafficsignalcontroller.signalmode")).setButton(modeButton));
		
		addTextBox(greenMainTimeText = new GUIComponentTextBox(guiLeft + 180, guiTop + 105, 40, String.valueOf(controller.greenMainTime), 10, Color.WHITE, Color.BLACK, 3));
		addLabel(new GUIComponentLabel(guiLeft + 30, greenMainTimeText.y, Color.WHITE, BuilderGUI.translate("gui.trafficsignalcontroller.greenmaintime")).setBox(greenMainTimeText));
		
		addTextBox(greenCrossTimeText = new GUIComponentTextBox(guiLeft + 180, guiTop + 115, 40, String.valueOf(controller.greenCrossTime), 10, Color.WHITE, Color.BLACK, 3));
		addLabel(new GUIComponentLabel(guiLeft + 30, greenCrossTimeText.y, Color.WHITE, BuilderGUI.translate("gui.trafficsignalcontroller.greencrosstime")).setBox(greenCrossTimeText));
		
		addTextBox(yellowMainTimeText = new GUIComponentTextBox(guiLeft + 180, guiTop + 125, 40, String.valueOf(controller.yellowMainTime), 10, Color.WHITE, Color.BLACK, 1));
		addLabel(new GUIComponentLabel(guiLeft + 30, yellowMainTimeText.y, Color.WHITE, BuilderGUI.translate("gui.trafficsignalcontroller.yellowmaintime")).setBox(yellowMainTimeText));
		
		addTextBox(yellowCrossTimeText = new GUIComponentTextBox(guiLeft + 180, guiTop + 135, 40, String.valueOf(controller.yellowCrossTime), 10, Color.WHITE, Color.BLACK, 1));
		addLabel(new GUIComponentLabel(guiLeft + 30, yellowCrossTimeText.y, Color.WHITE, BuilderGUI.translate("gui.trafficsignalcontroller.yellowcrosstime")).setBox(yellowMainTimeText));
		
		addTextBox(allRedTimeText = new GUIComponentTextBox(guiLeft + 180, guiTop + 145, 40, String.valueOf(controller.allRedTime), 10, Color.WHITE, Color.BLACK, 1));
		addLabel(new GUIComponentLabel(guiLeft + 30, allRedTimeText.y, Color.WHITE, BuilderGUI.translate("gui.trafficsignalcontroller.allredtime")).setBox(allRedTimeText));
		
		addButton(confirmButton = new GUIComponentButton(guiLeft + 25, guiTop + 160, 80, BuilderGUI.translate("gui.trafficsignalcontroller.confirm")){
			@Override
			public void onClicked(){
				//Convert strings to ints and send update packet to server.
				//Catch bad text if we have any as we don't want that to crash the server.
				try{
					controller.greenMainTime = Integer.valueOf(greenMainTimeText.getText());
					controller.greenCrossTime = Integer.valueOf(greenCrossTimeText.getText());
					controller.yellowMainTime = Integer.valueOf(yellowMainTimeText.getText());
					controller.yellowCrossTime = Integer.valueOf(yellowCrossTimeText.getText());
					controller.allRedTime = Integer.valueOf(allRedTimeText.getText());
					controller.componentLocations.clear();
					controller.componentLocations.addAll(componentLocations);
				}catch(Exception e){
					return;
				}
				InterfaceNetwork.sendToServer(new PacketTileEntitySignalControllerChange(controller));
				BuilderGUI.closeGUI();
			}
		});
	}
	
	@Override
	public void setStates(){
		trafficSignalCount.visible = trafficSignals > 0;
		streetLightCount.visible = streetLights > 0;
		
		trafficSignalCount.text = " X" + trafficSignals;
		streetLightCount.text = " X" + streetLights;
		
		if(trafficSignals > 0){
			orientationButton.enabled = true;
			modeButton.enabled = true;
			greenMainTimeText.enabled = true;
			greenCrossTimeText.enabled = true;
			yellowMainTimeText.enabled = true;
			yellowCrossTimeText.enabled = true;
			allRedTimeText.enabled = true;
			confirmButton.enabled = true;
		}else{
			orientationButton.enabled = false;
			modeButton.enabled = false;
			greenMainTimeText.enabled = false;
			greenCrossTimeText.enabled = false;
			yellowMainTimeText.enabled = false;
			yellowCrossTimeText.enabled = false;
			allRedTimeText.enabled = false;
			confirmButton.enabled = false;
		}
		greenMainTimeText.visible = controller.currentOpMode.equals(OpMode.TIMED_CYCLE);
	}
}
