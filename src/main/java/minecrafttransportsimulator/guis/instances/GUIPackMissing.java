package minecrafttransportsimulator.guis.instances;

import java.awt.Color;

import mcinterface.BuilderGUI;
import minecrafttransportsimulator.guis.components.AGUIBase;
import minecrafttransportsimulator.guis.components.GUIComponentLabel;

public class GUIPackMissing extends AGUIBase{
	GUIComponentLabel noticeLabel;
	
	@Override
	public void setupComponents(int guiLeft, int guiTop){
		addLabel(noticeLabel = new GUIComponentLabel(guiLeft + 130, guiTop + 10, Color.RED, BuilderGUI.translate("gui.packmissing.title"), 3.0F, true, false, 0));
		addLabel(new GUIComponentLabel(guiLeft + 10, guiTop + 40, Color.BLACK, BuilderGUI.translate("gui.packmissing.reason"), 0.75F, false, false, 320));
		addLabel(new GUIComponentLabel(guiLeft + 10, guiTop + 65, Color.BLACK, BuilderGUI.translate("gui.packmissing.nomod"), 0.75F, false, false, 320));
		addLabel(new GUIComponentLabel(guiLeft + 10, guiTop + 90, Color.BLACK, BuilderGUI.translate("gui.packmissing.modlink"), 0.75F, false, false, 320));
		addLabel(new GUIComponentLabel(guiLeft + 10, guiTop + 115, Color.BLACK, BuilderGUI.translate("gui.packmissing.misplaced"), 0.75F, false, false, 320));
		addLabel(new GUIComponentLabel(guiLeft + 10, guiTop + 150, Color.BLACK, BuilderGUI.translate("gui.packmissing.versionerror"), 0.75F, false, false, 320));
	}

	@Override
	public void setStates(){
		noticeLabel.visible = BuilderGUI.inClockPeriod(40, 20);
	}
	
	@Override
	public boolean renderDarkBackground(){
		return true;
	}
}