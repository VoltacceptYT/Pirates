package utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Level Creation, load level, save level, modify level, load game, save game
 * 
 * @author Greg
 *
 */
public class LoadSave {

	public static final String PLAYER_ATLAS = "player_sprites.png";
	public static final String LEVEL_ATLAS = "outside_sprites.png";
	public static final String MENU_BUTTONS = "button_atlas.png";
	public static final String MENU_BACKGROUND = "menu_background.png";
	public static final String PAUSE_BACKGROUND = "pause_background.png";
	public static final String SOUNDS_BUTTONS = "sound_button.png";
	public static final String URM_BUTTONS = "urm_buttons.png"; // unpause replay menu buttons ??
	public static final String VOLUME_BUTTONS = "volume_buttons.png"; // volume slider
	public static final String MENU_BACKGROUND_IMG = "background_menu.png"; // volume slider
	public static final String PLAYING_BG_IMG = "playing_bg_img.png";
	public static final String SMALL_CLOUDS = "small_clouds.png";
	public static final String BIG_CLOUDS = "big_clouds.png";
	public static final String CRABBY_SPRITE = "crabby_sprite.png";
	public static final String STATUS_BAR = "health_power_bar.png";
	public static final String COMPLETED_IMG = "completed_sprite.png";
	public static final String POTION_ATLAS = "potions_sprites.png";
	public static final String CONTAINER_ATLAS = "objects_sprites.png";
	public static final String TRAP_ATLAS = "trap_atlas.png";
	public static final String CANNON_ATLAS = "cannon_atlas.png";
	public static final String CANNON_BALL = "ball.png";
	public static final String DEATH_SCREEN = "death_screen.png";
	public static final String OPTIONS_MENU = "options_background.png";
	
	
	public static BufferedImage GetSpriteAtlas(String fileName) {
		BufferedImage img = null;
		try (InputStream is = LoadSave.class.getResourceAsStream("/" + fileName)) {
			img = ImageIO.read(is);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}
	
	public static BufferedImage[] GetAllLevels() {
		// Reads /lvls/1.png, /lvls/2.png, ... from the classpath until one is missing.
		// Works both from an IDE and from inside a packaged .jar (no File access).
		List<BufferedImage> levels = new ArrayList<>();
		int i = 1;
		while (true) {
			try (InputStream is = LoadSave.class.getResourceAsStream("/lvls/" + i + ".png")) {
				if (is == null)
					break;
				levels.add(ImageIO.read(is));
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			i++;
		}
		return levels.toArray(new BufferedImage[0]);
	}

}
