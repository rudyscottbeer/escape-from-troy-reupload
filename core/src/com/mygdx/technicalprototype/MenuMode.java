/*
 * LoadingMode.java
 *
 * Asset loading is a really tricky problem.  If you have a lot of sound or images,
 * it can take a long time to decompress them and load them into memory.  If you just
 * have code at the start to load all your assets, your game will look like it is hung
 * at the start.
 *
 * The alternative is asynchronous asset loading.  In asynchronous loading, you load a
 * little bit of the assets at a time, but still animate the game while you are loading.
 * This way the player knows the game is not hung, even though he or she cannot do 
 * anything until loading is complete. You know those loading screens with the inane tips 
 * that want to be helpful?  That is asynchronous loading.  
 *
 * This player mode provides a basic loading screen.  While you could adapt it for
 * between level loading, it is currently designed for loading all assets at the 
 * start of the game.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package com.mygdx.technicalprototype;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.*;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.DataOutput;
import com.mygdx.technicalprototype.assets.*;
import com.mygdx.technicalprototype.util.*;

import java.util.ArrayList;

import static com.mygdx.technicalprototype.AssetController.*;
/**
 * Class that provides a loading screen for the state of the game.
 *
 * You still DO NOT need to understand this class for this lab.  We will talk about this
 * class much later in the course.  This class provides a basic template for a loading
 * screen to be used at the start of the game or between levels.  Feel free to adopt
 * this to your needs.
 *
 * You will note that this mode has some textures that are not loaded by the AssetManager.
 * You are never required to load through the AssetManager.  But doing this will block
 * the application.  That is why we try to have as few resources as possible for this
 * loading screen.
 */
public class MenuMode implements Screen {
	private float alpha = 1;
	private float shiftAlpha = 1;
	private float camAlpha = 1;
	private ShapeRenderer sr = new ShapeRenderer(8);

	// There are TWO asset managers.  One to load the loading screen.  The other to load the assets
	/** Internal assets for this loading screen */
	private AssetDirectory internal;
	/** The actual assets to be loaded */
	private AssetDirectory assets;
	
	/** Background texture for start-up */
	private Texture background;
	/** Game title texture */
	private Texture title;
	/** Texture atlas to support a progress bar */
	private final Texture statusBar;

	/** Lore textures */
	private Texture loreframe;

	/** Lore Array */
	private ArrayList<Texture> loresArray = new ArrayList<>();
	private ArrayList<Texture> textsArray = new ArrayList<>();
	private int currentLore;
	private int currentText;
	
	// statusBar is a "texture atlas." Break it up into parts.
	/** Left cap to the status background (grey region) */
	private TextureRegion statusBkgLeft;
	/** Middle portion of the status background (grey region) */
	private TextureRegion statusBkgMiddle;
	/** Right cap to the status background (grey region) */
	private TextureRegion statusBkgRight;
	/** Left cap to the status forground (colored region) */
	private TextureRegion statusFrgLeft;
	/** Middle portion of the status forground (colored region) */
	private TextureRegion statusFrgMiddle;
	/** Right cap to the status forground (colored region) */
	private TextureRegion statusFrgRight;

	/** The menu music */
	public static Music menuIntro;
	public static Music menuLoop;
	public Music[] musics = new Music[2];

	/** Default budget for asset loader (do nothing but load 60 fps) */
	private static int DEFAULT_BUDGET = 15;
	/** Standard window size (for scaling) */
	private static int STANDARD_WIDTH  = 800;
	/** Standard window height (for scaling) */
	private static int STANDARD_HEIGHT = 700;
	/** Ratio of the bar width to the screen */
	private static float BAR_WIDTH_RATIO  = 0.66f;
	/** Ratio of the bar height to the screen */
	private static float BAR_HEIGHT_RATIO = 0.25f;	
	/** Ratio of the button size */
	private static float BUTTON_SCALE  = 0.75f;

    /** Ratio from distance between bottom of the middle button to bottom edge
     * to canvas height */
    private static float BOT_MID_RATIO = 0.1f;
    /** Ratio from the distance between the center of the middle button and the side buttons to canvas width*/
    private static float SIDE_RATIO = 0.2672f;
    /** Ratio from the bottom of the side buttons to bottom edge to canvas height */
    private static float BOT_SIDE_RATIO = 0.215f;
	
	/** Reference to GameCanvas created by the root */
	private GameCanvas canvas;
	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/** The width of the progress bar */
	private int width;
	/** The y-coordinate of the center of the progress bar */
	private int centerY;
	/** The x-coordinate of the center of the progress bar */
	private int centerX;
	/** The height of the canvas window (necessary since sprite origin != screen origin) */
	private int heightY;
	/** Scaling factor for when the student changes the resolution. */
	private float scale;
	/** The boundary of the world */
	protected Rectangle bounds;
	/** Width of the game world in Box2d units */
	protected static final float DEFAULT_WIDTH  = 32.0f;
	/** Height of the game world in Box2d units */
	protected static final float DEFAULT_HEIGHT = 18.0f;

	/** Current progress (0 to 1) of the asset manager */
	private float progress;
	/** The current state of the play button */
	private int   pressState;
	private int buttonPressed;
	/** The amount of time to devote to loading assets (as opposed to on screen hints, etc.) */
	private int   budget;

	/** Whether or not this player mode is still active */
	private boolean active;
	private boolean transition = false;

    /** The Stages needed for UI elements */
	private Stage menu;
	private Stage start;
	private Stage lore;
    /** The Table containing the buttons */
    private Table menuTable;
	private TextField.TextFieldStyle textStyle;
	private TextButton startButton;
	private TextButton[] menuButtons = new TextButton[3];
	private TextButton[] loreButtons = new TextButton[2];

    /** The InputListener for a menu button */
    private InputListener buttonListener;
	private SharedUI ui;

    private MenuState menuState = MenuState.LOADING;
	private MenuState previousState;

	private float[] volumes = new float[3];
	private float brightness = 1;

	/**
	 * Defines the menu UI button events
	 */
    class MenuListener extends InputListener {
        private TextButton textButton;

        public MenuListener(TextButton button) {
            this.textButton = button;
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            pressState = 1;
            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            if (pressState == 1 && x >= 0 && x < textButton.getWidth() && y >= 0 && y < textButton.getHeight()) {
				pressState = 2;
				if (textButton == menuButtons[0]) {
					setState(MenuState.LORE);
					pressState = 0;
				} else if (textButton == menuButtons[1]) {
					setState(MenuState.SETTINGS);
					pressState = 0;
				} else if (textButton == menuButtons[2]) {
					buttonPressed = 1;
				} else if (textButton == startButton) {
					setState(MenuState.MENU);
					pressState = 0;
				} else if (textButton == loreButtons[0]) {
					if (currentText < textsArray.size()-1) {
						currentText++;
						if (currentText >= 3 && currentText < 9) {
							currentLore++;
							if (currentLore == 6) {
								camAlpha = 0;
							}
						}
						pressState = 0;
					} else {
						buttonPressed = 0;
					}
				} else if (textButton == loreButtons[1]) {
					buttonPressed = 0;
				}
            } else {
                pressState = 0;
            }
            super.touchUp(event, x, y, pointer ,button);
        }

        @Override
        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            textButton.setStyle(ui.getHoverStyle());
            super.enter(event, x, y, pointer, fromActor);
        }

        @Override
        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            textButton.setStyle(ui.getButtonStyle());
            super.exit(event, x, y, pointer, toActor);

        }
    }

	/**
	 * Returns the budget for the asset loader.
	 *
	 * The budget is the number of milliseconds to spend loading assets each animation
	 * frame.  This allows you to do something other than load assets.  An animation 
	 * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to 
	 * do something else.  This is how game companies animate their loading screens.
	 *
	 * @return the budget in milliseconds
	 */
	public int getBudget() {
		return budget;
	}

	/**
	 * Sets the budget for the asset loader.
	 *
	 * The budget is the number of milliseconds to spend loading assets each animation
	 * frame.  This allows you to do something other than load assets.  An animation 
	 * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to 
	 * do something else.  This is how game companies animate their loading screens.
	 *
	 * @param millis the budget in milliseconds
	 */
	public void setBudget(int millis) {
		budget = millis;
	}
	
	/**
	 * Returns true if all assets are loaded and the player is ready to go.
	 *
	 * @return true if the player is ready to go
	 */
	public boolean isReady() {
		return pressState == 2;
	}

	/**
	 * Returns the asset directory produced by this loading screen
	 *
	 * This asset loader is NOT owned by this loading scene, so it persists even
	 * after the scene is disposed.  It is your responsbility to unload the
	 * assets in this directory.
	 *
	 * @return the asset directory produced by this loading screen
	 */
	public AssetDirectory getAssets() {
		return assets;
	}

	/**
	 * Creates a LoadingMode with the default budget, size and position.
	 *
	 * @param file  	The asset directory to load in the background
	 * @param canvas 	The game canvas to draw to
	 */
	public MenuMode(String file, GameCanvas canvas) {
		this(file, canvas, DEFAULT_BUDGET);
	}

	/**
	 * Creates a LoadingMode with the default size and position.
	 *
	 * The budget is the number of milliseconds to spend loading assets each animation
	 * frame.  This allows you to do something other than load assets.  An animation 
	 * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to 
	 * do something else.  This is how game companies animate their loading screens.
	 *
	 * @param file  	The asset directory to load in the background
	 * @param canvas 	The game canvas to draw to
	 * @param millis The loading budget in milliseconds
	 */
	public MenuMode(String file, GameCanvas canvas, int millis) {
		this.canvas  = canvas;
		this.bounds = new Rectangle(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT);
		ui = SharedUI.getInstance();
		budget = millis;
        menu = new Stage();
		start = new Stage();
		lore = new Stage();

		volumes[0] = 1;
		volumes[1] = 1;
		volumes[2] = 1;

        // Compute the dimensions from the canvas
		resize(canvas.getWidth(),canvas.getHeight());

		// We need these files loaded immediately
		internal = new AssetDirectory( "loading.json" );
		internal.loadAssets();
		internal.finishLoading();

		// Load the next two images immediately.
		background = internal.getEntry( "background", Texture.class );
		background.setFilter( TextureFilter.Linear, TextureFilter.Linear );
		title = internal.getEntry("title", Texture.class);
		title.setFilter( TextureFilter.Linear, TextureFilter.Linear );
		statusBar = internal.getEntry( "progress", Texture.class );

		// Load the load images and texts
		loresArray.add(internal.getEntry( "lore1", Texture.class ));
		loresArray.add(internal.getEntry( "lore2", Texture.class ));
		loresArray.add(internal.getEntry( "lore3", Texture.class ));
		loresArray.add(internal.getEntry( "lore4", Texture.class ));
		loresArray.add(internal.getEntry( "lore5", Texture.class ));
		loresArray.add(internal.getEntry( "lore6", Texture.class ));
		loresArray.add(internal.getEntry( "lore7", Texture.class ));
		loreframe = internal.getEntry( "loreframe", Texture.class );
		textsArray.add(internal.getEntry( "text", Texture.class ));
		textsArray.add(internal.getEntry( "text1", Texture.class ));
		textsArray.add(internal.getEntry( "text2", Texture.class ));
		textsArray.add(internal.getEntry( "text3", Texture.class ));
		textsArray.add(internal.getEntry( "text4", Texture.class ));
		textsArray.add(internal.getEntry( "text5", Texture.class ));
		textsArray.add(internal.getEntry( "text6", Texture.class ));
		textsArray.add(internal.getEntry( "text7", Texture.class ));
		textsArray.add(internal.getEntry( "text8", Texture.class ));
		textsArray.add(internal.getEntry( "text9", Texture.class ));
		textsArray.add(internal.getEntry( "text10", Texture.class ));

		// Load music
		menuIntro = internal.getEntry( "menu:intro", Music.class );
		menuLoop = internal.getEntry( "menu:loop", Music.class );
		musics[0] = menuIntro;
		musics[1] = menuLoop;

		// Break up the status bar texture into regions
		statusBkgLeft = internal.getEntry( "progress.backleft", TextureRegion.class );
		statusBkgRight = internal.getEntry( "progress.backright", TextureRegion.class );
		statusBkgMiddle = internal.getEntry( "progress.background", TextureRegion.class );

		statusFrgLeft = internal.getEntry( "progress.foreleft", TextureRegion.class );
		statusFrgRight = internal.getEntry( "progress.foreright", TextureRegion.class );
		statusFrgMiddle = internal.getEntry( "progress.foreground", TextureRegion.class );
//        font = internal.getEntry( "montserrat:regular" ,BitmapFont.class);

		// No progress so far.
		progress = 0;
		pressState = 0;

        // load Actors for menu UI
        menuTable = new Table();
        menuTable.setFillParent(true);
        menu.addActor(menuTable);

		Table loreTable = new Table();
		loreTable.setFillParent(true);
		lore.addActor(loreTable);

//        table.setDebug(true);

		// define font style generator
		textStyle = new TextField.TextFieldStyle();
		textStyle.font = ui.getFont();
		textStyle.fontColor = new Color(255,255,255,0.8f);

		// define start button
		startButton = new TextButton("PRESS TO START", ui.getButtonStyle());
		startButton.setBounds(0, 0, startButton.getWidth(), startButton.getHeight());
		startButton.addListener(new MenuListener(startButton));
		startButton.getLabel().setAlignment(Align.top);
		startButton.moveBy(Gdx.graphics.getWidth()/2-startButton.getWidth()/2,200);
		start.addActor(startButton);

        // define menu buttons
		menuButtons[0] = new TextButton("NEW GAME", ui.getButtonStyle());
		menuButtons[0].setBounds(0, 0, menuButtons[0].getWidth(), menuButtons[0].getHeight());
		menuButtons[0].addListener(new MenuListener(menuButtons[0]));
		menuButtons[0].getLabel().setAlignment(Align.top);
		menuButtons[1] = new TextButton("OPTIONS", ui.getButtonStyle());
		menuButtons[1].setBounds(0, 0, menuButtons[1].getWidth(), menuButtons[1].getHeight());
		menuButtons[1].addListener(new MenuListener(menuButtons[1]));
		menuButtons[1].getLabel().setAlignment(Align.topRight);
		menuButtons[2] = new TextButton("LEVELS", ui.getButtonStyle());
		menuButtons[2].setBounds(0, 0, menuButtons[2].getWidth(), menuButtons[2].getHeight());
		menuButtons[2].addListener(new MenuListener(menuButtons[2]));
		menuButtons[2].getLabel().setAlignment(Align.topLeft);

		// define lore button
		loreButtons[0] = new TextButton("CONTINUE", ui.getButtonStyle());
		loreButtons[0].setBounds(0, 0, loreButtons[0].getWidth(), loreButtons[0].getHeight());
		loreButtons[0].addListener(new MenuListener(loreButtons[0]));
		loreButtons[1] = new TextButton("SKIP", ui.getButtonStyle());
		loreButtons[1].setBounds(0, 0, loreButtons[1].getWidth(), loreButtons[1].getHeight());
		loreButtons[1].addListener(new MenuListener(loreButtons[1]));

        // position buttons in Table
        menuTable.bottom();
		menuTable.add(menuButtons[1]).bottom().right().expand()
				.padRight((SIDE_RATIO)*canvas.getWidth()-menuButtons[1].getWidth()/2-menuButtons[1].getWidth()/2)
				.padBottom(BOT_SIDE_RATIO*canvas.getHeight());
		menuTable.add(menuButtons[0]).padBottom(canvas.getHeight() * BOT_MID_RATIO).bottom();
		menuTable.add(menuButtons[2]).left().bottom().expand()
				.padLeft((SIDE_RATIO)*canvas.getWidth()-menuButtons[2].getWidth()/2-menuButtons[2].getWidth()/2)
				.padBottom(BOT_SIDE_RATIO*canvas.getHeight());

		loreTable.bottom().padTop(canvas.getHeight()).padLeft(canvas.getHeight()/20f).padRight(canvas.getHeight()/20f).padBottom(canvas.getHeight()/25f);
		loreTable.add(loreButtons[0]).left().expand();
		loreTable.add(loreButtons[1]).right();

		// Start loading the real assets
		assets = new AssetDirectory( file );
		assets.loadAssets();
		active = true;

		if(menuLoop.isPlaying()){
			menuLoop.stop();
		}

		if(menuIntro.isPlaying()){
			menuIntro.stop();
		}
		if(easyLoop != null && easyLoop.isPlaying()){
			easyLoop.stop();
		}

		if(easyIntro != null && easyIntro.isPlaying()){
			easyIntro.stop();
		}

		menuIntro.setOnCompletionListener(new Music.OnCompletionListener() {

			@Override
			public void onCompletion(Music music) {
				menuLoop.setLooping(true);
				menuLoop.setVolume(1);//volumes[0]*volumes[1]);
				menuLoop.play();
			}
		});
	}

	public void updateSettings(float[] volumes, float brightness) {
		this.volumes = volumes;
		this.brightness = brightness;
		for (Music m : musics) {
			m.setVolume(1);//volumes[0]*volumes[1]);
		}
	}

	public void updateVolume(int n, float volume) {
		volumes[n] = volume;
		if (n != 2) {
			for (Music m : musics) {
				m.setVolume(1);//volumes[0]*volumes[1]);
			}
		}
	}

	public float[] getVolumes() {
		return volumes;
	}

	public float getBrightness() {
		return brightness;
	}
	
	/**
	 * Called when this screen should release all resources.
	 */
	public void dispose() {
		internal.unloadAssets();
		internal.dispose();
        menu.dispose();
		bounds = null;
	}
	
	/**
	 * Update the status of this player mode.
	 *
	 * We prefer to separate update and draw from one another as separate methods, instead
	 * of using the single render() method that LibGDX does.  We will talk about why we
	 * prefer this in lecture.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	private void update(float delta) {
		if (menuState == MenuState.LOADING) {
			assets.update(budget);
			this.progress = assets.getProgress();
			if (progress >= 1.0f) {
				this.progress = 1.0f;
                setState(MenuState.START);
			}
		} else if (menuState == MenuState.START) {
			InputController input = InputController.getInstance();
			input.readInput(bounds, new Vector2(scale, scale));
			if (input.didAnyKey()) {
				setState(MenuState.MENU);
			}
		} else if ((menuState == MenuState.SETTINGS || menuState == MenuState.CONTROLS || menuState == MenuState.AUDIO_DISPLAY || menuState == MenuState.CREDITS) && ui.getPressState() == 2) {
			TextButton b = ui.getPressedButton();
			if (b == ui.getControlSettingsButtons()[7] || b == ui.getAudioDisplaySettingsButtons()[4] || b == ui.getCreditsButtons()[1]) {
				setState(MenuState.SETTINGS);
				ui.setState(SharedUI.Overlay.SETTINGS);
				pressState = 0;
			}

			switch (ui.getOverlay()) {
				case SETTINGS:
					if (b == ui.getSettingsButtons()[0]) {
						setState(MenuState.CONTROLS);
						ui.setState(SharedUI.Overlay.CONTROLS);
						pressState = 0;
					} else if (b == ui.getSettingsButtons()[1]) {
						setState(MenuState.AUDIO_DISPLAY);
						ui.setState(SharedUI.Overlay.AUDIO_DISPLAY);
						pressState = 0;
					} else if (b == ui.getSettingsButtons()[2]) {
						setState(MenuState.CREDITS);
						ui.setState(SharedUI.Overlay.CREDITS);
						pressState = 0;
					} else if (b == ui.getSettingsButtons()[3]) {
						setState(MenuState.MENU);
						ui.reset();
						pressState = 0;
					} else if (b == ui.getSettingsButtons()[4]) {
						setState(MenuState.MENU);
						ui.reset();
						pressState = 2;
						buttonPressed = -1;
					}
					break;
				case CONTROLS:
					pressState = 0;
					break;
				case AUDIO_DISPLAY:
					if (ui.getMasterDragged()) {
						updateVolume(0, ui.getMasterVolume());
					} else if (ui.getMusicDragged()) {
						updateVolume(1, ui.getMusicVolume());
					} else if (ui.getSFXDragged()) {
						updateVolume(2, ui.getSFXVolume());
					} else if (ui.getBrightnessDragged()) {
						brightness = ui.getBrightness();
					}
					pressState = 0;
					break;
			}
		}
	}

	/**
	 * Draw the status of this player mode.
	 *
	 * We prefer to separate update and draw from one another as separate methods, instead
	 * of using the single render() method that LibGDX does.  We will talk about why we
	 * prefer this in lecture.
	 */
	private void draw() {
		Color c;
		if (menuState == MenuState.START || menuState == MenuState.LOADING) {
			c = Color.BLACK;
		} else if (menuState == MenuState.MENU && previousState == MenuState.SETTINGS) {
			c = new Color(1 - alpha, 1 - alpha, 1 - alpha, 1 - 0.8f);
		} else if (menuState == MenuState.MENU || menuState == MenuState.LORE) {
			c = new Color(1 - alpha, 1 - alpha, 1 - alpha, 1);
		} else {
			c = new Color(alpha, alpha, alpha,1);
		}

		if (menuState == MenuState.LORE && currentLore == 0 && camAlpha > 0.5f) {
			camAlpha -= 0.001;
		} else if (menuState == MenuState.LORE && currentLore == 6 && camAlpha < 1) {
			camAlpha += 0.002;
		}

		if (!transition && (menuState == MenuState.MENU || menuState == MenuState.LORE) && alpha > 0) {
			alpha -= 0.03;
		} else if (menuState == MenuState.SETTINGS && alpha > 0.2f) {
			alpha -= 0.8*0.03;
		}

		if (menuState == MenuState.MENU && previousState == MenuState.SETTINGS && shiftAlpha < 1) {
			shiftAlpha += 0.02;
		} else if (menuState == MenuState.SETTINGS && previousState == MenuState.MENU && shiftAlpha > 0.3f) {
			shiftAlpha -= 0.02;
		} else if (menuState == MenuState.SETTINGS && previousState != MenuState.MENU && shiftAlpha < 0.3f) {
			shiftAlpha += 0.01;
		} else if (menuState == MenuState.CONTROLS && shiftAlpha > 0.1f) {
			shiftAlpha -= 0.01;
		} else if (menuState == MenuState.AUDIO_DISPLAY && previousState != MenuState.SETTINGS && shiftAlpha > 0.3f) {
			shiftAlpha -= 0.01;
		} else if (menuState == MenuState.CREDITS && shiftAlpha > 0) {
			shiftAlpha -= 0.01;
		}

		canvas.begin();

		if (menuState == MenuState.SETTINGS) {
			canvas.draw(background, c, 0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight());
		} else if (menuState != MenuState.LORE) {
			canvas.draw(background, c, 0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight());
		} else {
			if (currentLore == 0) {
				canvas.draw(loresArray.get(currentLore), c, -canvas.getWidth()*(1-camAlpha)/2, -canvas.getHeight()*(1-camAlpha)/2, (float) canvas.getWidth()*(2-camAlpha), (float) canvas.getHeight()*(2-camAlpha));
			} else if (currentLore == 6) {
				canvas.draw(loresArray.get(currentLore), c, -canvas.getWidth()*camAlpha*0.1f, -canvas.getHeight()*0.1f, (float) canvas.getWidth()*1.1f, (float) canvas.getHeight()*1.1f);
			} else {
				canvas.draw(loresArray.get(currentLore), c, 0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight());
			}
			canvas.draw(loreframe, c, 0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight());
			canvas.draw(textsArray.get(currentText), c, (float) canvas.getWidth()/2-textsArray.get(currentText).getWidth()/3, 27.5f, textsArray.get(currentText).getWidth()/1.5f, 35);
		}

		if (menuState == MenuState.MENU && previousState == MenuState.SETTINGS || menuState == MenuState.SETTINGS || menuState == MenuState.CONTROLS || menuState == MenuState.AUDIO_DISPLAY || menuState == MenuState.CREDITS) {
			canvas.draw(title, (canvas.getWidth() - title.getWidth()) / 2.0f, (canvas.getHeight() - title.getHeight()) / 2.0f + (1-shiftAlpha) * 240);
		} else if (menuState == MenuState.LOADING || menuState == MenuState.START ||  menuState == MenuState.MENU) {
			canvas.draw(title, (canvas.getWidth() - title.getWidth()) / 2.0f, (canvas.getHeight() - title.getHeight()) / 2.0f);
		}

		if (menuState == MenuState.LOADING) {
			drawProgress(canvas);
		}

		canvas.end();
	}
	
	/**
	 * Updates the progress bar according to loading progress
	 *
	 * The progress bar is composed of parts: two rounded caps on the end, 
	 * and a rectangle in a middle.  We adjust the size of the rectangle in
	 * the middle to represent the amount of progress.
	 *
	 * @param canvas The drawing context
	 */	
	private void drawProgress(GameCanvas canvas) {	
		canvas.draw(statusBkgLeft,   Color.WHITE, centerX-width/2, centerY,
				scale*statusBkgLeft.getRegionWidth(), scale*statusBkgLeft.getRegionHeight());
		canvas.draw(statusBkgRight,  Color.WHITE,centerX+width/2-scale*statusBkgRight.getRegionWidth(), centerY,
				scale*statusBkgRight.getRegionWidth(), scale*statusBkgRight.getRegionHeight());
		canvas.draw(statusBkgMiddle, Color.WHITE,centerX-width/2+scale*statusBkgLeft.getRegionWidth(), centerY,
				width-scale*(statusBkgRight.getRegionWidth()+statusBkgLeft.getRegionWidth()),
				scale*statusBkgMiddle.getRegionHeight());

		canvas.draw(statusFrgLeft,   Color.WHITE,centerX-width/2, centerY,
				scale*statusFrgLeft.getRegionWidth(), scale*statusFrgLeft.getRegionHeight());
		if (progress > 0) {
			float span = progress*(width-scale*(statusFrgLeft.getRegionWidth()+statusFrgRight.getRegionWidth()))/2.0f;
			canvas.draw(statusFrgRight,  Color.WHITE,centerX-width/2+scale*statusFrgLeft.getRegionWidth()+span, centerY,
					scale*statusFrgRight.getRegionWidth(), scale*statusFrgRight.getRegionHeight());
			canvas.draw(statusFrgMiddle, Color.WHITE,centerX-width/2+scale*statusFrgLeft.getRegionWidth(), centerY,
					span, scale*statusFrgMiddle.getRegionHeight());
		} else {
			canvas.draw(statusFrgRight,  Color.WHITE,centerX-width/2+scale*statusFrgLeft.getRegionWidth(), centerY,
					scale*statusFrgRight.getRegionWidth(), scale*statusFrgRight.getRegionHeight());
		}
	}

	// ADDITIONAL SCREEN METHODS
	/**
	 * Called when the Screen should render itself.
	 *
	 * We defer to the other methods update() and draw().  However, it is VERY important
	 * that we only quit AFTER a draw.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void render(float delta) {
		if (active) {
			update(delta);
			draw();
			if (menuState == MenuState.START) {
				start.act(delta);
				start.draw();
			} else if (menuState == MenuState.MENU) {
                menu.act(delta);
                menu.draw();
			} else if (menuState == MenuState.SETTINGS) {
				ui.getSettings().act(delta);
				ui.getSettings().draw();
			} else if (menuState == MenuState.CONTROLS) {
				ui.getControlSettings().act(delta);
				ui.getControlSettings().draw();
			} else if (menuState == MenuState.AUDIO_DISPLAY) {
				ui.getAudioDisplaySettings().act(delta);
				ui.getAudioDisplaySettings().draw();
			} else if (menuState == MenuState.CREDITS) {
				ui.getCredits().act(delta);
				ui.getCredits().draw();
			} else if (menuState == MenuState.LORE) {
				lore.act(delta);
				lore.draw();
			}

			if (isReady()) {
				Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
				sr.setColor(0, 0, 0, alpha);
				sr.begin(ShapeRenderer.ShapeType.Filled);
				sr.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				sr.end();
				Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

				if (!transition) {
					transition = true;
					alpha = 0;
				} else if (alpha >= 1 && listener != null) {
					pressState = 0;
					transition = false;
					listener.exitScreen(this, buttonPressed);
				} else {
					alpha += buttonPressed == 0 ? 0.01 : 0.02;
				}
			}

			// brightness
			if (brightness < 1) {
				Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
				sr.setColor(0, 0, 0, 1-brightness);
				sr.begin(ShapeRenderer.ShapeType.Filled);
				sr.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				sr.end();
				Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
			}
		}
	}

	/**
	 * Called when the Screen is resized. 
	 *
	 * This can happen at any point during a non-paused state but will never happen 
	 * before a call to show().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		// Compute the drawing scale
		float sx = ((float)width)/STANDARD_WIDTH;
		float sy = ((float)height)/STANDARD_HEIGHT;
		scale = (sx < sy ? sx : sy);
		
		this.width = (int)(BAR_WIDTH_RATIO*width);
		centerY = (int)(BAR_HEIGHT_RATIO*height);
		centerX = width/2;
		heightY = height;

        menu.getViewport().update(width, height, true);
	}

    /**
	 * Called when the Screen is paused.
	 * 
	 * This is usually when it's not active or visible on screen. An Application is 
	 * also paused before it is destroyed.
	 */
	public void pause() {
		// TODO Auto-generated method stub

	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 *
	 * This is usually when it regains focus.
	 */
	public void resume() {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Called when this screen becomes the current screen for a Game.
	 */
	public void show() {
		// Useless if called in outside animation loop
		active = true;
	}

	/**
	 * Called when this screen is no longer the current screen for a Game.
	 */
	public void hide() {
		// Useless if called in outside animation loop
		active = false;
	}

	/**
	 * Sets the ScreenListener for this mode
	 *
	 * The ScreenListener will respond to requests to quit.
	 */
	public void setScreenListener(ScreenListener listener) {
		this.listener = listener;
	}

	public void setCanvas(GameCanvas canvas) {
		this.canvas = canvas;
	}

	public void setState(MenuState s) {
		previousState = menuState;
		switch (s) {
			case START:
				Gdx.input.setInputProcessor(start);
				break;
			case MENU:
				alpha = 1;

				if (menuState == MenuState.SETTINGS) {
					shiftAlpha = 0.3f;
				} else {
					menuIntro.setVolume(1);//volumes[0]*volumes[1]);
					menuIntro.play();
				}
				Gdx.input.setInputProcessor(menu);
				break;
			case LORE:
				alpha = 1;
				currentLore = 0;
				currentText = 0;
				Gdx.input.setInputProcessor(lore);
				break;
			case SETTINGS:
				if (previousState == MenuState.MENU) {
					alpha = 1;
					shiftAlpha = 1;
				}

				Gdx.input.setInputProcessor(ui.getSettings());
				break;
			case CONTROLS:
				Gdx.input.setInputProcessor(new InputMultiplexer(ui.getControlSettings(), ui.getButtonInputProcessor()));
				break;
			case AUDIO_DISPLAY:
				Gdx.input.setInputProcessor(ui.getAudioDisplaySettings());
				break;
			case CREDITS:
				Gdx.input.setInputProcessor(ui.getCredits());
				break;
			default:
				break;
		}
		menuState = s;
	}

	public enum MenuState {
		LOADING, START, MENU, SETTINGS, CONTROLS, AUDIO_DISPLAY, CREDITS, LORE
	}
}