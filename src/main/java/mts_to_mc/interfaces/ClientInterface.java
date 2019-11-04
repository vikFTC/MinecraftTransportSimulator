package mts_to_mc.interfaces;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

/**
 * Helper class for interfacing with Minecraft.
 * This class is used for interfacing with the Minecraft client.
 * Numerous client methods are here, such as getting if the
 * player has paused the game, checking what GUI the player
 * has open, getting formatted text etc.
 * Events are also here that are designed to populate other
 * systems that require client-specific interactions,
 * such as the PaulsCode SoundSystem.
 *
 * @author don_bruce
 */
public class ClientInterface{
	
	public static boolean isGamePaused(){
		return Minecraft.getMinecraft().isGamePaused();
	}

	public static String translateText(String text){
		return I18n.format(text);
	}
}
