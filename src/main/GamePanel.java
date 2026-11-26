package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import inputs.KeyboardInputs;
import inputs.MouseInputs;
import utils.Settings;

public class GamePanel extends JPanel {

	private MouseInputs moustInputs;
	private Game game;

	public GamePanel(Game game) {
		moustInputs = new MouseInputs(this);
		this.game = game;
		
		setPanelSize();
		addKeyListener(new KeyboardInputs(this));
		addMouseListener(moustInputs);
		addMouseMotionListener(moustInputs);
	}

	public void setPanelSize() {
		Dimension dim = new Dimension(Settings.getWindowWidth(), Settings.getWindowHeight());
		setPreferredSize(dim);
	}

	public void updateGame() {}

	private double getRenderScale() {
		return Math.min((double) getWidth() / Game.GAME_WIDTH, (double) getHeight() / Game.GAME_HIEGHT);
	}

	private int getRenderOffsetX(double scale) {
		return (int) ((getWidth() - Game.GAME_WIDTH * scale) / 2);
	}

	private int getRenderOffsetY(double scale) {
		return (int) ((getHeight() - Game.GAME_HIEGHT * scale) / 2);
	}

	public MouseEvent toGameEvent(MouseEvent e) {
		double scale = getRenderScale();
		if (scale <= 0)
			return e;

		int x = (int) ((e.getX() - getRenderOffsetX(scale)) / scale);
		int y = (int) ((e.getY() - getRenderOffsetY(scale)) / scale);
		return new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(), x, y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, getWidth(), getHeight());

		double scale = getRenderScale();
		if (scale <= 0) {
			g2.dispose();
			return;
		}

		g2.translate(getRenderOffsetX(scale), getRenderOffsetY(scale));
		g2.scale(scale, scale);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, scale < 1.0 ? RenderingHints.VALUE_INTERPOLATION_BILINEAR : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g2.setClip(0, 0, Game.GAME_WIDTH, Game.GAME_HIEGHT);

		game.render(g2);
		g2.dispose();
	}
	
	public Game getGame() { return game; }
}