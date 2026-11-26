package main;

import java.awt.Graphics;

import audio.AudioPlayer;
import gamestates.GameOptions;
import gamestates.Gamestate;
import gamestates.Menu;
import gamestates.Playing;
import ui.AudioOptions;
import utils.LoadSave;
import utils.Settings;

public class Game implements Runnable {
	private GameWindow gameWindow;
	private GamePanel gamePanel;
	private Thread gameThread;
	private final int FPS = 120;
	private final int UPS = 200;
	
	private Playing playing;
	private Menu menu;
	private GameOptions gameOptions;
	private AudioOptions audioOptions;
	private AudioPlayer audioPlayer;
	
	public final static int TILES_DEFAULT_SIZE = 32;
	public static float SCALE = 3f;
	public final static int TILES_IN_WIDTH = 26; 
	public final static int TILES_IN_HIEGHT = 14; 
	
	public static int TILES_SIZE;
	public static int GAME_WIDTH;
	public static int GAME_HIEGHT;

	static {
		updateScale(3f);
	}

	public Game() {
		initClasses();

		gamePanel = new GamePanel(this);
		gameWindow = new GameWindow(gamePanel);
		gamePanel.setFocusable(true);
		gamePanel.requestFocus();

		startGameLooper();
	}

	private void initClasses() {
		audioOptions = new AudioOptions(this);
		audioPlayer = new AudioPlayer();
		menu = new Menu(this);
		playing = new Playing(this);
		gameOptions = new GameOptions(this);
	}

	private void startGameLooper() {
		gameThread = new Thread(this);
		gameThread.start();
	}

	public void update() {
		switch(Gamestate.state) {
		case MENU:
			menu.update();
			break;
		case PLAYING:
			playing.update();
			break;
		case OPTIONS:
			gameOptions.update();
			break;
		case QUIT:
		default:
			System.exit(0);
			break;
		}
	}

	public void render(Graphics g) {
		switch(Gamestate.state) {
		case MENU:
			menu.draw(g);
			break;
		case PLAYING:
			playing.draw(g);
			break;
		case OPTIONS:
			gameOptions.draw(g);
			break;
		default:
			break;
		}
	}

	@Override
	public void run() {
		double timePerFrame = 1000000000.0 / FPS;
		double timePerUpdate = 1000000000.0 / UPS;

		long previousTime = System.nanoTime();

		int frames = 0;
		int updates = 0;
		long lastSecond = System.currentTimeMillis();

		double deltaU = 0;
		double deltaF = 0;
		
		while (true) {
			long currentTime = System.nanoTime();

			deltaU += (currentTime - previousTime) / timePerUpdate;
			deltaF += (currentTime - previousTime) / timePerFrame;
			previousTime = currentTime;

			if (deltaU >= 1) {
				update();
				updates++;
				deltaU--;
			}

			if (deltaF >= 1) {
				gamePanel.repaint();
				frames++;
				deltaF--;
			}

			if (System.currentTimeMillis() - lastSecond >= 1000) {
				lastSecond = System.currentTimeMillis();
				frames = 0;
				updates = 0;
			}
		}
	}

	public static void updateScale(float newScale) {
		SCALE = newScale;
		TILES_SIZE = (int) (TILES_DEFAULT_SIZE * SCALE);
		GAME_WIDTH = TILES_SIZE * TILES_IN_WIDTH;
		GAME_HIEGHT = TILES_SIZE * TILES_IN_HIEGHT;
	}

	public void applyResolution() {
		int height = Settings.getWindowHeight();
		if (height >= 1080) {
			updateScale(3f);
		} else if (height >= 720) {
			updateScale(2f);
		} else {
			updateScale(1f);
		}
		
		gamePanel.setPanelSize();
		gameWindow.applyResolution(gamePanel);
	}

	public void windowFocusLost() {
		if(Gamestate.state == Gamestate.PLAYING)
			playing.getPlayer().resetBooleans();
	}

	public Menu getMenu() {
		return menu;
	}
	
	public Playing getPlaying() {
		return playing;
	}
	
	public GameOptions getGameOptions() {
		return gameOptions;
	}
	
	public AudioOptions getAudioOptions() {
		return audioOptions;
	}

	public AudioPlayer getAudioPlayer() {
		return audioPlayer;
	}
}