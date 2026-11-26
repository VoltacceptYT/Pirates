package editor;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * WYSIWYG editor for Options / Pause UI.
 *
 * Windows:
 *   run_ui_editor.bat
 *
 * Numbers are design units (the value before * Game.SCALE in your code).
 */
public class UIEditor extends JFrame {

    public static final int DESIGN_W = 832; // 26 * 32
    public static final int DESIGN_H = 448; // 14 * 32

    private float previewScale = 1.5f;
    private JPanel canvas;
    private JLabel status;
    private JComboBox<String> screenSelector;

    private final List<UIElement> elements = new ArrayList<>();
    private UIElement selected = null;
    private int dragOffsetX, dragOffsetY;
    private boolean dragging = false;

    private static final int GRID = 4; // snap grid in design units
    private final java.util.Deque<List<UIElement>> undoStack = new java.util.ArrayDeque<>();
    private final java.util.Deque<List<UIElement>> redoStack = new java.util.ArrayDeque<>();
    private boolean suppressUndo = false;

    private BufferedImage optionsBg, pauseBg;
    private BufferedImage soundFrame;
    private BufferedImage volumeSlider;
    private BufferedImage volumeKnob;
    private BufferedImage urmFrame;
    private BufferedImage resArrowL;
    private BufferedImage resArrowR;
    private BufferedImage resPlaque;

    public UIEditor() {
        super("Pirates UI Editor - Options / Pause");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        loadImages();
        buildUI();
        loadOptionsScreen();
    }


    private List<UIElement> snapshot() {
        List<UIElement> copy = new ArrayList<>();
        for (UIElement el : elements) {
            UIElement c = new UIElement(el.name, el.x, el.y, el.w, el.h, el.img, el.isBg);
            copy.add(c);
        }
        return copy;
    }

    private void pushUndo() {
        if (suppressUndo) return;
        undoStack.push(snapshot());
        redoStack.clear();
        if (undoStack.size() > 100) undoStack.removeLast();
    }

    private void restore(List<UIElement> state) {
        // keep selection by name if possible
        String selName = selected != null ? selected.name : null;
        elements.clear();
        elements.addAll(state);
        selected = null;
        if (selName != null) {
            for (UIElement el : elements) {
                if (el.name.equals(selName)) { selected = el; break; }
            }
        }
        canvas.repaint();
        updateStatus();
    }

    private void undo() {
        if (undoStack.isEmpty()) return;
        redoStack.push(snapshot());
        restore(undoStack.pop());
    }

    private void redo() {
        if (redoStack.isEmpty()) return;
        undoStack.push(snapshot());
        restore(redoStack.pop());
    }

    private int snap(int v) {
        return Math.round(v / (float) GRID) * GRID;
    }

    private void loadImages() {
        try {
            optionsBg = ImageIO.read(new File("res/options_background.png"));
            pauseBg   = ImageIO.read(new File("res/pause_background.png"));

            BufferedImage soundSheet = ImageIO.read(new File("res/sound_button.png"));
            soundFrame = soundSheet.getSubimage(0, 0, 42, 42);

            BufferedImage volSheet = ImageIO.read(new File("res/volume_buttons.png"));
            volumeKnob   = volSheet.getSubimage(0, 0, 28, 44);
            volumeSlider = volSheet.getSubimage(3 * 28, 0, 215, 44);

            BufferedImage urmSheet = ImageIO.read(new File("res/urm_buttons.png"));
            urmFrame = urmSheet.getSubimage(0, 2 * 56, 56, 56);

            BufferedImage resSheet = ImageIO.read(new File("res/resolution_ui.png"));
            resArrowL  = resSheet.getSubimage(0, 0, 42, 42);
            resArrowR  = resSheet.getSubimage(0, 42, 42, 42);
            resPlaque  = resSheet.getSubimage(0, 84, 213, 36);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Could not load images from res/.\nRun from the project root folder.\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        screenSelector = new JComboBox<>(new String[]{"Options", "Pause"});
        screenSelector.addActionListener(e -> {
            if (screenSelector.getSelectedIndex() == 0) loadOptionsScreen();
            else loadPauseScreen();
            canvas.repaint();
        });
        toolbar.add(new JLabel("Screen:"));
        toolbar.add(screenSelector);

        toolbar.add(new JLabel("  Preview scale:"));
        JSpinner scaleSpinner = new JSpinner(new SpinnerNumberModel(1.5, 0.5, 4.0, 0.25));
        scaleSpinner.addChangeListener(e -> {
            previewScale = ((Number) scaleSpinner.getValue()).floatValue();
            canvas.revalidate();
            canvas.repaint();
        });
        toolbar.add(scaleSpinner);

        JButton exportBtn = new JButton("Export Java code");
        exportBtn.addActionListener(e -> exportCode());
        toolbar.add(exportBtn);

        JButton resetBtn = new JButton("Reset to defaults");
        resetBtn.addActionListener(e -> {
            if (screenSelector.getSelectedIndex() == 0) loadOptionsScreen();
            else loadPauseScreen();
            canvas.repaint();
        });
        toolbar.add(resetBtn);

        root.add(toolbar, BorderLayout.NORTH);

        canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                int w = (int) (DESIGN_W * previewScale);
                int h = (int) (DESIGN_H * previewScale);

                g2.setColor(new Color(210, 163, 163));
                g2.fillRect(0, 0, getWidth(), getHeight());

                int ox = (getWidth() - w) / 2;
                int oy = (getHeight() - h) / 2;
                g2.translate(ox, oy);
                g2.scale(previewScale, previewScale);

                g2.setColor(new Color(180, 140, 140));
                g2.fillRect(0, 0, DESIGN_W, DESIGN_H);

                // light grid
                g2.setColor(new Color(0, 0, 0, 30));
                for (int gx = 0; gx <= DESIGN_W; gx += GRID) g2.drawLine(gx, 0, gx, DESIGN_H);
                for (int gy = 0; gy <= DESIGN_H; gy += GRID) g2.drawLine(0, gy, DESIGN_W, gy);

                for (UIElement el : elements) {
                    el.draw(g2, selected == el);
                }
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension((int) (DESIGN_W * previewScale) + 40,
                                     (int) (DESIGN_H * previewScale) + 40);
            }
        };
        canvas.setBackground(Color.DARK_GRAY);

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = toDesign(e.getPoint());
                selected = null;
                for (int i = elements.size() - 1; i >= 0; i--) {
                    UIElement el = elements.get(i);
                    if (el.contains(p.x, p.y)) {
                        selected = el;
                        dragOffsetX = p.x - el.x;
                        dragOffsetY = p.y - el.y;
                        dragging = true;
                        pushUndo(); // save state before drag
                        break;
                    }
                }
                canvas.repaint();
                updateStatus();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragging && selected != null) {
                    selected.x = snap(selected.x);
                    selected.y = snap(selected.y);
                    canvas.repaint();
                    updateStatus();
                }
                dragging = false;
            }
        });
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging && selected != null) {
                    Point p = toDesign(e.getPoint());
                    selected.x = p.x - dragOffsetX;
                    selected.y = p.y - dragOffsetY;
                    canvas.repaint();
                    updateStatus();
                }
            }
        });

        root.add(new JScrollPane(canvas), BorderLayout.CENTER);

        JPanel props = new JPanel();
        props.setLayout(new BoxLayout(props, BoxLayout.Y_AXIS));
        props.setBorder(BorderFactory.createTitledBorder("Selected element"));
        props.setPreferredSize(new Dimension(240, 0));

        status = new JLabel("<html>Click an element to select.<br>Drag to move.</html>");
        status.setBorder(new EmptyBorder(8, 8, 8, 8));
        props.add(status);

        JPanel sizePanel = new JPanel(new GridLayout(0, 2, 4, 4));
        sizePanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JButton shrink = new JButton("Scale 1/2");
        shrink.addActionListener(e -> {
            if (selected != null) {
                pushUndo();
                selected.w = Math.max(4, selected.w / 2);
                selected.h = Math.max(4, selected.h / 2);
                selected.w = snap(selected.w);
                selected.h = snap(selected.h);
                canvas.repaint(); updateStatus();
            }
        });
        JButton grow = new JButton("Scale 2x");
        grow.addActionListener(e -> {
            if (selected != null) {
                pushUndo();
                selected.w *= 2; selected.h *= 2;
                selected.w = snap(selected.w);
                selected.h = snap(selected.h);
                canvas.repaint(); updateStatus();
            }
        });
        sizePanel.add(shrink); sizePanel.add(grow);

        JButton setW = new JButton("Set W...");
        setW.addActionListener(e -> {
            if (selected == null) return;
            String s = JOptionPane.showInputDialog(this, "Width:", selected.w);
            if (s != null) try {
                pushUndo();
                selected.w = snap(Integer.parseInt(s.trim()));
                canvas.repaint(); updateStatus();
            } catch (Exception ignored) {}
        });
        JButton setH = new JButton("Set H...");
        setH.addActionListener(e -> {
            if (selected == null) return;
            String s = JOptionPane.showInputDialog(this, "Height:", selected.h);
            if (s != null) try {
                pushUndo();
                selected.h = snap(Integer.parseInt(s.trim()));
                canvas.repaint(); updateStatus();
            } catch (Exception ignored) {}
        });
        sizePanel.add(setW); sizePanel.add(setH);
        props.add(sizePanel);

        JTextArea help = new JTextArea(
            "All numbers are design units\n" +
            "(the value before * Game.SCALE).\n\n" +
            "ResolutionOptions already uses\n" +
            "compact sizes (14, 71, 12...).\n\n" +
            "Export copies Java snippets\n" +
            "to the clipboard."
        );
        help.setEditable(false);
        help.setOpaque(false);
        help.setBorder(new EmptyBorder(8, 8, 8, 8));
        props.add(help);

        root.add(props, BorderLayout.EAST);
        setContentPane(root);
        // Keyboard: Ctrl+Z undo, Ctrl+Shift+Z / Ctrl+Y redo
        JRootPane rootPane = getRootPane();
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");
        rootPane.getActionMap().put("undo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { undo(); }
        });
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "redo");
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "redo");
        rootPane.getActionMap().put("redo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { redo(); }
        });

    }

    private Point toDesign(Point screen) {
        int w = (int) (DESIGN_W * previewScale);
        int h = (int) (DESIGN_H * previewScale);
        int ox = (canvas.getWidth() - w) / 2;
        int oy = (canvas.getHeight() - h) / 2;
        return new Point(
            (int) ((screen.x - ox) / previewScale),
            (int) ((screen.y - oy) / previewScale)
        );
    }

    private void updateStatus() {
        if (selected == null) {
            status.setText("<html>Click an element to select.<br>Drag to move.</html>");
            return;
        }
        status.setText(String.format(
            "<html><b>%s</b><br>x = %d<br>y = %d<br>w = %d<br>h = %d</html>",
            selected.name, selected.x, selected.y, selected.w, selected.h
        ));
    }

    private void loadOptionsScreen() {
        elements.clear();
        selected = null;
        undoStack.clear();
        redoStack.clear();

        int bgW = 282, bgH = 393;
        int bgX = DESIGN_W / 2 - bgW / 2;
        int bgY = 33;
        elements.add(new UIElement("Options BG", bgX, bgY, bgW, bgH, optionsBg, true));

        elements.add(new UIElement("Music Button", 450, 140, 42, 42, soundFrame, false));
        elements.add(new UIElement("SFX Button",   450, 186, 42, 42, soundFrame, false));

        elements.add(new UIElement("Volume Slider", 309, 278, 215, 44, volumeSlider, false));
        elements.add(new UIElement("Volume Knob",   309 + 215/2 - 14, 278, 28, 44, volumeKnob, false));

        int resY = 310;
        int arrow = 14;
        int plaqueW = 71, plaqueH = 12;
        int gap = 4;
        int total = arrow * 2 + plaqueW + gap * 2;
        int startX = DESIGN_W / 2 - total / 2;
        elements.add(new UIElement("Res Left Arrow",  startX, resY, arrow, arrow, resArrowL, false));
        elements.add(new UIElement("Res Plaque",      startX + arrow + gap, resY + (arrow - plaqueH)/2, plaqueW, plaqueH, resPlaque, false));
        elements.add(new UIElement("Res Right Arrow", startX + arrow + gap + plaqueW + gap, resY, arrow, arrow, resArrowR, false));

        elements.add(new UIElement("Home Button", 387, 352, 56, 56, urmFrame, false));

        updateStatus();
    }

    private void loadPauseScreen() {
        elements.clear();
        selected = null;
        undoStack.clear();
        redoStack.clear();

        int bgW = 258, bgH = 389;
        int bgX = DESIGN_W / 2 - bgW / 2;
        int bgY = 25;
        elements.add(new UIElement("Pause BG", bgX, bgY, bgW, bgH, pauseBg, true));

        elements.add(new UIElement("Music Button", 450, 140, 42, 42, soundFrame, false));
        elements.add(new UIElement("SFX Button",   450, 186, 42, 42, soundFrame, false));
        elements.add(new UIElement("Volume Slider", 309, 278, 215, 44, volumeSlider, false));
        elements.add(new UIElement("Volume Knob",   309 + 215/2 - 14, 278, 28, 44, volumeKnob, false));

        int resY = 310;
        int arrow = 14;
        int plaqueW = 71, plaqueH = 12;
        int gap = 4;
        int total = arrow * 2 + plaqueW + gap * 2;
        int startX = DESIGN_W / 2 - total / 2;
        elements.add(new UIElement("Res Left Arrow",  startX, resY, arrow, arrow, resArrowL, false));
        elements.add(new UIElement("Res Plaque",      startX + arrow + gap, resY + (arrow - plaqueH)/2, plaqueW, plaqueH, resPlaque, false));
        elements.add(new UIElement("Res Right Arrow", startX + arrow + gap + plaqueW + gap, resY, arrow, arrow, resArrowR, false));

        elements.add(new UIElement("Menu Button",    313, 348, 56, 56, urmFrame, false));
        elements.add(new UIElement("Replay Button",  387, 348, 56, 56, urmFrame, false));
        elements.add(new UIElement("Unpause Button", 462, 348, 56, 56, urmFrame, false));

        updateStatus();
    }

    private void exportCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("// ===== Generated by UIEditor =====\n");
        sb.append("// Design units - multiply by Game.SCALE in code\n\n");

        for (UIElement el : elements) {
            sb.append(String.format("// %s  ->  x=%d  y=%d  w=%d  h=%d\n",
                    el.name, el.x, el.y, el.w, el.h));
        }

        sb.append("\n// --- Ready-to-paste snippets ---\n\n");

        UIElement music = find("Music Button");
        UIElement sfx   = find("SFX Button");
        UIElement vol   = find("Volume Slider");
        UIElement home  = find("Home Button");
        UIElement resL  = find("Res Left Arrow");
        UIElement resP  = find("Res Plaque");

        if (music != null) {
            sb.append("// AudioOptions\n");
            sb.append(String.format("int soundX = (int) (%d * Game.SCALE);\n", music.x));
            sb.append(String.format("int musicY = (int) (%d * Game.SCALE);\n", music.y));
            if (sfx != null)
                sb.append(String.format("int sfxY   = (int) (%d * Game.SCALE);\n", sfx.y));
        }
        if (vol != null) {
            sb.append(String.format("int volumeX = (int) (%d * Game.SCALE);\n", vol.x));
            sb.append(String.format("int volumeY = (int) (%d * Game.SCALE);\n", vol.y));
        }
        if (home != null) {
            sb.append("\n// GameOptions menu button\n");
            sb.append(String.format("int menuX = (int) (%d * Game.SCALE);\n", home.x));
            sb.append(String.format("int menuY = (int) (%d * Game.SCALE);\n", home.y));
        }
        if (resL != null) {
            sb.append("\n// ResolutionOptions\n");
            sb.append(String.format("// y = (int) (%d * Game.SCALE);\n", resL.y));
            sb.append(String.format("// arrowSize = (int) (%d * Game.SCALE);\n", resL.w));
            if (resP != null) {
                sb.append(String.format("// labelWidth  = (int) (%d * Game.SCALE);\n", resP.w));
                sb.append(String.format("// labelHeight = (int) (%d * Game.SCALE);\n", resP.h));
            }
        }

        String result = sb.toString();
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(result), null);

        JTextArea area = new JTextArea(result, 22, 65);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "Exported (also copied to clipboard)", JOptionPane.INFORMATION_MESSAGE);
    }

    private UIElement find(String name) {
        for (UIElement el : elements)
            if (el.name.equals(name)) return el;
        return null;
    }

    static class UIElement {
        String name;
        int x, y, w, h;
        BufferedImage img;
        boolean isBg;

        UIElement(String name, int x, int y, int w, int h, BufferedImage img, boolean isBg) {
            this.name = name;
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.img = img;
            this.isBg = isBg;
        }

        boolean contains(int px, int py) {
            return px >= x && px < x + w && py >= y && py < y + h;
        }

        void draw(Graphics2D g, boolean sel) {
            if (img != null) {
                g.drawImage(img, x, y, w, h, null);
            } else {
                g.setColor(new Color(100, 100, 200, 180));
                g.fillRect(x, y, w, h);
            }
            if (sel) {
                g.setColor(Color.YELLOW);
                g.setStroke(new BasicStroke(2f));
                g.drawRect(x - 1, y - 1, w + 1, h + 1);
            } else if (!isBg) {
                g.setColor(new Color(0, 220, 0, 100));
                g.drawRect(x, y, w - 1, h - 1);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UIEditor().setVisible(true));
    }
}
