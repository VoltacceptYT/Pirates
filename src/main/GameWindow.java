package main;

import java.awt.Color;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.Dimension;

import javax.swing.JFrame;

import utils.Settings;

public class GameWindow {

	private JFrame jframe;

	public GameWindow(GamePanel gamePanel) {
		jframe = new JFrame();
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		jframe.add(gamePanel);
		jframe.setResizable(false);
		jframe.pack(); 
		jframe.setLocationRelativeTo(null);
		jframe.setVisible(true); 
		
		jframe.addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				gamePanel.getGame().windowFocusLost();
			}
		
			@Override
			public void windowGainedFocus(WindowEvent e) {}
		});
	}

	public void applyResolution(GamePanel gamePanel) {
		gamePanel.setPreferredSize(new Dimension(Settings.getWindowWidth(), Settings.getWindowHeight()));
		jframe.pack();
		jframe.setSize(Settings.getWindowWidth(), Settings.getWindowHeight());
		jframe.setLocationRelativeTo(null);
		gamePanel.requestFocusInWindow();
	}
}