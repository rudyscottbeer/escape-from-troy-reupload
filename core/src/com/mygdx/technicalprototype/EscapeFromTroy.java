/*
 * GDXRoot.java
 *
 * This is the primary class file for running the game.  It is the "static main" of
 * LibGDX.  In the first lab, we extended ApplicationAdapter.  In previous lab
 * we extended Game.  This is because of a weird graphical artifact that we do not
 * understand.  Transparencies (in 3D only) is failing when we use ApplicationAdapter.
 * There must be some undocumented OpenGL code in setScreen.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package com.mygdx.technicalprototype;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.technicalprototype.util.*;
import com.mygdx.technicalprototype.assets.*;

/**
 * Root class for a LibGDX.
 *
 * This class is technically not the ROOT CLASS. Each platform has another class above
 * this (e.g. PC games use DesktopLauncher) which serves as the true root.  However,
 * those classes are unique to each platform, while this class is the same across all
 * plaforms. In addition, this functions as the root class all intents and purposes,
 * and you would draw it as a root class in an architecture specification.
 */
public class EscapeFromTroy extends Game implements ScreenListener {
	public ShapeRenderer shapeRenderer;

	/** AssetManager to load game assets (textures, sounds, etc.) */
	AssetDirectory directory;
	/** Drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas;
	/** Player mode for the asset loading screen (CONTROLLER CLASS) */
	private MenuMode loading;
	/** Player mode for the level select screen (CONTROLLER CLASS) */
	private LevelSelectMode levelSelect;
	/** List of all WorldControllers */
	private LevelMode level;

	private float[] volumes = new float[3];
	private float brightness = 1;

	/**
	 * Creates a new game from the configuration settings.
	 *
	 * This method configures the asset manager, but does not load any assets
	 * or assign any screen.
	 */
	public EscapeFromTroy() { }

	/**
	 * Called when the Application is first created.
	 *
	 * This is method immediately loads assets for the loading screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	public void create() {
		shapeRenderer = new ShapeRenderer(8);

		canvas  = new GameCanvas();
		loading = new MenuMode("assets.json",canvas,1);
		levelSelect = new LevelSelectMode(canvas, 1);

		volumes[0] = 1;
		volumes[1] = 1;
		volumes[2] = 1;
		updateSettings(volumes, 1);

		loading.setScreenListener(this);
		levelSelect.setScreenListener(this);

		setScreen(loading);
	}

	/**
	 * Called when the Application is destroyed.
	 *
	 * This is preceded by a call to pause().
	 */
	public void dispose() {
		// Call dispose on our children
		setScreen(null);

		if (loading != null)
			loading.dispose();
		if (levelSelect != null)
			levelSelect.dispose();
		if (level != null)
			level.dispose();

		if (canvas != null)
			canvas.dispose();
		canvas = null;

		// Unload all of the resources
		// Unload all of the resources
		if (directory != null) {
			directory.unloadAssets();
			directory.dispose();
			directory = null;
		}
		super.dispose();
	}

	/**
	 * Called when the Application is resized.
	 *
	 * This can happen at any point during a non-paused state but will never happen
	 * before a call to create().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		canvas.resize();
		super.resize(width,height);
	}

	public void updateSettings() {
		if (loading != null) {
			loading.updateSettings(volumes, brightness);
		}

		if (levelSelect != null) {
			levelSelect.updateSettings(volumes, brightness);
		}

		if (level != null) {
			level.updateSettings(volumes, brightness);
		}
	}

	public void updateSettings(float[] volumes, float brightness) {
		this.volumes = volumes;
		this.brightness = brightness;
		updateSettings();
	}

	/**
	 * The given screen has made a request to exit its player mode.
	 *
	 * The value exitCode can be used to implement menu options.
	 *
	 * @param screen   The screen requesting to exit
	 * @param exitCode The state of the screen upon exit
	 */
	public void exitScreen(Screen screen, int exitCode) {
		if (exitCode == -1) {
			Gdx.app.exit();
		}

		if (screen == loading) {
			directory = loading.getAssets();
			if (exitCode == 0) {
				level = new LevelMode(1, this);
				level.gatherAssets(directory);
				level.setScreenListener(this);
				level.setCanvas(canvas);
				level.reset();
				setScreen(level);
			} else if (exitCode == 1) {
				levelSelect.setStage();
				levelSelect.chooseDifficulty();
				setScreen(levelSelect);
			}
			updateSettings(loading.getVolumes(), loading.getBrightness());
		} else if (screen == levelSelect && exitCode > 0) {
//			try {
				level = new LevelMode(exitCode, this);
				level.gatherAssets(directory);
				level.setScreenListener(this);
				level.setCanvas(canvas);
				level.reset();
				setScreen(level);
//			} catch (NullPointerException e) {
//				System.out.println("Unsupported level");
//				setScreen(levelSelect);
//			}
		} else if (screen == levelSelect) {
			loading.setState(MenuMode.MenuState.MENU);
			setScreen(loading);
		} else if (screen == level && exitCode > 0) {
			level = new LevelMode(exitCode+1, this);
			level.gatherAssets(directory);
			level.setScreenListener(this);
			level.setCanvas(canvas);
			level.reset();
			setScreen(level);
			updateSettings(level.getVolumes(), level.getBrightness());
		} else if (screen == level && exitCode == -2) {
			scrubCanvas();
			levelSelect.setStage();
			levelSelect.chooseDifficulty();
			setScreen(levelSelect);
			updateSettings(level.getVolumes(), level.getBrightness());
		} else {
			scrubCanvas();
			loading.setState(MenuMode.MenuState.MENU);
			setScreen(loading);
			updateSettings(level.getVolumes(), level.getBrightness());
		}
	}

	public void scrubCanvas() {
		canvas = new GameCanvas();
		if (loading != null)
			loading.setCanvas(canvas);
		if (levelSelect != null)
			levelSelect.setCanvas(canvas);
	}
}
