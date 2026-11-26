package ui;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import main.Game;
import utils.LoadSave;
import utils.Settings;

public class ResolutionOptions {

	private static final int BTN_SLICE = 42;          
	private static final int PLAQUE_W_SLICE = 213;    
	private static final int PLAQUE_H_SLICE = 36;     
	private static final int GLYPH_W_SLICE = 9;       
	private static final int GLYPH_H_SLICE = 15;      
	private static final int GLYPH_GAP_SLICE = 3;     
	private static final String GLYPHS = "0123456789x";

	private final Game game;
	private final Rectangle leftBounds, rightBounds, labelBounds;
	private BufferedImage[] leftImgs, rightImgs, glyphImgs;
	private BufferedImage plaqueImg;
	private boolean leftOver, rightOver, leftPressed, rightPressed;

	public ResolutionOptions(Game game, int centerX, int y) {
		this.game = game;
		loadImgs();

		int arrowSize = (int) (14 * Game.SCALE);
		int labelWidth = (int) (71 * Game.SCALE);
		int labelHeight = (int) (12 * Game.SCALE);
		int gap = (int) (4 * Game.SCALE);
		int totalWidth = arrowSize * 2 + labelWidth + gap * 2;
		int startX = centerX - totalWidth / 2;

		leftBounds = new Rectangle(startX, y, arrowSize, arrowSize);
		labelBounds = new Rectangle(startX + arrowSize + gap, y + (arrowSize - labelHeight) / 2, labelWidth, labelHeight);
		rightBounds = new Rectangle(startX + arrowSize + gap + labelWidth + gap, y, arrowSize, arrowSize);
	}

	private void loadImgs() {
		BufferedImage atlas = LoadSave.GetSpriteAtlas(LoadSave.RESOLUTION_UI);

		leftImgs = new BufferedImage[3];
		rightImgs = new BufferedImage[3];
		for (int i = 0; i < 3; i++) {
			leftImgs[i] = atlas.getSubimage(i * BTN_SLICE, 0, BTN_SLICE, BTN_SLICE);
			rightImgs[i] = atlas.getSubimage(i * BTN_SLICE, BTN_SLICE, BTN_SLICE, BTN_SLICE);
		}
		plaqueImg = atlas.getSubimage(0, BTN_SLICE * 2, PLAQUE_W_SLICE, PLAQUE_H_SLICE);

		glyphImgs = new BufferedImage[GLYPHS.length()];
		for (int i = 0; i < glyphImgs.length; i++)
			glyphImgs[i] = atlas.getSubimage(i * GLYPH_W_SLICE, BTN_SLICE * 2 + PLAQUE_H_SLICE, GLYPH_W_SLICE, GLYPH_H_SLICE);
	}

	public void update() {
	}

	public void draw(Graphics g) {
		g.drawImage(plaqueImg, labelBounds.x, labelBounds.y, labelBounds.width, labelBounds.height, null);
		drawButton(g, leftImgs, leftBounds, leftOver, leftPressed);
		drawButton(g, rightImgs, rightBounds, rightOver, rightPressed);
		drawLabel(g);
	}

	private void drawButton(Graphics g, BufferedImage[] imgs, Rectangle r, boolean over, boolean pressed) {
		int index = pressed ? 2 : over ? 1 : 0;
		g.drawImage(imgs[index], r.x, r.y, r.width, r.height, null);
	}

	private void drawLabel(Graphics g) {
		String text = Settings.getResolutionLabel();
		int paren = text.indexOf(' ');
		if (paren > 0)
			text = text.substring(0, paren);

		int glyphW = (int) (3 * Game.SCALE);
		int glyphH = (int) (5 * Game.SCALE);
		int gap = (int) (1 * Game.SCALE);
		int totalW = text.length() * glyphW + (text.length() - 1) * gap;

		int x = labelBounds.x + (labelBounds.width - totalW) / 2;
		int y = labelBounds.y + (int) (3 * Game.SCALE); 
		
		for (char c : text.toCharArray()) {
			int idx = GLYPHS.indexOf(c);
			if (idx >= 0)
				g.drawImage(glyphImgs[idx], x, y, glyphW, glyphH, null);
			x += glyphW + gap;
		}
	}

	public void mousePressed(MouseEvent e) {
		if (leftBounds.contains(e.getX(), e.getY()))
			leftPressed = true;
		else if (rightBounds.contains(e.getX(), e.getY()))
			rightPressed = true;
	}

	public void mouseReleased(MouseEvent e) {
		if (leftPressed && leftBounds.contains(e.getX(), e.getY()))
			changeResolution(-1);
		else if (rightPressed && rightBounds.contains(e.getX(), e.getY()))
			changeResolution(1);

		leftPressed = false;
		rightPressed = false;
	}

	public void mouseMoved(MouseEvent e) {
		leftOver = leftBounds.contains(e.getX(), e.getY());
		rightOver = rightBounds.contains(e.getX(), e.getY());
	}

	private void changeResolution(int direction) {
		int count = Settings.getResolutionCount();
		Settings.setResolutionIndex((Settings.getResolutionIndex() + direction + count) % count);
		leftOver = false;
		rightOver = false;
		game.applyResolution();
	}
}