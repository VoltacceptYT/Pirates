package ui;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import main.Game;
import utils.LoadSave;
import utils.Settings;

/**
 * Resolution selector: [<] [ 1280x720 ] [>]
 */
public class ResolutionOptions {

	// --- SLICING DIMENSIONS (Fixed at 3x because your sprite sheet is 3x) ---
	// DO NOT change these to Game.SCALE, or the image will be cut in half!
	private static final int IMG_SCALE = 3;
	private static final int BTN = 14 * IMG_SCALE; 
	private static final int PLAQUE_W = 71 * IMG_SCALE; 
	private static final int PLAQUE_H = 12 * IMG_SCALE; 
	private static final int GLYPH_W = 3 * IMG_SCALE; 
	private static final int GLYPH_H = 5 * IMG_SCALE; 
	private static final int GLYPH_GAP = 1 * IMG_SCALE; 
	private static final String GLYPHS = "0123456789x";

	// --- RENDERING DIMENSIONS (The base 480p sizes, multiplied by Game.SCALE) ---
	private static final int BTN_ORIG = 14;
	private static final int PLAQUE_W_ORIG = 71;
	private static final int PLAQUE_H_ORIG = 12;
	private static final int GLYPH_W_ORIG = 3;
	private static final int GLYPH_H_ORIG = 5;
	private static final int GLYPH_GAP_ORIG = 1;

	private final Game game;
	private final Rectangle leftBounds, rightBounds, labelBounds;
	private BufferedImage[] leftImgs, rightImgs, glyphImgs;
	private BufferedImage plaqueImg;
	private boolean leftOver, rightOver, leftPressed, rightPressed;

	public ResolutionOptions(Game game, int centerX, int y) {
		this.game = game;
		loadImgs();

		// These dynamic values will now correctly adjust to 1, 2, or 3 based on resolution!
		int arrowSize = (int) (BTN_ORIG * Game.SCALE);
		int labelWidth = (int) (PLAQUE_W_ORIG * Game.SCALE);
		int labelHeight = (int) (PLAQUE_H_ORIG * Game.SCALE);
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
			// Slicing using the 3x dimensions (BTN)
			leftImgs[i] = atlas.getSubimage(i * BTN, 0, BTN, BTN);
			rightImgs[i] = atlas.getSubimage(i * BTN, BTN, BTN, BTN);
		}
		plaqueImg = atlas.getSubimage(0, BTN * 2, PLAQUE_W, PLAQUE_H);

		glyphImgs = new BufferedImage[GLYPHS.length()];
		for (int i = 0; i < glyphImgs.length; i++)
			glyphImgs[i] = atlas.getSubimage(i * GLYPH_W, BTN * 2 + PLAQUE_H, GLYPH_W, GLYPH_H);
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
		// Draws the 3x image but scales it down to 2x (720p) or 1x (480p) automatically
		g.drawImage(imgs[index], r.x, r.y, r.width, r.height, null);
	}

	private void drawLabel(Graphics g) {
		String text = Settings.getResolutionLabel();
		int paren = text.indexOf(' ');
		if (paren > 0)
			text = text.substring(0, paren);

		// Calculates text size dynamically based on Game.SCALE
		int glyphW = (int) (GLYPH_W_ORIG * Game.SCALE);
		int glyphH = (int) (GLYPH_H_ORIG * Game.SCALE);
		int gap = (int) (GLYPH_GAP_ORIG * Game.SCALE);
		int totalW = text.length() * glyphW + (text.length() - 1) * gap;

		int x = labelBounds.x + (labelBounds.width - totalW) / 2;
		int y = labelBounds.y + (int) (3 * Game.SCALE); // 3px from top (scaled)
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