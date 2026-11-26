package ui;

import static utils.Constants.UI.URMButtons.URM_SIZE;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import gamestates.Gamestate;
import gamestates.Playing;
import main.Game;
import utils.LoadSave;

public class PauseOverlay {
	
	private Playing playing;
	private BufferedImage backgroundImg;
	private int bgX,bgY,bgW, bgH; // width, height, x and y location for background image
	private AudioOptions audioOptions;
	private ResolutionOptions resolutionOptions;
	private UrmButton menuButton, replayButton, unpauseButton;
	
	public PauseOverlay(Playing playing) {
		this.playing = playing;
		loadBackground();
		audioOptions = playing.getGame().getAudioOptions();
		resolutionOptions = new ResolutionOptions(playing.getGame(), Game.GAME_WIDTH / 2, (int) (108 * Game.SCALE));
		createUrmButtons();
	}

	private void createUrmButtons() {
		int buttonY = (int) (116 * Game.SCALE);
		int spacing = (int) (30 * Game.SCALE);
		int startX = Game.GAME_WIDTH / 2 - spacing - URM_SIZE / 2;
		
		menuButton = new UrmButton(startX, buttonY, URM_SIZE, URM_SIZE, 2);
		replayButton = new UrmButton(startX + spacing, buttonY, URM_SIZE, URM_SIZE, 1);
		unpauseButton = new UrmButton(startX + spacing * 2, buttonY, URM_SIZE, URM_SIZE, 0);
	}

	private void loadBackground() {
		backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.PAUSE_BACKGROUND);
		bgW = (int) (backgroundImg.getWidth() * Game.SCALE * utils.Constants.UI.COMPACT);
		bgH = (int) (backgroundImg.getHeight() * Game.SCALE * utils.Constants.UI.COMPACT);
		bgX = (int) (Game.GAME_WIDTH / 2 - bgW / 2);
		bgY = (int) (8 * Game.SCALE);
	}

	public void update() {
		menuButton.update();
		replayButton.update();
		unpauseButton.update();
		audioOptions.update();
	}
	
	public void draw(Graphics g) {
		g.drawImage(backgroundImg, bgX, bgY, bgW, bgH, null); // background
		menuButton.draw(g);
		replayButton.draw(g);
		unpauseButton.draw(g);
		audioOptions.draw(g);
		resolutionOptions.draw(g);
	}
	
	public void mouseDragged(MouseEvent e) {
		audioOptions.mouseDragged(e);	
	}

	public void mousePressed(MouseEvent e) {
		if(isIn(e,menuButton)) {
			menuButton.setMousePressed(true);
		}else if(isIn(e,replayButton)) {
			replayButton.setMousePressed(true);
		}else if(isIn(e,unpauseButton)) {
			unpauseButton.setMousePressed(true);
		}else {
			audioOptions.mousePressed(e);
			resolutionOptions.mousePressed(e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		if(isIn(e,menuButton)) {
			if(menuButton.isMousePressed()) {
				playing.resetAll();
				playing.setGamestate(Gamestate.MENU);
				playing.unpauseGame();
			}
		}else if(isIn(e,replayButton)) {
			if (replayButton.isMousePressed()) {
				playing.resetAll();
				playing.unpauseGame();
			}
		}else if(isIn(e,unpauseButton)) {
			if(unpauseButton.isMousePressed()) 
				playing.unpauseGame();
		}else {
			audioOptions.mouseReleased(e);
			resolutionOptions.mouseReleased(e);
		}
		menuButton.resetBooleans();
		replayButton.resetBooleans();
		unpauseButton.resetBooleans();
	}

	public void mouseMoved(MouseEvent e) {
		menuButton.setMouseOver(false);
		replayButton.setMouseOver(false);
		unpauseButton.setMouseOver(false);
		resolutionOptions.mouseMoved(e);
		if(isIn(e,menuButton)) {
			menuButton.setMouseOver(true);
		}else if(isIn(e,replayButton)) {
			replayButton.setMouseOver(true);
		}else if(isIn(e,unpauseButton)) {
			unpauseButton.setMouseOver(true);
		}else 
			audioOptions.mouseMoved(e);
	}
	
	private boolean isIn(MouseEvent e, PauseButton pb ) {
		return pb.getBounds().contains(e.getX(), e.getY());
	}
}
