package com.mygdx.technicalprototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.mygdx.technicalprototype.assets.AssetDirectory;
import com.mygdx.technicalprototype.util.ScreenListener;

public class LevelSelectMode implements Screen {
    private float alpha = 1;
    private ShapeRenderer sr = new ShapeRenderer(8);

    /** Internal assets for this loading screen */
    private AssetDirectory internal;
    /** The actual assets to be loaded */
    private AssetDirectory assets;

    /** Background texture for start-up */
    private Texture background;
    private Texture easyBackground;
    private Texture mediumBackground;
    private Texture hardBackground;

    /** The font for the menu buttons */
    private BitmapFont smallFont;
    private BitmapFont largeFont;

    /** The menu music */
    private Music menuIntro;
    private Music menuLoop;
    private Music[] musics = new Music[2];

    /** Default budget for asset loader (do nothing but load 60 fps) */
    private static int DEFAULT_BUDGET = 15;
    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;

    /** Ratio from distance between bottom of the middle button to bottom edge
     * to canvas height */
    private static float BOT_MID_RATIO = 0.1f;
    /** Ratio from the center of the side buttons to the left/right edge to canvas height*/
    private static float SIDE_RATIO = 0.18f;
    /** Ratio from the bottom of the side buttons to bottom edge to canvas height */
    private static float BOT_SIDE_RATIO = 0.20885f;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** The current state of the play button */
    private int   pressState;
    private int buttonPressed;
    /** The amount of time to devote to loading assets (as opposed to on screen hints, etc.) */
    private int   budget;

    /** Whether or not this player mode is still active */
    private boolean active;
    private boolean transition = false;

    private Difficulty difficulty;

    private Stage currentStage;
    private Stage difficultyStage;
    /** The main menu button text style */
    private TextButton.TextButtonStyle buttonStyle;
    /** The text style when the mouse hovers over a UI button*/
    private TextButton.TextButtonStyle hoverStyle;
    /** Level buttons */
    private TextButton[] levelButtons;
    private ImageButton easyButton;
    private ImageButton mediumButton;
    private ImageButton hardButton;
    private ImageButton backButton;
    /** The InputListener for a menu button */
    private InputListener buttonListener;

    private float[] volumes = new float[3];
    private float brightness = 1;

    class MenuListener extends InputListener {
        private TextButton textButton;
        private ImageButton imageButton;

        public MenuListener(TextButton button) {
            this.textButton = button;
        }

        public MenuListener(ImageButton button) {
            this.imageButton = button;
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            pressState = 1;
            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            if (difficulty == null && pressState == 1 && x >= 0 && x < imageButton.getWidth() && y >= 0 && y < imageButton.getHeight()) {
                pressState = 0;

                if (imageButton == backButton) {
                    pressState = 2;
                    buttonPressed = 0;
                } else {
                    if (imageButton == easyButton) {
                        difficulty = Difficulty.EASY;
                        buttonPressed = 0;
                    } else if (imageButton == mediumButton) {
                        difficulty = Difficulty.MEDIUM;
                        buttonPressed = 7;
                    } else if (imageButton == hardButton) {
                        difficulty = Difficulty.HARD;
                        buttonPressed = 15;
                    }
                    selectDifficulty();
                    setStage();
                }
            } else if (pressState == 1 && imageButton == backButton && x >= 0 && x < imageButton.getWidth() && y >= 0 && y < imageButton.getHeight()) {
                difficulty = null;
                pressState = 0;
                selectDifficulty();
                setStage();
            } else if (pressState == 1 && x >= 0 && x < textButton.getWidth() && y >= 0 && y < textButton.getHeight()) {
                pressState = 2;

                switch (textButton.getText().toString()) {
                    case "I":
                        buttonPressed += 1;
                        break;
                    case "II":
                        buttonPressed += 2;
                        break;
                    case "III":
                        buttonPressed += 3;
                        break;
                    case "IV":
                        buttonPressed += 4;
                        break;
                    case "V":
                        buttonPressed += 5;
                        break;
                    case "VI":
                        buttonPressed += 6;
                        break;
                    case "VII":
                        buttonPressed += 7;
                        break;
                    case "VIII":
                        buttonPressed += 8;
                        break;
                    default:
                        buttonPressed = -1;
                        break;
                }
            } else {
                pressState = 0;
            }
            super.touchUp(event, x, y, pointer ,button);
        }

        @Override
        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            if (difficulty == null && imageButton != backButton) {
                Table difficultyTable = new Table();
                difficultyTable.setFillParent(true);
                difficultyTable.center();
                if (imageButton == easyButton) {
                    difficultyTable.add(easyButton).size(450);
                    difficultyTable.add(mediumButton).size(400);
                    difficultyTable.add(hardButton).size(400);
                } else if (imageButton == mediumButton) {
                    difficultyTable.add(easyButton).size(400);
                    difficultyTable.add(mediumButton).size(450);
                    difficultyTable.add(hardButton).size(400);
                } else {
                    difficultyTable.add(easyButton).size(400);
                    difficultyTable.add(mediumButton).size(400);
                    difficultyTable.add(hardButton).size(450);
                }
                difficultyStage.addActor(difficultyTable);
            } else if (difficulty != null && imageButton != backButton) {
                textButton.setStyle(hoverStyle);
            } else {
                backButton.setColor(new Color(255,255,255,1));
            }
            super.enter(event, x, y, pointer, fromActor);
        }

        @Override
        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            if (difficulty == null && imageButton != backButton) {
                Table difficultyTable = new Table();
                difficultyTable.setFillParent(true);
                difficultyTable.center();
                difficultyTable.add(easyButton).size(400);
                difficultyTable.add(mediumButton).size(400);
                difficultyTable.add(hardButton).size(400);
                difficultyStage.addActor(difficultyTable);
            } else if (difficulty != null && imageButton != backButton) {
                textButton.setStyle(buttonStyle);
            } else {
                backButton.setColor(new Color(255,255,255,0.8f));
            }
            super.exit(event, x, y, pointer, toActor);

        }
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

    public LevelSelectMode(GameCanvas canvas, int millis) {
        this.canvas = canvas;
        budget = millis;
        difficultyStage = new Stage();
        currentStage = difficultyStage;

        Table difficultyTable = new Table();
        difficultyTable.setFillParent(true);

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());

        // We need these files loaded immediately
        internal = new AssetDirectory( "loading.json" );
        internal.loadAssets();
        internal.finishLoading();

        // Load the next two images immediately.
        background = internal.getEntry( "levelselect:background", Texture.class );
        background.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );
        easyBackground = internal.getEntry( "levelselect:easybg", Texture.class );
        easyBackground.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );
        mediumBackground = internal.getEntry( "levelselect:mediumbg", Texture.class );
        mediumBackground.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );
        hardBackground = internal.getEntry( "levelselect:hardbg", Texture.class );
        hardBackground.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );

        backButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(internal.getEntry("levelselect:back", Texture.class))));
        backButton.setBounds(0, 0, backButton.getWidth(), backButton.getHeight());
        backButton.addListener(new MenuListener(backButton));
        backButton.setPosition(0, 650);
        backButton.setColor(new Color(255, 255, 255, 0.8f));
        easyButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(internal.getEntry( "levelselect:easy", Texture.class ))));
        easyButton.setBounds(0, 0, easyButton.getWidth(), easyButton.getHeight());
        easyButton.addListener(new MenuListener(easyButton));
        mediumButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(internal.getEntry( "levelselect:medium", Texture.class ))));
        mediumButton.setBounds(0, 0, mediumButton.getWidth(), mediumButton.getHeight());
        mediumButton.addListener(new MenuListener(mediumButton));
        hardButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(internal.getEntry( "levelselect:hard", Texture.class ))));
        hardButton.setBounds(0, 0, hardButton.getWidth(), hardButton.getHeight());
        hardButton.addListener(new MenuListener(hardButton));
        difficultyTable.center();
        difficultyTable.add(easyButton).size(400);
        difficultyTable.add(mediumButton).size(400);
        difficultyTable.add(hardButton).size(400);
        difficultyStage.addActor(difficultyTable);
        difficultyStage.addActor(backButton);

        // Load music
        menuIntro = internal.getEntry( "menu:intro", Music.class );
        menuLoop = internal.getEntry( "menu:loop", Music.class );
        musics[0] = menuIntro;
        musics[1] = menuLoop;

        smallFont = internal.getEntry( "montserratalternates:light-small" , BitmapFont.class);
        largeFont = internal.getEntry( "montserratalternates:light-large" , BitmapFont.class);

        pressState = 0;

        // load Actors for menu UI
        setStage();

        active = true;
    }

    private void selectDifficulty() {
        if (difficulty == null) {
            currentStage = difficultyStage;
            currentStage.addActor(backButton);
            return;
        }

        currentStage = new Stage();

        if (difficulty == Difficulty.EASY) {
            buttonStyle = new TextButton.TextButtonStyle();
            buttonStyle.font = largeFont;
            hoverStyle = new TextButton.TextButtonStyle(buttonStyle);
            hoverStyle.fontColor = Color.LIGHT_GRAY;

            createButtons();
            levelButtons[0].setPosition(70, 290);
            levelButtons[1].setPosition(226,565);
            levelButtons[2].setPosition(321,116);
            levelButtons[3].setPosition(713,277);
            levelButtons[4].setPosition(910,532);
            levelButtons[5].setPosition(894,55);
            levelButtons[6].setPosition(1160,320);

            currentStage.addActor(levelButtons[6]);
        } else if (difficulty == Difficulty.MEDIUM) {
            buttonStyle = new TextButton.TextButtonStyle();
            buttonStyle.font = smallFont;
            hoverStyle = new TextButton.TextButtonStyle(buttonStyle);
            hoverStyle.fontColor = Color.LIGHT_GRAY;

            createButtons();
            levelButtons[0].setPosition(46, 314);
            levelButtons[1].setPosition(201,83);
            levelButtons[2].setPosition(305,545);
            levelButtons[3].setPosition(505,210);
            levelButtons[4].setPosition(821,380);
            levelButtons[5].setPosition(849,84);
            levelButtons[6].setPosition(1145,277);
            levelButtons[7].setPosition(984,563);

            currentStage.addActor(levelButtons[6]);
            currentStage.addActor(levelButtons[7]);
        } else {
            buttonStyle = new TextButton.TextButtonStyle();
            buttonStyle.font = largeFont;
            hoverStyle = new TextButton.TextButtonStyle(buttonStyle);
            hoverStyle.fontColor = Color.LIGHT_GRAY;

            createButtons();
            levelButtons[0].setPosition(102, 274);
            levelButtons[1].setPosition(362,520);
            levelButtons[2].setPosition(360,73);
            levelButtons[3].setPosition(760,335);
            levelButtons[4].setPosition(942,90);
            levelButtons[5].setPosition(1146,433);
        }
        currentStage.addActor(levelButtons[0]);
        currentStage.addActor(levelButtons[1]);
        currentStage.addActor(levelButtons[2]);
        currentStage.addActor(levelButtons[3]);
        currentStage.addActor(levelButtons[4]);
        currentStage.addActor(levelButtons[5]);
        currentStage.addActor(backButton);
    }

    private void createButtons() {
        levelButtons = new TextButton[8];

        levelButtons[0] = new TextButton("I", buttonStyle);
        levelButtons[0].setBounds(0, 0, levelButtons[0].getWidth(), levelButtons[0].getHeight());
        levelButtons[0].addListener(new MenuListener(levelButtons[0]));

        levelButtons[1] = new TextButton("II", buttonStyle);
        levelButtons[1].setBounds(0, 0, levelButtons[1].getWidth(), levelButtons[1].getHeight());
        levelButtons[1].addListener(new MenuListener(levelButtons[1]));

        levelButtons[2] = new TextButton("III", buttonStyle);
        levelButtons[2].setBounds(0, 0, levelButtons[2].getWidth(), levelButtons[2].getHeight());
        levelButtons[2].addListener(new MenuListener(levelButtons[2]));

        levelButtons[3] = new TextButton("IV", buttonStyle);
        levelButtons[3].setBounds(0, 0, levelButtons[3].getWidth(), levelButtons[3].getHeight());
        levelButtons[3].addListener(new MenuListener(levelButtons[3]));

        levelButtons[4] = new TextButton("V", buttonStyle);
        levelButtons[4].setBounds(0, 0, levelButtons[4].getWidth(), levelButtons[4].getHeight());
        levelButtons[4].addListener(new MenuListener(levelButtons[4]));

        levelButtons[5] = new TextButton("VI", buttonStyle);
        levelButtons[5].setBounds(0, 0, levelButtons[5].getWidth(), levelButtons[5].getHeight());
        levelButtons[5].addListener(new MenuListener(levelButtons[5]));

        levelButtons[6] = new TextButton("VII", buttonStyle);
        levelButtons[6].setBounds(0, 0, levelButtons[6].getWidth(), levelButtons[6].getHeight());
        levelButtons[6].addListener(new MenuListener(levelButtons[6]));

        levelButtons[7] = new TextButton("VIII", buttonStyle);
        levelButtons[7].setBounds(0, 0, levelButtons[7].getWidth(), levelButtons[7].getHeight());
        levelButtons[7].addListener(new MenuListener(levelButtons[7]));
    }

    public void updateSettings(float[] volumes, float brightness) {
        this.volumes = volumes;
        this.brightness = brightness;
        for (Music m : musics) {
            m.setVolume(1);//volumes[0]*volumes[1]);
        }
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        internal.unloadAssets();
        internal.dispose();
        difficultyStage.dispose();
        if (currentStage != difficultyStage)
            currentStage.dispose();
    }

    public void update(float dt) {

    }

    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        Color c = new Color((1-alpha)*0.9f, (1-alpha)*0.9f, (1-alpha)*0.9f, 1);
        if (alpha > 0 && !transition) {
            alpha -= 0.02;
        }

        canvas.begin();
        if (difficulty == Difficulty.EASY) {
            canvas.draw(easyBackground, c, 0.0f, 0.0f, (float)canvas.getWidth(), (float)canvas.getHeight() );
        } else if (difficulty == Difficulty.MEDIUM) {
            canvas.draw(mediumBackground, c, 0.0f, 0.0f, (float)canvas.getWidth(), (float)canvas.getHeight() );
        } else if (difficulty == Difficulty.HARD) {
            canvas.draw(hardBackground, c, 0.0f, 0.0f, (float)canvas.getWidth(), (float)canvas.getHeight() );
        } else {
            canvas.draw(background, c, 0.0f, 0.0f, (float)canvas.getWidth(), (float)canvas.getHeight() );
        }
        canvas.end();
    }

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

            currentStage.act(delta);
            currentStage.draw();

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
                    alpha += 0.01;
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

        heightY = height;

        //stage.getViewport().update(width, height, true);
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

    public void setStage() {
        Gdx.input.setInputProcessor(currentStage);
    }

    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
    }

    public void chooseDifficulty() {
        difficulty = null;
        selectDifficulty();
        setStage();
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}
