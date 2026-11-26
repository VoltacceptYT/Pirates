package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Settings {

	public static final int[][] RESOLUTIONS = { { 854, 480 }, { 1280, 720 }, { 1920, 1080 }, { 2560, 1440 } };
	public static final String[] RESOLUTION_LABELS = { "854x480 (480p)", "1280x720 (720p)", "1920x1080 (1080p)", "2560x1440 (1440p)" };

	private static final String FILE_NAME = "settings.properties";
	private static final String KEY_RESOLUTION = "resolution";
	private static final int DEFAULT_RESOLUTION = 1;

	private static int resolutionIndex = DEFAULT_RESOLUTION;

	static {
		load();
	}

	private Settings() {
	}

	private static void load() {
		File file = new File(FILE_NAME);
		if (!file.exists())
			return;

		Properties props = new Properties();
		try (FileInputStream in = new FileInputStream(file)) {
			props.load(in);
			int index = Integer.parseInt(props.getProperty(KEY_RESOLUTION, String.valueOf(DEFAULT_RESOLUTION)).trim());
			if (index >= 0 && index < RESOLUTIONS.length)
				resolutionIndex = index;
		} catch (IOException | NumberFormatException e) {
			System.out.println("Settings.load()...................... Could not read " + FILE_NAME + ", using defaults");
		}
	}

	public static void save() {
		Properties props = new Properties();
		props.setProperty(KEY_RESOLUTION, String.valueOf(resolutionIndex));
		try (FileOutputStream out = new FileOutputStream(FILE_NAME)) {
			props.store(out, "Pirates settings");
		} catch (IOException e) {
			System.out.println("Settings.save()...................... Could not write " + FILE_NAME);
		}
	}

	public static int getResolutionCount() {
		return RESOLUTIONS.length;
	}

	public static int getResolutionIndex() {
		return resolutionIndex;
	}

	public static void setResolutionIndex(int index) {
		if (index < 0 || index >= RESOLUTIONS.length)
			return;
		resolutionIndex = index;
		save();
	}

	public static int getWindowWidth() {
		return RESOLUTIONS[resolutionIndex][0];
	}

	public static int getWindowHeight() {
		return RESOLUTIONS[resolutionIndex][1];
	}

	public static String getResolutionLabel() {
		return RESOLUTION_LABELS[resolutionIndex];
	}
}
