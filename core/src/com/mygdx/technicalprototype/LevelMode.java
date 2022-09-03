package com.mygdx.technicalprototype;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.technicalprototype.assets.AssetDirectory;
import com.mygdx.technicalprototype.physics.obstacle.WheelObstacle;
import com.mygdx.technicalprototype.util.*;
import com.mygdx.technicalprototype.physics.obstacle.ObstacleSelector;

import java.util.ArrayList;
import java.util.Iterator;

import static com.mygdx.technicalprototype.AssetController.*;
import static com.mygdx.technicalprototype.MenuMode.*;


/**
 * Gameplay specific controller for the gameplay prototype.
 */

public class LevelMode implements Screen, ContactListener {
    private float alpha = 1;
    private ShapeRenderer sr = new ShapeRenderer(8);

    /** Number of velocity iterations for the constrain solvers */
    public static final int WORLD_VELOC = 6;
    /** Number of position iterations for the constrain solvers */
    public static final int WORLD_POSIT = 2;

    /** Width of the game world in Box2d units */
    protected static final float DEFAULT_WIDTH  = 32.0f;
    /** Height of the game world in Box2d units */
    protected static final float DEFAULT_HEIGHT = 18.0f;
    /** The default value of gravity (going down) */
    protected static final float DEFAULT_GRAVITY = -4.9f;

    /**
     * The rate in which to increase/decrease a planet's size
     */
    private static final float SIZE_RATE = 0.04f;
    private static final float SOLAR_FLARE_COOLDOWN = 3;
    private static final float SOLAR_FLARE_TELE_TIME = 0.6f;

    /** How many frames after winning/losing do we continue? */
    public static final int EXIT_COUNT = 500;

    /** Blast forces */
    private static final float PLANET_FORCE = 100f;
    private static final float REWARD_FORCE = 1000f;
    private static final float SHIP_FORCE = 3000f;
    private static final float REWARD_BLAST_RANGE = 50f;
    private static final float SHIP_BLAST_RANGE = 500f;
    private static final float INVINCIBILITY_LENGTH = 2;



    private int levelNum;

    /**
     * An array of resizable celestial objects
     */
    private ArrayList<Planet> resizables;

    /*
    * Frame Buffer for shaders
     */
    private FrameBuffer fbo;

    /** An array of dynamic obstacles */
    private ArrayList<DynamicObstacle> dynamicObs;

    /** An array of static obstacles */
    private ArrayList<StaticObstacle> staticObs;

    /** An array of moving particles */
    private ArrayList<MovParticle> movParticles;

    /** An array of static rewards */
    private ArrayList<StaticObstacle> rewards;

    /** An array of checkpoints */
    private ArrayList<Checkpoint> checkpoints;

    /** An array of enemies */
    private ArrayList<Enemy> es;

    /** Array of enemy zones */
    ArrayList<TriggerZone> enemyAreas;

    /**
     * Mouse selector to move the ragdoll
     */
    private ObstacleSelector selector;
    private Planet selected;

    private AIController ai;

    /** The Box2D world */
    protected World world;
    /** The boundary of the world */
    protected Rectangle bounds;
    /** The world scale */
    protected Vector2 scale;
    /** The amount of time for a physics engine step. */
    public float worldStep = 1/60.0f;

    /** Reference to the game canvas */
    protected GameCanvas canvas;
    /** All the objects in the world. */
    protected PooledList<GameObject> objects  = new PooledList<GameObject>();
    /** Queue for adding objects */
    protected PooledList<GameObject> addQueue = new PooledList<GameObject>();
    /** Listener that will update the player mode when we are done */
    protected ScreenListener listener;

    private Stage pauseScreen;

    private TextButton[] pauseButtons = new TextButton[5];
    private TextButton[] completeButtons = new TextButton[3];
    private TextButton[] failedButtons = new TextButton[3];

    private TextButton[] endButtons = new TextButton[2];


    /**
     * Respawn location, defaults to beginning
     */
    private Vector2 checkpoint;
    private Vector2 checkpointVel;

    private Vector2 startPoint;

    /**
     * Reference to the goalDoor (for collision detection)
     */
    private StaticObstacle goalDoor;
    /**
     * Reference to the entryDoor (no need for collision detection)
     */
    private StaticObstacle entryDoor;

    /**
     * An array of enemies
     */
    private ArrayList<Enemy> enemies = new ArrayList<>();;

    /**
     * The arraylist of points to define path
     */
    ArrayList<Vector2> forwardPath;
    ArrayList<Vector2> backwardPath;
    ArrayList<Vector2> forwardVels;
    ArrayList<Vector2> backwardVels;
    public static ArrayList<Vector2> debugPts = new ArrayList<>();
    float nextIndexFloat;
    int nextIndex;
    Path pathController;
    private PathShader pathShader;

    /**
     * Object for the rocket
     */
    private Rocket ship;


    /** Temp vectors for mem aloc */
    private Vector2 temp = new Vector2();
    private Vector2 temp1 = new Vector2();

    /** Temp Rectangle for mem aloc */
    private Rectangle tempRect  = new Rectangle();

    /**
     * The level minimap
     */

    private Minimap minimap;
    /**
     * The minimap pixel width
     */
    private static final int MINIMAP_WIDTH = 320;
    /**
     * The minimap pixel height
     */
    private static final int MINIMAP_HEIGHT = 160;
    /**
     * The minimap padding
     */
    private static final int MINIMAP_PADDING = 54;

    /**
     * Current health (0 to 1), 0 is dead, 1 is full health
     */
    private float health = 1;

    /** Various logic flags */
    private boolean hitCheckpoint = false;
    private boolean debug;
    private boolean active;
    private boolean inEnemyArea;
    private boolean toFailed = false;
    private boolean paused = false;
    private boolean settings = false;
    private boolean clearAllEnemy = false;
    private boolean toExit = false;
    private boolean toLevelSelect = false;
    private boolean toNext = false;
    private boolean transition = false;
    private boolean opening = true;
    private boolean isComplete;
    private boolean isFailed;
    private boolean isSelected = false;
    private boolean theEnd = false;
    private boolean toEnd = false;

    private int endScreenNum = 0;
    private int endText = 0;

    private int countdown;
    private int   pressState;
    private int levelNameCountdown = 300;
    private float sumDelta = 0;
    private float cycles = 0;

    /** The main menu button text style */
    private TextButton.TextButtonStyle buttonStyle;
    /** The text style when the mouse hovers over a UI button*/
    private TextButton.TextButtonStyle hoverStyle;

    private EscapeFromTroy game;
    private SharedUI ui;

    private Stage complete;
    private Stage failed;
    private Stage end;

    private int numStars = 0;

    /*
    * The controller for various shader effects
     */
    private EffectsController effects;

    private Affine2 shipCameraTrans;
    private Affine2 backgroundCameraTrans;
    private static final int CAM_PADDING = 0;
    private float prevXTrans;
    private float prevYTrans;

    private Color boundOpacity;

    /** Boolean to indicate whether to remove all enemies */
    private boolean removeAllEnemy=false;
    /** The individual enemy we would like to remove*/
    private Enemy globalEnemy;

    private float[] volumes = new float[3];
    private float soundsVol = 0.75f;
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
                if (textButton == endButtons[0]) {
                    if (endText == 0) {
                        endText = 1;
                    } else {
                        endText = 2;
                        endScreenNum = 1;
                    }
                } else if (textButton == endButtons[1]) {
                    endText = 2;
                    endScreenNum = 1;
                } else if (textButton == pauseButtons[0]) {
                    resume();
                } else if (textButton == pauseButtons[1]) {
                    hitCheckpoint = false;
                    reset();
                } else if (textButton == pauseButtons[2]) {
                    settings = true;
                    ui.setState(SharedUI.Overlay.SETTINGS);
                    Gdx.input.setInputProcessor(ui.getSettings());
                } else if (textButton == pauseButtons[3]) {
                    transition = true;
                    alpha = 0;
                    toLevelSelect = true;
                } else if (textButton == pauseButtons[4] || textButton == completeButtons[2] || textButton == failedButtons[2]) {
                    transition = true;
                    alpha = 0;
                    toExit = true;
                } else if (textButton == completeButtons[0]) {
                    transition = true;
                    alpha = 0;
                    if (levelNum < 21)
                        toNext = true;
                    else
                        toEnd = true;
                } else if (textButton == completeButtons[1] || textButton == failedButtons[1]) {
                    hitCheckpoint = false;
                    reset();
                } else if (textButton == failedButtons[0]) {
                    reset();
                }
            } else {
                pressState = 0;
            }
            super.touchUp(event, x, y, pointer ,button);
        }

        @Override
        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            textButton.setStyle(hoverStyle);
            super.enter(event, x, y, pointer, fromActor);
        }

        @Override
        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            textButton.setStyle(buttonStyle);
            super.exit(event, x, y, pointer, toActor);

        }
    }

    /**
     * Sets whether debug mode is active.
     *
     * If true, all objects will display their physics bodies.
     *
     * @param value whether debug mode is active.
     */
    public void setDebug(boolean value) {
        debug = value;
    }

    /**
     * Sets whether the level is completed.
     *
     * If true, the level will advance after a countdown
     *
     * @param value whether the level is completed.
     */
    public void setComplete(boolean value) {
        if (isFailed) {
            return;
        }
        if (value) {
            countdown = EXIT_COUNT/4;
        }
        isComplete = value;
    }

    /**
     * Sets whether the level is failed.
     *
     * If true, the level will reset after a countdown
     *
     * @param value whether the level is failed.
     */
    public void setFailure(boolean value) {
        if (isComplete) {
            return;
        }
        if (value) {
            countdown = EXIT_COUNT;
        }
        isFailed = value;
    }

    /**
     * Sets the canvas associated with this controller
     *
     * The canvas is shared across all controllers.  Setting this value will compute
     * the drawing scale from the canvas size.
     *
     * @param canvas the canvas associated with this controller
     */
    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
        this.scale.x = canvas.getWidth()/bounds.getWidth();
        this.scale.y = canvas.getHeight()/bounds.getHeight();
    }

    /**
     * Creates and initialize a new instance of the game
     * <p>
     * The game has default gravity and other settings
     */
    public LevelMode(int level, EscapeFromTroy g) {
        world = new World(new Vector2(0,DEFAULT_GRAVITY),false);
        ui = SharedUI.getInstance();
        this.bounds = new Rectangle(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT);
        this.scale = new Vector2(1,1);
        isComplete = false;
        isFailed = false;
        debug  = false;
        active = false;
        countdown = -1;

        setDebug(false);
        setComplete(false);
        setFailure(false);
        world.setContactListener(this);
        game = g;
        shipCameraTrans = (new Affine2()).idt();
        backgroundCameraTrans = (new Affine2()).idt();

        boundOpacity = new Color(Color.WHITE);

        checkpoint = Vector2.Zero;
        checkpointVel = new Vector2(0.5f, 0);
        toLoad = level;
        levelNum = level;
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);

        minimap = new Minimap(MINIMAP_WIDTH, MINIMAP_HEIGHT, MINIMAP_PADDING);

        effects = new EffectsController();

        buttonStyle = ui.getButtonStyle();
        hoverStyle = ui.getHoverStyle();


        pauseScreen = new Stage();
        complete = new Stage();
        failed = new Stage();

        Table pauseTable = new Table();
        pauseTable.setFillParent(true);
        pauseScreen.addActor(pauseTable);
        Table completeTable = new Table();
        completeTable.setFillParent(true);
        complete.addActor(completeTable);
        Table failedTable = new Table();
        failedTable.setFillParent(true);
        failed.addActor(failedTable);

        pauseButtons[0] = new TextButton("RESUME", buttonStyle);
        pauseButtons[0].setBounds(0, 0, pauseButtons[0].getWidth(), pauseButtons[0].getHeight());
        pauseButtons[0].addListener(new LevelMode.MenuListener(pauseButtons[0]));
        pauseButtons[1] = new TextButton("\nRESTART LEVEL", buttonStyle);
        pauseButtons[1].setBounds(0, 0, pauseButtons[1].getWidth(), pauseButtons[1].getHeight());
        pauseButtons[1].addListener(new LevelMode.MenuListener(pauseButtons[1]));
        pauseButtons[2] = new TextButton("\nSETTINGS", buttonStyle);
        pauseButtons[2].setBounds(0, 0, pauseButtons[2].getWidth(), pauseButtons[2].getHeight());
        pauseButtons[2].addListener(new LevelMode.MenuListener(pauseButtons[2]));
        pauseButtons[3] = new TextButton("\nLEVEL SELECT", buttonStyle);
        pauseButtons[3].setBounds(0, 0, pauseButtons[3].getWidth(), pauseButtons[3].getHeight());
        pauseButtons[3].addListener(new LevelMode.MenuListener(pauseButtons[3]));
        pauseButtons[4] = new TextButton("\nEXIT TO TITLE SCREEN", buttonStyle);
        pauseButtons[4].setBounds(0, 0, pauseButtons[4].getWidth(), pauseButtons[4].getHeight());
        pauseButtons[4].addListener(new LevelMode.MenuListener(pauseButtons[4]));

        completeButtons[0] = new TextButton("NEXT STAGE", buttonStyle);
        completeButtons[0].setBounds(0, 0, completeButtons[0].getWidth(), completeButtons[0].getHeight());
        completeButtons[0].addListener(new LevelMode.MenuListener(completeButtons[0]));
        completeButtons[1] = new TextButton("\nRESTART STAGE", buttonStyle);
        completeButtons[1].setBounds(0, 0, completeButtons[1].getWidth(), completeButtons[1].getHeight());
        completeButtons[1].addListener(new LevelMode.MenuListener(completeButtons[1]));
        completeButtons[2] = new TextButton("\nEXIT TO TITLE SCREEN", buttonStyle);
        completeButtons[2].setBounds(0, 0, completeButtons[2].getWidth(), completeButtons[2].getHeight());
        completeButtons[2].addListener(new LevelMode.MenuListener(completeButtons[2]));

        failedButtons[0] = new TextButton("RETRY FROM LAST CHECKPOINT", buttonStyle);
        failedButtons[0].setBounds(0, 0, failedButtons[0].getWidth(), failedButtons[0].getHeight());
        failedButtons[0].addListener(new LevelMode.MenuListener(failedButtons[0]));
        failedButtons[1] = new TextButton("\nRESTART STAGE", buttonStyle);
        failedButtons[1].setBounds(0, 0, failedButtons[1].getWidth(), failedButtons[1].getHeight());
        failedButtons[1].addListener(new LevelMode.MenuListener(failedButtons[1]));
        failedButtons[2] = new TextButton("\nEXIT TO TITLE SCREEN", buttonStyle);
        failedButtons[2].setBounds(0, 0, failedButtons[2].getWidth(), failedButtons[2].getHeight());
        failedButtons[2].addListener(new LevelMode.MenuListener(failedButtons[2]));

        pauseTable.center().padTop(Gdx.graphics.getHeight()/4);
        pauseTable.add(pauseButtons[0]);
        pauseTable.row();
        pauseTable.add(pauseButtons[1]);
        pauseTable.row();
        pauseTable.add(pauseButtons[2]);
        pauseTable.row();
        pauseTable.add(pauseButtons[3]);
        pauseTable.row();
        pauseTable.add(pauseButtons[4]);

        completeTable.center().padTop(Gdx.graphics.getHeight()/6);
        completeTable.add(completeButtons[0]);
        completeTable.row();
        completeTable.add(completeButtons[1]);
        completeTable.row();
        completeTable.add(completeButtons[2]);

        failedTable.center().padTop(Gdx.graphics.getHeight()/6);
        failedTable.add(failedButtons[0]);
        failedTable.row();
        failedTable.add(failedButtons[1]);
        failedTable.row();
        failedTable.add(failedButtons[2]);

        if (levelNum >= 21) {
            end = new Stage();

            Table endTable = new Table();
            endTable.setFillParent(true);
            end.addActor(endTable);

            // define end buttons
            endButtons[0] = new TextButton("CONTINUE", ui.getButtonStyle());
            endButtons[0].setBounds(0, 0, endButtons[0].getWidth(), endButtons[0].getHeight());
            endButtons[0].addListener(new LevelMode.MenuListener(endButtons[0]));
            endButtons[1] = new TextButton("SKIP", ui.getButtonStyle());
            endButtons[1].setBounds(0, 0, endButtons[1].getWidth(), endButtons[1].getHeight());
            endButtons[1].addListener(new LevelMode.MenuListener(endButtons[1]));

            endTable.bottom().padTop(Gdx.graphics.getHeight()).padLeft(Gdx.graphics.getHeight()/20f).padRight(Gdx.graphics.getHeight()/20f).padBottom(Gdx.graphics.getHeight()/25f);
            endTable.add(endButtons[0]).left().expand();
            endTable.add(endButtons[1]).right();
        }
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
        AssetController.updateVolume(n, volume);
    }

    public float[] getVolumes() {
        return volumes;
    }

    public float getBrightness() {
        return brightness;
    }

    /**
     * Resets the status of the game so that we can play again.
     * <p>
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        Vector2 gravity = new Vector2(world.getGravity());

        for (GameObject obj : objects) {
            obj.deactivatePhysics(world);
        }
        //refill health bar
        health = 1;
        //stop remaining sound effects
        decelerate.stop();
        accelerate.stop();
        objects.clear();
        level.dispose();
        addQueue.clear();
        world.dispose();

        world = new World(gravity, false);
        enemies = new ArrayList<>();
        ui = SharedUI.getInstance();

        inEnemyArea = false;
        settings = false;
        paused = false;
        toExit = false;
        toLevelSelect = false;
        toNext = false;
        isSelected = false;

        worldStep = 1/60.0f;

        setDebug(false);
        setComplete(false);
        setFailure(false);


        world.setContactListener(this);
        populateLevel();
//        if (hitCheckpoint)
//            ship.setPosition(checkpoint);
    }

    /**
     * Returns whether to process the update loop
     *
     * At the start of the update loop, we check if it is time
     * to switch to a new game mode.  If not, the update proceeds
     * normally.
     *
     * @param dt	Number of seconds since last animation frame
     *
     * @return whether to process the update loop
     */
    public boolean preUpdate(float dt) {
        if (settings && ui.getPressState() == 2) {
            TextButton b = ui.getPressedButton();
            if (b == ui.getControlSettingsButtons()[7] || b == ui.getAudioDisplaySettingsButtons()[4] || b == ui.getCreditsButtons()[1]) {
                ui.setState(SharedUI.Overlay.SETTINGS);
                Gdx.input.setInputProcessor(ui.getSettings());
                pressState = 0;
            }

            switch (ui.getOverlay()) {
                case SETTINGS:
                    if (b == ui.getSettingsButtons()[0]) {
                        ui.setState(SharedUI.Overlay.CONTROLS);
                        Gdx.input.setInputProcessor(new InputMultiplexer(ui.getControlSettings(), ui.getButtonInputProcessor()));
                        pressState = 0;
                    } else if (b == ui.getSettingsButtons()[1]) {
                        ui.setState(SharedUI.Overlay.AUDIO_DISPLAY);
                        Gdx.input.setInputProcessor(ui.getAudioDisplaySettings());
                        pressState = 0;
                    } else if (b == ui.getSettingsButtons()[2]) {
                        ui.setState(SharedUI.Overlay.CREDITS);
                        Gdx.input.setInputProcessor(ui.getCredits());
                        pressState = 0;
                    } else if (b == ui.getSettingsButtons()[3]) {
                        settings = false;
                        Gdx.input.setInputProcessor(pauseScreen);
                        ui.reset();
                        pressState = 0;
                    } else if (b == ui.getSettingsButtons()[4]) {
                        pressState = 2;
                        ui.reset();
                        toExit = true;
                    }
                    break;
                case CONTROLS:
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
                case CREDITS:
                    break;
            }
        }

        InputController input = InputController.getInstance();
        input.readInput(bounds, scale);
        if (listener == null) {
            return true;
        }

        // Toggle debug
        if (input.didDebug()) {
            debug = !debug;
        }

        // Handle resets
        if (input.didReset()) {
            reset();
        }

        // Now it is time to maybe switch screens.
        if (input.didExit()) {
            if (!paused) {
                pause();
            } else {
                resume();
            }
            return false;
        } else if (countdown > 0) {
            countdown--;
        } else if (countdown == 0) {
            if (isFailed) {
                toFailed = true;
            }
        }
        return true;
    }

    /**
     * The core gameplay loop of this world.
     * <p>
     * This method contains the specific update code for this mini-game. It does
     * not handle collisions, as those are managed by the parent class WorldController.
     * This method is called after input is read, but before collisions are resolved.
     * The very last thing that it should do is apply forces to the appropriate objects.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
//        if (cycles < 60) {
//            cycles++;
//        } else {
//            sumDelta += dt;
//            cycles++;
//        }

        if (isFailed) {
            return;
        }
        if (paused) {
            return;
        }
        effects.update();



        // Detect input for planet resizing.
        // Tertiary is left click (grow), quaternary is right click (shrink).
        InputController input = InputController.getInstance();

        if (health < 0.33) {
            pause();
            Gdx.input.setInputProcessor(failed);
            for (int i = 0; i < dynamicObs.size(); i++) {
                DynamicObstacle d = dynamicObs.get(i);
                d.setStopTime(true);
            }
            setFailure(true);
        }

        temp1.set(canvas.getWidth() / 2, canvas.getHeight() / 2);
        pathController.addPath(temp1, ship.getVelocity(), resizables);


        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && numStars > 0){
            doStarBlast();
        }

        /*
         * Trigger swarm via event example
         */
//        if(Gdx.input.isKeyJustPressed(Input.Keys.K)){
//            ArrayList<Enemy> el = ai.triggerSwarm(temp1.set(0, Gdx.graphics.getHeight()), 10, 1);
//            for (Enemy e:
//                 el) {
//                addObject(e);
//            }
//
//        }


        //if (ai.hasEnemies()) {
            ArrayList<Enemy> el = ai.update();
            if(el != null) {
                for (Enemy e :
                        el) {
                    addObject(e);
                }
            }
        //}

        ship.getSprinkler().setAngle(-ship.getVelocity().angleRad());
        ship.getSprinkler().update(dt, ship.getPos());
        Vector2 pos = Helper.boxCoordsToWorldCoords(scale, ship.getPosition());
        for(int i=0; i< movParticles.size()-1; i++){
//            movParticles.get(i).setAngle(-ship.getVelocity().angleRad());
            movParticles.get(i).setLocation(canvas, pos);
            movParticles.get(i).update(dt, new Vector2(640+50*(i+1),360+50*(i+1)));

        }
//        movParticles.get(movParticles.size()-1).setAngle(-ship.getVelocity().angleRad());
        movParticles.get(movParticles.size()-1).setLocation(canvas, pos);
        movParticles.get(movParticles.size()-1).update(dt, new Vector2(640+180,360+250));



        forwardPath = pathController.getAllForwardPoints();
        backwardPath = pathController.getAllBackwardPoints();
        forwardVels = pathController.getAllForwardVels();
        backwardVels = pathController.getAllBackwardVels();
        nextIndexFloat = ship.getLinearVelocity();

        ArrayList dis = new ArrayList();
        for(int i=0; i< resizables.size(); i++){
            dis.add(Math.abs(ship.getPosition().x-(resizables.get(i)).getX()) + Math.abs(ship.getPosition().y-(resizables.get(i)).getY()));
        }
        int index = 0;
        if(dis.size() != 0 && !isSelected) {
            float min = (float) dis.get(index);

            for (int i = 0; i < dis.size(); i++) {
                if ((float) dis.get(i) <= min) {
                    min = (float) dis.get(i);
                    index = i;
                }
            }
            selected = resizables.get(index);
        }

        // Click on non-planet space
        if (((input.didTertiary() || input.didQuaternary()) && !selector.isSelected()) && !isComplete) {
            selector.select(input.getCrossHair().x, input.getCrossHair().y);
            isSelected = false;
            click.play(soundsVol);
        }
        else if (!input.didTertiary() && !input.didQuaternary() && selector.isSelected() && !isComplete) {
            selector.deselect();
        }
        // Click on planet to resize
        else if (input.didTertiary() && selector.isSelected() && !isComplete) {
            if (selector.getObstacle() instanceof Planet) {
                isSelected = true;
                Planet resizable = (Planet) selector.getObstacle();
                float radAfterChange = (resizable.getRadius() + SIZE_RATE * 500) / scale.x;
                if (!resizable.isAnimating() && planetResizeElapsedTime == -1 && resizable.changeSize(1)) {
                    increaseSize.play(soundsVol);
                    planetResizeElapsedTime = 0;
                    resizable.setGrowing(true);
                    resizable.setRadiusAfterChange(radAfterChange);
                    if (!ship.getVelocity().equals(forwardVels.get(1))) {
                        pathController.calculateLastTwoPoints(resizable);
                        Vector2 lastPt = pathController.lastOrbitPt;
                        Vector2 lastVel = pathController.lastOrbitVel;
                        Vector2 secondLastPt = pathController.secondLastOrbitPt;
                        Vector2 secondLastVel = pathController.secondLastOrbitVel;

                        pathController.calculatePath(lastPt, lastVel, secondLastPt, secondLastVel, resizables);
                        pathController.moveForward(pathController.index);
                        ship.setPos(new Vector2(pathController.forwardPts.get(0)));
                        ship.setVelocity(new Vector2(pathController.forwardVels.get(0)));
                    } else {
                        pathController.calculatePath(ship.getPos(), ship.getVelocity(), ship.getPos(), ship.getVelocity(), resizables);
                    }
                }
                click.stop();
            } else {
                isSelected = false;
            }
        }
        else if (input.didQuaternary() && selector.isSelected() && !isComplete) {
            if (selector.getObstacle() instanceof Planet) {
                isSelected = true;
                Planet resizable = (Planet) selector.getObstacle();
                float radAfterChange = (resizable.getRadius() - SIZE_RATE * 500) / scale.x;
                if (!resizable.isAnimating() && planetResizeElapsedTime == -1 && resizable.changeSize(-1)) {
                    increaseSize.play(soundsVol);
                    planetResizeElapsedTime = 0;
                    resizable.setShrinking(true);
                    resizable.setRadiusAfterChange(radAfterChange);
                    if (!ship.getVelocity().equals(forwardVels.get(1))) {
                        pathController.calculateLastTwoPointsBackwards(resizable);
                        Vector2 lastPt = pathController.lastOrbitPt;
                        Vector2 lastVel = pathController.lastOrbitVel;
                        Vector2 secondLastPt = pathController.secondLastOrbitPt;
                        Vector2 secondLastVel = pathController.secondLastOrbitVel;

                        pathController.calculatePath(lastPt, lastVel, secondLastPt, secondLastVel, resizables);
                        pathController.moveForward(pathController.index);
                        ship.setPos(new Vector2(pathController.forwardPts.get(0)));
                        ship.setVelocity(new Vector2(pathController.forwardVels.get(0)));
                    } else {
                        pathController.calculatePath(ship.getPos(), ship.getVelocity(), ship.getPos(), ship.getVelocity(), resizables);
                    }
                }
                click.stop();
                //intentionally use increaseSize sound for decreaseSize
            } else {
                isSelected = false;
            }
        } else if (input.didNearA() && !isComplete && resizables.size() > 0) {
            Planet resizable = selected;
            float radAfterChange = (resizable.getRadius() + SIZE_RATE * 500) / scale.x;
            if (!resizable.isAnimating() && planetResizeElapsedTime == -1 && resizable.changeSize(1)) {
                increaseSize.play(soundsVol);
                planetResizeElapsedTime = 0;
                resizable.setGrowing(true);
                resizable.setRadiusAfterChange(radAfterChange);
                if (!ship.getVelocity().equals(forwardVels.get(1))) {
                    pathController.calculateLastTwoPoints(resizable);
                    Vector2 lastPt = pathController.lastOrbitPt;
                    Vector2 lastVel = pathController.lastOrbitVel;
                    Vector2 secondLastPt = pathController.secondLastOrbitPt;
                    Vector2 secondLastVel = pathController.secondLastOrbitVel;

                    pathController.calculatePath(lastPt, lastVel, secondLastPt, secondLastVel, resizables);
                    pathController.moveForward(pathController.index);
                    ship.setPos(new Vector2(pathController.forwardPts.get(0)));
                    ship.setVelocity(new Vector2(pathController.forwardVels.get(0)));
                } else {
                    pathController.calculatePath(ship.getPos(), ship.getVelocity(), ship.getPos(), ship.getVelocity(), resizables);
                }
            }
        } else if (input.didNearF() && !isComplete && resizables.size() > 0) {
            Planet resizable = selected;
            float radAfterChange = (resizable.getRadius() - SIZE_RATE * 500) / scale.x;
            if (!resizable.isAnimating() && planetResizeElapsedTime == -1 && resizable.changeSize(-1)) {
                increaseSize.play(soundsVol);
                planetResizeElapsedTime = 0;
                resizable.setShrinking(true);
                resizable.setRadiusAfterChange(radAfterChange);
                if (!ship.getVelocity().equals(forwardVels.get(1))) {
                    pathController.calculateLastTwoPointsBackwards(resizable);
                    Vector2 lastPt = pathController.lastOrbitPt;
                    Vector2 lastVel = pathController.lastOrbitVel;
                    Vector2 secondLastPt = pathController.secondLastOrbitPt;
                    Vector2 secondLastVel = pathController.secondLastOrbitVel;

                    pathController.calculatePath(lastPt, lastVel, secondLastPt, secondLastVel, resizables);
                    pathController.moveForward(pathController.index);
                    ship.setPos(new Vector2(pathController.forwardPts.get(0)));
                    ship.setVelocity(new Vector2(pathController.forwardVels.get(0)));
                } else {
                    pathController.calculatePath(ship.getPos(), ship.getVelocity(), ship.getPos(), ship.getVelocity(), resizables);
                }
            }
            //intentionally use increaseSize sound for decreaseSize
        }

        // handling player input
        if (input.didPrimary() && nextIndexFloat < 20f && !isComplete) {
            decelerate.play(soundsVol);

            ship.getSprinkler().setMoving(true);


            if (nextIndexFloat >= 0) {
                ship.addLinearVelocity(0.5f);
            } else {
                ship.addLinearVelocity(0.8f);
            }
        } else if (input.didSecondary() && -nextIndexFloat < 20f && !isComplete) {
            decelerate.play(soundsVol);

            ship.getSprinkler().setMoving(false);

            if (nextIndexFloat <= 0) {
                ship.addLinearVelocity(-0.5f);
            } else {
                ship.addLinearVelocity(-0.8f);
            }
        } else if (!isComplete) {
            ship.getSprinkler().setMoving(false);
            if (nextIndex > 0) {
                ship.addLinearVelocity(-0.1f);
            } else if (nextIndex < 0) {
                ship.addLinearVelocity(0.1f);
            }
        }

        // movement controls
        nextIndex = (int) nextIndexFloat;
        if (nextIndex > 0) {
            ship.setPos(forwardPath.get(nextIndex));
            ship.setVelocity(forwardVels.get(nextIndex));
            pathController.moveForward(nextIndex);
        } else if (nextIndex < 0) {
            ship.setPos(backwardPath.get(-nextIndex));
            ship.setVelocity(new Vector2(backwardVels.get(-nextIndex)).scl(-1));
            pathController.moveForward(nextIndex);
        }

        if (outsideBounds()) {
            pathController.moveForward(-nextIndex);
            restrictShipToBounds();
        }

        if (ship.getLinearVelocity() != 0)
            updateLocations();

        for (int i = 0; i < dynamicObs.size(); i++) {
            DynamicObstacle d = dynamicObs.get(i);
            d.move(dt);
            d.setLocation(canvas, ship.getPos());
        }

        // update minimap
        minimap.loadPositions(objects);
        minimap.updateMap(ship, scale);

        ai.updateGravity(resizables);


        /*
         * Check if player is in enemy area trigger
         */

        inEnemyArea = false;
        for(TriggerZone z : enemyAreas){

                tempRect.set(z.bounds);
                if (tempRect.contains(ship.getPos())) {
                    inEnemyArea = true;
                  
                    if(!z.triggered) {
                      if(easyLoop.isPlaying()){
                        easyLoop.stop();
                    }
                    if(easyIntro.isPlaying()){
                        easyIntro.stop();
                    }
                    enemyMusic.play();
                      
                    enemyMusic.setLooping(true);
                        z.triggered = true;
                        alertSound.play();
                        for (Vector2 dir :
                                z.directions) {
                            tempRect.getCenter(temp1);
                            temp1.sub(dir.cpy().scl(scale));
                            temp1.set((Gdx.graphics.getWidth() / 2) - temp1.x, (Gdx.graphics.getHeight() / 2) - temp1.y);
                            temp1 = AIController.screenEdge(temp1, 25);
                            if(z.directions.size() == 1) {
                                effects.triggerAlert(temp1, Color.FIREBRICK);
                            }else{
                                effects.triggerAlert(new Vector2(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2), Color.FIREBRICK);
                            }
                            ai.triggerSwarm(temp1, z.amount, z.distance, z.speed, z.respawn);
                        }

                    }
                }

        }
    }
    /**
     * Processes physics
     *
     * Once the update phase is over, but before we draw, we are ready to handle
     * physics.  The primary method is the step() method in world.  This implementation
     * works for all applications and should not need to be overwritten.
     *
     * @param dt	Number of seconds since last animation frame
     */
    public void postUpdate(float dt) {
        // Add any objects created by actions
        while (!addQueue.isEmpty()) {
            addObject(addQueue.poll());
        }

        // Turn the physics engine crank.
        world.step(worldStep,WORLD_VELOC,WORLD_POSIT);

        // Garbage collect the deleted objects.
        // Note how we use the linked list nodes to delete O(1) in place.
        // This is O(n) without copying.
        Iterator<PooledList<GameObject>.Entry> iterator = objects.entryIterator();
        while (iterator.hasNext()) {
            PooledList<GameObject>.Entry entry = iterator.next();
            GameObject obj = entry.getValue();
            if (obj.isRemoved()) {
                obj.deactivatePhysics(world);
                entry.remove();
            } else {
                // Note that update is called last!
                obj.update(dt);
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
        // IGNORE FOR NOW
    }

    @Override
    public void render(float delta) {
        if (active) {
            if (preUpdate(delta)) {
                update(delta); // This is the one that must be defined.
                postUpdate(delta);
            }
            draw(delta);
        }

        if (opening) {
            Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
            sr.setColor(0, 0, 0, alpha);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            sr.end();
            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

            if (alpha > 0) {
                alpha -= 0.01;
            } else {
                opening = false;
            }
        } else if (transition) {
            Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
            sr.setColor(0, 0, 0, alpha);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            sr.end();
            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

            if (alpha < 1) {
                alpha += 0.02;
            } else {
                if (toExit) {
                    listener.exitScreen(this, 0);
                } else if (toNext) {
                    listener.exitScreen(this, levelNum);
                } else if (toLevelSelect) {
                    listener.exitScreen(this, -2);
                } else if (toEnd) {
                    theEnd = true;
                    transition = false;
                    Gdx.input.setInputProcessor(end);
                }
            }
        } else if (theEnd && endScreenNum == 0) {
            end.act(delta);
            end.draw();
        }

        // brightness
        if (brightness < 1) {
            Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
            game.shapeRenderer.setColor(0, 0, 0, 1-brightness);
            game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            game.shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            game.shapeRenderer.end();
            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
        }
    }

    public void updateLocations() {
        for (GameObject obj : objects) {
            if (obj instanceof Planet) {
                ((Planet) obj).setLocation(canvas, ship.getPos());
            } else if (obj instanceof Checkpoint) {
                ((Checkpoint) obj).setLocation(canvas, ship.getPos());
            } else if (obj instanceof StaticObstacle && !obj.getName().equals("solarflare")) {
                ((StaticObstacle) obj).setLocation(canvas, ship.getPos());
            } else if (obj instanceof Enemy) {
                ((Enemy) obj).setLocation(ship.getPos());
            }
        }
    }

    /**
     * Dispose of all (non-static) resources allocated to this mode.
     */
    public void dispose() {
        for (GameObject obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        addQueue.clear();
        world.dispose();
        objects = null;
        addQueue = null;
        bounds = null;
        scale = null;
        world = null;
        canvas = null;
    }

    /**
     * Restricts the ship's position to be within the level bounds.
     */
    private void restrictShipToBounds() {
        Vector2 pos = ship.getPos();
        if (pos.x + ship.getTexture().getRegionWidth()/2f > level.getMapPixelWidth()) {
            pos.x = level.getMapPixelWidth() - ship.getTexture().getRegionWidth()/2f;
        } else if (pos.x - ship.getTexture().getRegionWidth()/2f < 0) {
            pos.x = ship.getTexture().getRegionWidth()/2f;
        }
        if (pos.y + ship.getTexture().getRegionHeight()/2f > level.getMapPixelHeight()) {
            pos.y = level.getMapPixelHeight() - ship.getTexture().getRegionHeight()/2f;
        } else if (pos.y - ship.getTexture().getRegionHeight()/2f < 0) {
            pos.y = ship.getTexture().getRegionHeight()/2f;
        }
        if (!ship.getPos().equals(pos)) {
            ship.setLinearVelocity(0);
        }
        ship.setPos(pos);
    }

    /**
     * Whether the ship is outside the level boundary.
     * @return true if the ship is outside the level bounds.
     */
    private boolean outsideBounds() {
        Vector2 pos = ship.getPos();
        return pos.x + ship.getTexture().getRegionWidth()/2f > level.getMapPixelWidth()
                || pos.x - ship.getTexture().getRegionWidth()/2f < 0
                || pos.y + ship.getTexture().getRegionHeight()/2f > level.getMapPixelHeight()
                || pos.y - ship.getTexture().getRegionHeight()/2f < 0;
    }

    /**
     * Modifies the camera transform matrices when the rocket is near the level edge.
     */
    private void moveCamera() {
        Vector2 pos = ship.getPos();
        boolean xEdgeZero = pos.x - CAM_PADDING - canvas.getWidth()/2f < 0;
        boolean xEdgeMax = pos.x + CAM_PADDING + canvas.getWidth()/2f > level.getMapPixelWidth();
        boolean yEdgeZero = pos.y - CAM_PADDING - canvas.getHeight()/2f  < 0;
        boolean yEdgeMax = pos.y + CAM_PADDING + canvas.getHeight()/2f  > level.getMapPixelHeight();
        if (xEdgeZero || xEdgeMax || yEdgeZero || yEdgeMax) {
            float xTrans, yTrans;
            if (xEdgeZero) {
                xTrans = canvas.getWidth()/2f - pos.x - CAM_PADDING;
            } else if (xEdgeMax) {
                xTrans = level.getMapPixelWidth() - pos.x - canvas.getWidth()/2f - CAM_PADDING;
            }
            else {
                xTrans = prevXTrans;
            }
            if (yEdgeZero) {
                yTrans = canvas.getHeight()/2f - pos.y - CAM_PADDING;
            } else if (yEdgeMax) {
                yTrans = level.getMapPixelHeight() - pos.y - canvas.getHeight()/2f - CAM_PADDING;
            }
            else {
                yTrans = prevYTrans;
            }
            shipCameraTrans.translate(
                    prevXTrans - xTrans, prevYTrans - yTrans
            );
            float bgFlipX = xEdgeMax ? -9/16f : 1;
            float bgFlipY = yEdgeMax ? -9/16f : 1;
            backgroundCameraTrans.translate(
                    (prevXTrans - xTrans) * bgFlipX, (prevYTrans - yTrans) * bgFlipY
            );
            prevXTrans = xTrans;
            prevYTrans = yTrans;
        }
        else {
            shipCameraTrans.idt();
            backgroundCameraTrans.idt();
        }
    }

    private void determineBoundOpacity() {
        Vector2 pos = ship.getPos();
        boolean xEdgeZero = pos.x - canvas.getWidth()/2f < 0;
        boolean xEdgeMax = pos.x + canvas.getWidth()/2f > level.getMapPixelWidth();
        boolean yEdgeZero = pos.y - canvas.getHeight()/2f  < 0;
        boolean yEdgeMax = pos.y + canvas.getHeight()/2f  > level.getMapPixelHeight();
        boundOpacity.a = 0;
        boundOpacity.a = Math.max(boundOpacity.a, xEdgeZero ? 1 - ((pos.x - ship.getTexture().getRegionWidth()/2f) / (canvas.getWidth()/2f)) : 0);
        boundOpacity.a = Math.max(boundOpacity.a, xEdgeMax ? 1 - ((level.getMapPixelWidth() - pos.x - ship.getTexture().getRegionWidth()/2f) / (canvas.getWidth()/2f)) : 0);
        boundOpacity.a = Math.max(boundOpacity.a, yEdgeZero ? 1 - ((pos.y - ship.getTexture().getRegionHeight()/2f )/ (canvas.getHeight()/2f)) : 0);
        boundOpacity.a = Math.max(boundOpacity.a, yEdgeMax ? 1 - ((level.getMapPixelHeight() - pos.y - ship.getTexture().getRegionHeight()/2f ) / (canvas.getHeight()/2f)) : 0);
    }

    public void gatherAssets(AssetDirectory directory) {
        AssetController.gatherAssets(directory, scale);
        goalTile  = new TextureRegion(directory.getEntry( "shared:goal", Texture.class ));

        minimap.gatherAssets(directory);
        // set failure if health decreases to 0
        if(health<0.3){
            Gdx.input.setInputProcessor(failed);
            setFailure(true);
        }
    }

    /**
     * Pushes back the enemies that are in range of `obj`. The further the enemy
     * is, the smaller the magnitude of the force. All parameters units are in
     * Box2d units.
     *
     * @param center the center position of the blast
     * @param maxForceMag the maximum force blast magnitude
     * @param range the max range of the blast
     */
    private void blastBackEnemies(Vector2 center, float maxForceMag, float range, boolean shockwave) {

        for (Enemy e : enemies) {
            float dist = e.getPosition().dst(center);
            if (dist <= range) {
                // squared distance fall-off ratio
                e.setState(Enemy.State.STUN, ship.getPos());
                Vector2 force = (new Vector2(e.getPosition().sub(center)))
                        .setLength((float) Math.pow(1 - (dist / range), 1) * maxForceMag);
//                e.getBody().applyLinearImpulse(force, e.getPosition(), true);
//                e.setDir(force);
                e.setForce(force);
            }
        }
        if(shockwave) {
            if(shipExplodeSound.isPlaying()){
                shipExplodeSound.stop();
            }
            shockwaveSound.play();
            effects.triggerShock(center.cpy().scl(scale));
        }
    }

     /** Immediately adds the object to the physics world
     *
     * param obj The object to add
     */
    protected void addObject(GameObject obj) {
        objects.add(obj);
        obj.activatePhysics(world);
    }


    /**
     * Lays out the game geography.
     */
    private void populateLevel() {


        numStars = 0;

        level.setScale(scale);
        level.loadLevel(checkpoint);
        if(!hitCheckpoint) {
            if (startPoint == null) {
                startPoint = new Vector2(level.getEntry().getPos().x * scale.x, level.getEntry().getPos().y * scale.y);
            }
            checkpoint = new Vector2(startPoint);
        }

        // add entry in game geography
        JsonValue entry = constants.get("entry");
        Vector2 entryPos = new Vector2(entry.get("pos").getFloat(0), entry.get("pos").getFloat(1));
        float radius = entry.getFloat("radius");

        //entryDoor = new StaticObstacle(entryPos,radius,new Vector2(0,0), true);
        entryDoor = level.getEntry();
        entryDoor.setRadius(radius);
        entryDoor.setBodyType(BodyDef.BodyType.StaticBody);
        entryDoor.setDensity(entry.getFloat("density1", 0));
        entryDoor.setFriction(entry.getFloat("friction1", 0));
        entryDoor.setRestitution(entry.getFloat("restitution1", 0));
        entryDoor.setSensor(true);
        entryDoor.setDrawScale(scale);
        entryDoor.setName("entry");
        addObject(entryDoor);

        // add goal in game geography
        JsonValue goal = constants.get("goal");
        Vector2 goalPos = new Vector2(goal.get("pos").getFloat(0), goal.get("pos").getFloat(1) + 10);
        radius = goal.getFloat("radius");

//        goalDoor = new StaticObstacle(goalPos,radius,new Vector2(0,0), true);
        goalDoor = level.getGoal();
        goalDoor.setRadius(level.getGoal().getRadius());
        goalDoor.setLocation(canvas, new Vector2(35, 55));
        goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
        goalDoor.setDensity(goal.getFloat("density", 0));
        goalDoor.setFriction(goal.getFloat("friction", 0));
        goalDoor.setRestitution(goal.getFloat("restitution", 0));
        goalDoor.setSensor(true);
        goalDoor.setDrawScale(scale);
        goalDoor.setName("goal");
        addObject(goalDoor);

        // hardcoded planets for now
        resizables = new ArrayList<>();

        for (int i = 0; i < level.getResizables().size(); i++) {
            resizables.add(level.getResizables().get(i));
        }

        //movParticles
        float ox = Gdx.graphics.getWidth() / 2 / scale.x;
        float oy = Gdx.graphics.getHeight() / 2 / scale.y;
        movParticles = new ArrayList<>();
        MovParticle movParticle1 = new MovParticle(new Vector2(1200, 550), constants.get("movparticles"),ox,oy);
        MovParticle movParticle2 = new MovParticle(new Vector2(1000, 450),constants.get("movparticles"),ox,oy);
        MovParticle movParticle3 = new MovParticle(new Vector2(800, 350),constants.get("movparticles"),ox,oy);
        MovParticle movParticle4 = new MovParticle(new Vector2(600, 250),constants.get("movparticles"),ox,oy);
        movParticles.add(movParticle1);
        movParticles.add(movParticle2);
        movParticles.add(movParticle3);
        movParticles.add(movParticle4);
        for(int i = 0; i < movParticles.size(); i++){
            movParticles.get(i).setDrawScale(scale);
            movParticles.get(i).setTexture(particle);
            movParticles.get(i).setDensity(constants.get("defaults").getFloat("density", 0.0f));
            movParticles.get(i).setFriction(constants.get("defaults").getFloat("friction", 0.0f));
            movParticles.get(i).setRestitution(constants.get("defaults").getFloat("restitution", 0.0f));
            addObject(movParticles.get(i));
        }
        MovParticle movParticle5 = new MovParticle(new Vector2(400, 500),constants.get("movparticles"),ox,oy);
        movParticle5.setDrawScale(scale);
        movParticle5.setTexture(particle);
        movParticles.add(movParticle5);


        JsonValue defaults = constants.get("defaults");

        dynamicObs = new ArrayList<>();
        dynamicObs.addAll(level.getDynamicObstacles());

        for (int i = 0; i < dynamicObs.size(); i++) {
            DynamicObstacle d = dynamicObs.get(i);
            d.setSensor(true);
//            d.setBodyType(BodyDef.BodyType.KinematicBody);
            d.setDensity(defaults.getFloat("density", 0.0f));
            d.setFriction(defaults.getFloat("friction", 0.0f));
            d.setRestitution(defaults.getFloat("restitution", 0.0f));
            d.setDrawScale(scale);
            d.setTexture(cometTextures[0]);
            d.setName("dynamic" + i);
            addObject(d);
        }

        for (int i = 0; i < resizables.size(); i++) {
            Planet p = resizables.get(i);
            p.setBodyType(BodyDef.BodyType.StaticBody);
            p.setDensity(defaults.getFloat("density", 0.0f));
            p.setFriction(defaults.getFloat("friction", 0.0f));
            p.setRestitution(defaults.getFloat("restitution", 0.0f));
            p.setDrawScale(scale);
            p.setSensor(true);

            switch (p.getColor()) {
                case BLUE:
                    p.setTexture(bluePlanetTexture);
                    break;
                case GREEN:
                    p.setTexture(greenPlanetTexture);
                    break;
                case PINK:
                    p.setTexture(pinkPlanetTexture);
                    break;
                case PURPLE:
                    p.setTexture(purplePlanetTexture);
                    break;
                case SUN:
                    p.setTexture(sunPlanetTexture);
                    WheelObstacle solarFlare = p.getSolarFlare();
                    solarFlare.setBodyType(BodyDef.BodyType.DynamicBody);
                    solarFlare.setGravityScale(0);
                    solarFlare.setDensity(entry.getFloat("density1", 0));
                    solarFlare.setFriction(entry.getFloat("friction1", 0));
                    solarFlare.setRestitution(entry.getFloat("restitution1", 0));
                    solarFlare.setSensor(true);
                    solarFlare.setDrawScale(scale);
                    solarFlare.setName("solarflare");
                    addObject(solarFlare);
                    break;
                case YELLOW:
                    p.setTexture(yellowPlanetTexture);
                    break;
            }
            p.setName("planet" + i);
            addObject(p);
        }

        ship = new Rocket(checkpoint, 0.1f, ship1Textures[0], ship2Textures[0], ship3Textures[0], constants, scale, health);

        ship.setBodyType(BodyDef.BodyType.DynamicBody);
        ship.setSensor(true);
        ship.setGravityScale(0);
        ship.setDensity(defaults.getFloat("density", 0.0f));
        ship.setFriction(defaults.getFloat("friction", 0.0f));
        ship.setRestitution(defaults.getFloat("restitution", 0.0f));
        ship.setDrawScale(scale);

        ship.setTexture(ship3Textures[0]);
        ship.setVelocity(checkpointVel);
        ship.setName("ship");
        ship.getSprinkler().setTexture(particle);
        addObject(ship);

        staticObs = new ArrayList<>();
//        staticObs.add(new StaticObstacle(new Vector2(Helper.worldCoordsToBoxCoords(scale, ship.getPos()).x-3,Helper.worldCoordsToBoxCoords(scale, ship.getPos()).y+3), 1f, ship.getPos(), false));
//        staticObs.add(new StaticObstacle(new Vector2(Helper.worldCoordsToBoxCoords(scale, ship.getPos()).x+10,Helper.worldCoordsToBoxCoords(scale, ship.getPos()).y-7), 1f, ship.getPos(), false));
        staticObs.addAll(level.getStaticObstacles());
        for (int i = 0; i < staticObs.size(); i++) {
            StaticObstacle s = staticObs.get(i);
            s.setBodyType(BodyDef.BodyType.StaticBody);
            s.setDensity(defaults.getFloat("density", 0.0f));
            s.setFriction(defaults.getFloat("friction", 0.0f));
            s.setRestitution(defaults.getFloat("restitution", 0.0f));
            s.setDrawScale(scale);
            s.setSensor(false);
            s.setName("debris");
            addObject(s);
        }

        rewards = new ArrayList<>();
        rewards.addAll(level.getRewards());

        for (int i = 0; i < rewards.size(); i++) {
            StaticObstacle r = rewards.get(i);
            r.setBodyType(BodyDef.BodyType.StaticBody);
            r.setDensity(defaults.getFloat("density", 0.0f));
            r.setFriction(defaults.getFloat("friction", 0.0f));
            r.setRestitution(defaults.getFloat("restitution", 0.0f));
            r.setDrawScale(scale);
//            r.setTexture(starCollectTextures[0]);
            r.setName("rewards");
            addObject(r);
        }

        checkpoints = new ArrayList<>();
        checkpoints.addAll(level.getCheckpoints());

        for (int i = 0; i < checkpoints.size(); i++) {
            Checkpoint c = checkpoints.get(i);
            c.setBodyType(BodyDef.BodyType.StaticBody);
            c.setDensity(defaults.getFloat("density", 0.0f));
            c.setFriction(defaults.getFloat("friction", 0.0f));
            c.setRestitution(defaults.getFloat("restitution", 0.0f));
            c.setDrawScale(scale);
            c.setName("checkpoint" + i);
            addObject(c);
        }

        checkpointElapsedTime = 0;
        holeElapsedTime = 0;
        shipHurtElapsedTime = 0;
        starCollectElpasedTime = 0;
        planetResizeElapsedTime = -1;
        planetElapsedTime = 0;
        solarFlareElapsedTime = 0;
        universalElapsedTime = 0;
        enemyElapsedTime = 0;


        int numEnemies = 500;

//        if(toLoad == 7) {
//            Vector2[] enemyPath = {new Vector2(10, 10)};//{new Vector2(5, 5), new Vector2(5, 10), new Vector2(10, 10), new Vector2(10, 5), new Vector2(7.5f, 2.5f)};
//            //53.75, 29.5
//
//            for (int i = 0; i < numEnemies; i++) {
//                Enemy e = new Enemy(new Vector2(-i/10, -i/10), enemyPath, ship.getPos(), scale);
//                addObject(e);
//                enemies.add(e);
//            }
//
//
//            Enemy enemy1 = new Enemy(new Vector2(0, 0), enemyPath, ship.getPos(), scale);
//            addObject(enemy1);
//
//            enemies.add(enemy1);
//        }

        enemies.addAll(level.getEnemies());
        for (Enemy e : enemies) {
            addObject(e);
        }
        ai = new AIController(enemies, ship, defaults, scale);





        //ai = new AIController(es, ship, defaults, scale);


//        ArrayList<Enemy> el = ai.addSwarm(Vector2.Zero, 20);
//        es.addAll(el);
//        for (Enemy e:el) {
//            addObject(e);
//        }



        pathController = new Path();

        selector = new ObstacleSelector(world);
        selector.setDrawScale(scale);

        if (easyLoop.isPlaying()) {
            easyLoop.stop();
        }

        if (easyIntro.isPlaying()) {
            easyIntro.stop();
        }
        if (menuLoop.isPlaying()) {
            menuLoop.stop();
        }

        if (menuIntro.isPlaying()) {
            menuIntro.stop();
        }

        if (enemyMusic.isPlaying()) {
            enemyMusic.stop();
        }

        easyIntro.setOnCompletionListener(new Music.OnCompletionListener() {

            @Override
            public void onCompletion(Music music) {
                easyLoop.setLooping(true);
                easyLoop.setVolume(1f);
                easyLoop.play();


            }
        });
        easyIntro.setVolume(1f);
        easyIntro.play();

        enemyAreas = level.getEnemyAreas();


        /** Declare a path shader (which draws the path) */
        pathShader = new PathShader(pixel);
        pathShader.setDrawScale(scale);

        pathController.calculatePath(checkpoint, checkpointVel, checkpoint, checkpointVel, resizables);

        updateLocations();

    }

    /// CONTACT LISTENER METHODS
    /**
     * Callback method for the start of a collision
     *
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
        Body body1 = contact.getFixtureA().getBody();
        Body body2 = contact.getFixtureB().getBody();

        Object fd1 = body1.getUserData();
        Object fd2 = body2.getUserData();

        if (fd1 != null && fd2 != null) { // non-wall collision
            if (fd1 == ship && fd2 == goalDoor || fd2 == ship && fd1 == goalDoor) {
                exit.play(soundsVol);
                setComplete(true);
                pause();
                Gdx.input.setInputProcessor(complete);
            } else if (fd1 == ship && fd2 instanceof Planet) {
                collisionSound.play(soundsVol);
                health -= 1;
                ship.setHealth(health);
                ((Rocket)fd1).setInvincible(true);
                shipHurtElapsedTime = 0;
            } else if (fd2 == ship && fd1 instanceof Planet) {
                collisionSound.play(soundsVol);
                health -= 1;
                ship.setHealth(health);
                ship.setInvincible(true);
                shipHurtElapsedTime = 0;
            } else if (fd2 == ship && fd1 instanceof StaticObstacle && fd1 != entryDoor && fd1 != goalDoor) {
                if(((StaticObstacle) fd1).getName().equals("rewards")){
                    starCollectSound.play(soundsVol);
                    ((StaticObstacle) fd1).setColleted(true);
                    starCollectElpasedTime = 0;
                    numStars++;
                    rewards.remove(fd1);
                    staticObs.remove(fd1);
                    objects.remove(fd1);
//                    float range = REWARD_BLAST_RANGE;
//                    blastBackEnemies(((StaticObstacle) fd1).getPosition(), REWARD_FORCE, range);
                }else{
                    collisionSound.play(soundsVol);
                    health -= 1;
                    ship.setHealth(health);
                    ship.setInvincible(true);
                    shipHurtElapsedTime = 0;
                }

            } else if (fd1 == ship && fd2 instanceof StaticObstacle && fd2 != entryDoor && fd2 != goalDoor) {
                if(((StaticObstacle) fd2).getName().equals("rewards")){
                    starCollectSound.play(soundsVol);
                    ((StaticObstacle) fd2).setColleted(true);
                    starCollectElpasedTime = 0;
                    numStars++;
                    rewards.remove(fd2);
                    staticObs.remove(fd2);
                    objects.remove(fd2);

//                    float range = REWARD_BLAST_RANGE;
//                    blastBackEnemies(((StaticObstacle) fd2).getPosition(), REWARD_FORCE, range);
                }else{
                    collisionSound.play(soundsVol);
                    health -= 1;
                    ship.setHealth(health);
                    ship.setInvincible(true);
                    shipHurtElapsedTime = 0;
                }

            } else if (fd1 == ship && fd2 instanceof Enemy && !ship.getInvincible()) {
                collisionSound.play(soundsVol);
                health -= 0.333;
                ship.setHealth(this.health);
                ship.setInvincible(true);
                shipHurtElapsedTime = 0;
                float range = SHIP_BLAST_RANGE;
                blastBackEnemies(ship.getPosition(), SHIP_FORCE, range, true);
            } else if (fd2 == ship && fd1 instanceof Enemy && !ship.getInvincible()) {
                collisionSound.play(soundsVol);
                this.health -= 0.333;
                ship.setHealth(this.health);
                ship.setInvincible(true);
                shipHurtElapsedTime = 0;
                float range = SHIP_BLAST_RANGE;
                blastBackEnemies(ship.getPosition(), SHIP_FORCE, range, true);
            } else if (fd1 == ship && fd2 instanceof Checkpoint && !((Checkpoint)fd2).isCollected()) {
                checkSound.play(soundsVol);
                checkpointElapsedTime = 0;
                checkpoint.set(((Checkpoint)fd2).getPos());
                checkpointVel.set(ship.getVelocity());
                hitCheckpoint = true;
//                for (int i = 0; i< es.size(); i++){
//                    ai.removeEnemy(es.get(i));
//                }
                enemyElapsedTime = 0;
                removeAllEnemy = true;
//                ai.removeAllEnemy();
                enemies = ai.getEnemies();
                ((Checkpoint)fd2).changeCollectAnimation(true);
            } else if (fd2 == ship && fd1 instanceof Checkpoint && !((Checkpoint)fd1).isCollected()) {
                checkSound.play(soundsVol);
                checkpointElapsedTime = 0;
                checkpoint.set(((Checkpoint)fd1).getPos());
                checkpointVel.set(ship.getVelocity());
                ((Checkpoint)fd1).changeCollectAnimation(true);
                hitCheckpoint = true;
                enemyElapsedTime = 0;
                removeAllEnemy = true;
                shipExplodeSound.play();
//                ai.removeAllEnemy();
                if (enemyMusic.isPlaying()) {
                    enemyMusic.stop();
                }
                if (!easyLoop.isPlaying()){
                    easyLoop.play();
                    easyLoop.setLooping(true);
                }

//                enemies = ai.getEnemies();
            } else if (fd1 instanceof Enemy && fd2 instanceof Planet) {
                ((Enemy) fd1).setExplode(true);
                globalEnemy = ((Enemy) fd1);
                shipExplodeSound.play();
//                enemyElapsedTime=0;
                ai.removeEnemy((Enemy) fd1);
            } else if (fd2 instanceof Enemy && fd1 instanceof Planet) {
                ((Enemy) fd2).setExplode(true);
                globalEnemy = ((Enemy) fd2);
                shipExplodeSound.play();
//                enemyElapsedTime=0;
                ai.removeEnemy((Enemy) fd2);
            } else if (fd1 == ship && fd2 instanceof DynamicObstacle && !ship.getInvincible()) {
                ((GameObject)fd2).setCollided(true);
                collisionSound.play(soundsVol);
                health -= 1;
                ship.setHealth(health);
                ship.setInvincible(true);
                shipHurtElapsedTime = 0;
            } else if (fd2 == ship && fd1 instanceof DynamicObstacle && !ship.getInvincible()) {
                ((GameObject)fd1).setCollided(true);
                collisionSound.play(soundsVol);
                health -= 1;
                ship.setHealth(health);
                ship.setInvincible(true);
                shipHurtElapsedTime = 0;
            }
            else if (fd1 instanceof Planet && fd2 instanceof DynamicObstacle) {
                ((GameObject)fd2).setCollided(true);
            } else if (fd2 instanceof Planet && fd1 instanceof DynamicObstacle) {
                ((GameObject)fd1).setCollided(true);
            }
        }

    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when two objects cease to touch.  We do not use it.
     */
    public void endContact(Contact contact) {
        return;
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    private final Vector2 cache = new Vector2();

    private void drawBoundTextures() {
        // draw bounds
        determineBoundOpacity();
//        super.setPosition(((canvas.getWidth() / 2) - shipPos.x) / drawScale.x + worldPosition.x,
//                ((canvas.getHeight() / 2) - shipPos.y) / drawScale.y + worldPosition.y);
        float boundScaleTB = ((float) canvas.getHeight())/boundaryTextureBot.getRegionHeight();
        float shortEdge =  canvas.getHeight();
        float longEdge = boundaryTextureTop.getRegionWidth()*boundScaleTB;
        // magic numbers are manually determined offsets
        canvas.draw(boundaryTextureLeft, boundOpacity, 0, 0,
                -ship.getPos().x-40, -ship.getPos().y, shortEdge, longEdge);
        canvas.draw(boundaryTextureRight, boundOpacity, 0, 0,
                -ship.getPos().x+level.getMapPixelWidth()+canvas.getWidth()/2f-35, -ship.getPos().y,
                shortEdge, longEdge);

        canvas.draw(boundaryTextureBot, boundOpacity,0, 0,
                -ship.getPos().x-longEdge+180, -canvas.getHeight()/2f-ship.getPos().y+40,
                longEdge, shortEdge);
        canvas.draw(boundaryTextureBot, boundOpacity,0, 0,
                -ship.getPos().x+180, -canvas.getHeight()/2f-ship.getPos().y+40,
                longEdge, shortEdge);
        canvas.draw(boundaryTextureBot, boundOpacity,0, 0,
                -ship.getPos().x+longEdge+180, -canvas.getHeight()/2f-ship.getPos().y+40,
                longEdge, shortEdge);

        canvas.draw(boundaryTextureTop, boundOpacity, 0, 0,
                -ship.getPos().x+180-longEdge, canvas.getHeight()/2f+level.getMapPixelHeight()-ship.getPos().y-40,
                longEdge, shortEdge);
        canvas.draw(boundaryTextureTop, boundOpacity, 0, 0,
                -ship.getPos().x+180, canvas.getHeight()/2f+level.getMapPixelHeight()-ship.getPos().y-40,
                longEdge, shortEdge);
        canvas.draw(boundaryTextureTop, boundOpacity, 0, 0,
                -ship.getPos().x+180+longEdge, canvas.getHeight()/2f+level.getMapPixelHeight()-ship.getPos().y-40,
                longEdge, shortEdge);
    }


    /**
     * Draw the physics objects together with foreground and background
     *
     * This is completely overridden to support custom background and foreground art.
     *
     * @param dt Timing values from parent loop
     */
    public void draw(float dt) {
        canvas.shaderOff();
        fbo.begin();
        canvas.clear();

        // to prevent the blue background from showing in camera edge movement
        float backgroundOffsetX = -700f;
        float backgroundOffsetY = -400f;
        temp.set((canvas.getWidth() / 2) - ship.getPos().x + backgroundOffsetX,
                ((canvas.getHeight() / 2) - ship.getPos().y + backgroundOffsetY));

        // Draw background with camera translations
        moveCamera();
        canvas.begin();
        level.drawLevel(canvas, scale, temp);
        canvas.end();

        canvas.begin();

        drawBoundTextures();

        checkpointElapsedTime += dt;
        holeElapsedTime += dt;
        shipHurtElapsedTime += dt;
        planetElapsedTime += dt;
        solarFlareElapsedTime += dt;
        universalElapsedTime += dt;
        starCollectElpasedTime += dt;
        enemyElapsedTime += dt;

        for (GameObject obj : objects) {
            if (obj instanceof Checkpoint) {
                if (((Checkpoint) obj).isCollectedAnimation()) {
                    if ((checkpointCollectedAnimation.getKeyFrame(checkpointElapsedTime, true)) == checkpointCollectedTextures[11]) {
                        ((Checkpoint) obj).collect();
                        ((Checkpoint) obj).changeCollectAnimation(false);
                    }
                    ((Checkpoint) obj).draw(canvas, (TextureRegion) (checkpointCollectedAnimation.getKeyFrame(checkpointElapsedTime, true)));
                } else {
                    ((Checkpoint) obj).draw(canvas, (TextureRegion) (checkpointAnimation.getKeyFrame(checkpointElapsedTime, true)));
                }
            }
            else if (obj instanceof StaticObstacle) {
                StaticObstacle so = (StaticObstacle)obj;
                if (obj.getName().equals("entry")) {
                    if (!so.getEntryAnimation()) {
                        if ((blackholeStartAnimation.getKeyFrame(holeElapsedTime, true)) == blackholeStartTextures[11]) {
                            ((StaticObstacle) obj).setEntryAnimation(true);
                            holeElapsedTime = 0;
                            enter.play(soundsVol);
                        }
                        so.draw(canvas, (TextureRegion)(blackholeStartAnimation.getKeyFrame(holeElapsedTime, true)), 1);
                    } else {


                        so.draw(canvas, (TextureRegion)(blackholeLoopAnimation.getKeyFrame(holeElapsedTime, true)), 1);
                    }
                } else if (obj.getName().equals("goal")) {
                    if (!((StaticObstacle) obj).getEntryAnimation()) {
                        if ((whiteholeStartAnimation.getKeyFrame(holeElapsedTime, true)) == whiteholeStartTextures[11]) {
                            ((StaticObstacle) obj).setEntryAnimation(true);
                            holeElapsedTime = 0;
                        }
                        so.draw(canvas, (TextureRegion)(whiteholeStartAnimation.getKeyFrame(holeElapsedTime, true)), 1);
                    } else {
                        so.draw(canvas, (TextureRegion)(whiteholeLoopAnimation.getKeyFrame(holeElapsedTime, true)), 1);

                    }
                } else if (obj.getName().equals("debris")) {
                    so.draw(canvas, (TextureRegion)(debrisAnimation.getKeyFrame(universalElapsedTime, true)), 2f);
                }
                else if (obj.getName().equals("rewards")) {
                    if(so.getCollected()) {
                        if (starCollectElpasedTime < 1 && !so.getDisappear()) {
                            so.draw(canvas, (TextureRegion) (starCollectAnimation.getKeyFrame(starCollectElpasedTime, true)), 2f);
                        } else if (starCollectElpasedTime >= 1) {
//                            so.setColleted(false);
                            so.setDisappear(true);
                        }
                    }else if(so.getDisappear()){
                        continue;
                    }else {
                        so.draw(canvas, (TextureRegion)(starLoopAnimation.getKeyFrame(universalElapsedTime, true)), 2f);
                    }
                }

            } else if (obj instanceof Rocket) {
                continue;

            } else if (obj instanceof MovParticle){
//                Vector2 pos = Helper.boxCoordsToWorldCoords(scale, ship.getPosition());
                for(int i=0; i< movParticles.size()-1; i++){
                    movParticles.get(i).draw(canvas, new Vector2(640-1200+i*300, 360 -500 + (i+1)*250));

                }
                movParticles.get(movParticles.size()-1).draw(canvas, new Vector2(640+300, 360+250));
            } else if (obj instanceof Planet) {
                Planet p = (Planet)obj;
                float radius = p.getRadius() * 3.35f;
                Vector2 position = Helper.boxCoordsToWorldCoords(scale, p.getPosition());
                if (selected == p && (selected.changeSize(-1) || selected.changeSize(1))) {
                    canvas.draw(highlight, Color.WHITE, position.x - radius / 2, position.y - radius / 2, radius, radius);
                }
                if (p.getColor() == Planet.PlanetColor.SUN) {
                    TextureRegion tr = (TextureRegion) (sunPlanetAnimation.getKeyFrame(planetElapsedTime, true));
                    p.setTexture(tr);
                }
                if (p.isGrowing()) {
                    planetResizeElapsedTime += dt;

                    if (p.getRadius() < p.getRadiusAfterChange()) {
                        p.resize(SIZE_RATE, ship);
                        //blastBackEnemies(p.getPosition(), PLANET_FORCE, p.getRadius() * 2f / p.getDrawScale().x, false);
                    }

                    Animation planetResizeAnimation = bluePlanetResizeAnimation;
                    TextureRegion[] planetResizeTextures = bluePlanetResizeTextures;
                    switch (p.getColor()) {
                        case BLUE:
                            planetResizeAnimation = bluePlanetResizeAnimation;
                            planetResizeTextures = bluePlanetResizeTextures;
                            break;
                        case GREEN:
                            planetResizeAnimation = greenPlanetResizeAnimation;
                            planetResizeTextures = greenPlanetResizeTextures;
                            break;
                        case PINK:
                            planetResizeAnimation = pinkPlanetResizeAnimation;
                            planetResizeTextures = pinkPlanetResizeTextures;
                            break;
                        case PURPLE:
                            planetResizeAnimation = purplePlanetResizeAnimation;
                            planetResizeTextures = purplePlanetResizeTextures;
                            break;
                        case SUN:
                            planetResizeAnimation = sunPlanetResizeAnimation;
                            planetResizeTextures = sunPlanetResizeTextures;
                            break;
                        case YELLOW:
                            planetResizeAnimation = yellowPlanetResizeAnimation;
                            planetResizeTextures = yellowPlanetResizeTextures;
                            break;
                    }
                    TextureRegion tr = (TextureRegion) (planetResizeAnimation.getKeyFrame(planetResizeElapsedTime, true));
                    canvas.draw(tr, Color.WHITE, position.x - radius / 2, position.y - radius / 2, radius, radius);
                    if (tr == planetResizeTextures[7]) {
                        p.setGrowing(false);
                        planetResizeElapsedTime = -1;
                    }
                } else if (p.isShrinking()) {
                    planetResizeElapsedTime += dt;

                    if (p.getRadius() > p.getRadiusAfterChange()) {
                        p.resize(-SIZE_RATE, ship);
                    }

                    Animation planetResizeAnimation = bluePlanetResizeAnimationReverse;
                    TextureRegion[] planetResizeTextures = bluePlanetResizeTexturesReverse;
                    switch (p.getColor()) {
                        case BLUE:
                            planetResizeAnimation = bluePlanetResizeAnimationReverse;
                            planetResizeTextures = bluePlanetResizeTexturesReverse;
                            break;
                        case GREEN:
                            planetResizeAnimation = greenPlanetResizeAnimationReverse;
                            planetResizeTextures = greenPlanetResizeTexturesReverse;
                            break;
                        case PINK:
                            planetResizeAnimation = pinkPlanetResizeAnimationReverse;
                            planetResizeTextures = pinkPlanetResizeTexturesReverse;
                            break;
                        case PURPLE:
                            planetResizeAnimation = purplePlanetResizeAnimationReverse;
                            planetResizeTextures = purplePlanetResizeTexturesReverse;
                            break;
                        case SUN:
                            planetResizeAnimation = sunPlanetResizeAnimationReverse;
                            planetResizeTextures = sunPlanetResizeTexturesReverse;
                            break;
                        case YELLOW:
                            planetResizeAnimation = yellowPlanetResizeAnimationReverse;
                            planetResizeTextures = yellowPlanetResizeTexturesReverse;
                            break;
                    }
                    TextureRegion tr = (TextureRegion) (planetResizeAnimation.getKeyFrame(planetResizeElapsedTime, true));
                    canvas.draw(tr, Color.WHITE, position.x - radius / 2, position.y - radius / 2, radius, radius);
                    if (tr == planetResizeTextures[7]) {
                        p.setShrinking(false);
                        planetResizeElapsedTime = -1;
                    }
                }

                if (p.getColor() != Planet.PlanetColor.SUN) {
                    p.draw(canvas, p.getRadius() * 1.5f, Color.WHITE);
                } else {
                    if (!solarFlareAnimation.isAnimationFinished(solarFlareElapsedTime)) {
                        p.getSolarFlare().setActive(true);
                        TextureRegion trFlare = (TextureRegion) (solarFlareAnimation.getKeyFrame(solarFlareElapsedTime, true));
                        p.setSolarFlareDistance(solarFlareAnimation.getKeyFrameIndex(solarFlareElapsedTime) + 1);
                        p.draw(canvas, p.getRadius() * 1.5f, trFlare);
                        if (solarFlareElapsedTime <= SOLAR_FLARE_COOLDOWN/5) {
                            Color c = new Color(Color.RED);
                            c.a = 0.35f * (1-((solarFlareElapsedTime) / (SOLAR_FLARE_COOLDOWN/5f)));
                            p.draw(canvas, p.getRadius() * 1.5f, c);
                        }
                    } else {
                        p.getSolarFlare().setActive(false);
                        p.draw(canvas, p.getRadius() * 1.5f, Color.WHITE);
                        if (solarFlareElapsedTime > SOLAR_FLARE_COOLDOWN-SOLAR_FLARE_TELE_TIME) {
                            Color c = new Color(Color.RED);
                            // 0.35f is the max opacity
                            c.a = 0.35f * (1-((SOLAR_FLARE_COOLDOWN-solarFlareElapsedTime) / SOLAR_FLARE_TELE_TIME));
                            p.draw(canvas, p.getRadius() * 1.5f, c);
                        }
                        if (solarFlareElapsedTime > SOLAR_FLARE_COOLDOWN) {
                            solarFlareElapsedTime = 0;
                        }
                    }
                }
            } else if (obj instanceof DynamicObstacle) {
                DynamicObstacle dynamicObst = (DynamicObstacle) obj;
                dynamicObst.setTexture((TextureRegion) (cometAnimation.getKeyFrame(universalElapsedTime, true)));
                dynamicObst.draw(canvas);
            } else if( obj instanceof Enemy){
                if(enemyElapsedTime < 1){
                    if(removeAllEnemy){
                        Enemy enemy = (Enemy) obj;
                        enemy.setTexture((TextureRegion) (enemyExplodeAnimation.getKeyFrame(universalElapsedTime, true)));
                        enemy.draw(canvas);
                    }
//                    else if(globalEnemy!=null){
//                        Enemy enemy = globalEnemy;
//                        enemy.setTexture((TextureRegion) (enemyExplodeAnimation.getKeyFrame(universalElapsedTime, true)));
//                        enemy.draw(canvas);
//                    }
                    else obj.draw(canvas);

                } else if(enemyElapsedTime >= 1) {
                    if(removeAllEnemy){
                        ai.removeAllEnemy();
                    }
//                    enemies = ai.getEnemies();
                    else if(globalEnemy!=null){
                        ai.removeEnemy(globalEnemy);
                        if(obj!=globalEnemy){
                            obj.draw(canvas);
                        }
                    } else obj.draw(canvas);
                }
            } else {
                obj.draw(canvas);
            }
        }
        canvas.end();
        canvas.begin();
        // draw ship separately for level edge / camera purposes
        if (ship.getInvincible()) {
            if (shipHurtElapsedTime < INVINCIBILITY_LENGTH && (int)(shipHurtElapsedTime * 4) % 2 == 0) {
                ship.draw(canvas);
            } else if (shipHurtElapsedTime >= INVINCIBILITY_LENGTH) {
                ship.setInvincible(false);
            }
            if (shipHurtElapsedTime < 1) {
                TextureRegion tr = (TextureRegion) (shipHurtAnimation.getKeyFrame(shipHurtElapsedTime, true));
                canvas.draw(tr, canvas.getWidth() / 2 - tr.getRegionWidth() / 2, canvas.getHeight() / 2 - tr.getRegionHeight() / 2);
            }
        } else {
            ship.draw(canvas);
        }

        pathShader.drawPath(canvas, forwardPath, ship.getPos());
        pathShader.drawPath(canvas, backwardPath, ship.getPos());
        Vector2 position = Helper.boxCoordsToWorldCoords(scale, ship.getPosition());

        canvas.end();
        canvas.begin();

        if (levelNum == 1) {
            canvas.draw(l1text, position.x-200, position.y-250);
        } else if (levelNum == 2) {
            canvas.draw(l2text, position.x-200, position.y-320 +l2text2.getRegionHeight());
            canvas.draw(l2text2, position.x-200, position.y-320);
        } else if (levelNum == 3) {
            canvas.draw(l3text, position.x-5, position.y-300);
        } else if (levelNum == 4) {
            canvas.draw(l4text, position.x-5, position.y-300);
        } else if (levelNum == 5) {
            canvas.draw(l5text, position.x-5, position.y-320 +l5text2.getRegionHeight());
            canvas.draw(l5text2, position.x-5, position.y-320);
        } else if (levelNum == 7) {
            canvas.draw(ml2text, position.x-5, position.y-300+ml2text2.getRegionHeight());
            canvas.draw(ml2text2, position.x-5, position.y-300);
            canvas.draw(ml1text, position.x-5-ml2text2.getRegionWidth(), position.y-300);
        }


//        // draw health bar frame, health bar bg, health bar with dynamic color
//        canvas.draw(pixel, Color.GOLD, 0, 4  , Gdx.graphics.getWidth(), 20);
//        canvas.draw(pixel, Color.WHITE, 0, 8, Gdx.graphics.getWidth(), 12);
//        if(health>0.5f){
//            canvas.draw(pixel, Color.GREEN, 0, 8, Gdx.graphics.getWidth()*health, 12);
//        }else if (health>0.25f){
//            canvas.draw(pixel, Color.CORAL, 0, 8, Gdx.graphics.getWidth()*health, 12);
//        }else{
//            canvas.draw(pixel, Color.RED, 0, 8, Gdx.graphics.getWidth()*health, 12);
//        }

        // draw level name
        if (levelNameCountdown > 250) {
            levelNameCountdown--;
            canvas.drawText(level.levelfancyname, ui.getFont(), canvas.getHeight()-canvas.getHeight()*0.975f, canvas.getHeight()*0.975f+200*(float)(levelNameCountdown-250)/(float)300);
        } else if (levelNameCountdown > 50) {
            levelNameCountdown--;
            canvas.drawText(level.levelfancyname, ui.getFont(), canvas.getHeight()-canvas.getHeight()*0.975f, canvas.getHeight()*0.975f);
        } else if (levelNameCountdown > 0) {
            levelNameCountdown--;
            canvas.drawText(level.levelfancyname, ui.getFont(), canvas.getHeight()-canvas.getHeight()*0.975f, canvas.getHeight()*0.975f-200*(float)(levelNameCountdown-50)/(float)300);
        }

        // draw minimap
        TextureRegion minimapTex = minimap.generateMap();
        float mapWidth = canvas.getWidth()/6f; // these are for resizing the map
        float mapHeight = canvas.getHeight()/6f;
        float mapPadding = canvas.getHeight()/40f;
        minimapTex.flip(false, true);
        canvas.draw(minimapTex, Color.WHITE, canvas.getWidth()-mapWidth-mapPadding, canvas.getHeight()-mapHeight-mapPadding, mapWidth, mapHeight);

        canvas.draw(starIcon, Color.WHITE, canvas.getWidth()*0.946f, canvas.getHeight()*0.766f, starIcon.getRegionWidth()*0.54f, starIcon.getRegionHeight()*0.54f);
        canvas.drawText(Integer.toString(numStars), ui.getFont(), canvas.getWidth()*0.97f, canvas.getHeight()*0.8f);

        canvas.draw(pixel, Color.CLEAR, 0, 8, Gdx.graphics.getWidth()*health, 12);

        if (isComplete) {
            float a = (float)(EXIT_COUNT/4-countdown)/(EXIT_COUNT/16);
            if (a > 0.8) {
                a = 0.8f;
            }
            Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
            game.shapeRenderer.setColor(0, 0, 0, a);
            game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            game.shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            game.shapeRenderer.end();
            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
//            if (Gdx.input.getInputProcessor() != complete)
//                Gdx.input.setInputProcessor(complete);
            complete.act(dt);
            complete.draw();
            canvas.drawTextCentered("STAGE COMPLETE", ui.getTitleFont(), 100);
        } else if (paused) {
            Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
            game.shapeRenderer.setColor(0, 0, 0, 0.8f);
            game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            game.shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            game.shapeRenderer.end();
            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

            if (settings) {
                switch (ui.getOverlay()) {
                    case SETTINGS:
                        ui.getSettings().act(dt);
                        ui.getSettings().draw();
                        canvas.drawTextCentered("SETTINGS", ui.getTitleFont(), 100);
                        break;
                    case CONTROLS:
                        ui.getControlSettings().act(dt);
                        ui.getControlSettings().draw();
                        canvas.drawTextCentered("CONTROLS", ui.getTitleFont(), 240);
                        break;
                    case AUDIO_DISPLAY:
                        ui.getAudioDisplaySettings().act(dt);
                        ui.getAudioDisplaySettings().draw();
                        canvas.drawTextCentered("AUDIO/DISPLAY", ui.getTitleFont(), 100);
                        break;
                    case CREDITS:
                        ui.getCredits().act(dt);
                        ui.getCredits().draw();
                        canvas.drawTextCentered("CREDITS", ui.getTitleFont(), 240);
                        break;
                }
            } else if (!isComplete && !isFailed) {
                pauseScreen.act(dt);
                pauseScreen.draw();
                canvas.drawTextCentered("PAUSED", ui.getTitleFont(), 100);
            }
        }

        if (isFailed) {
            Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
            game.shapeRenderer.setColor(0, 0, 0, (float)(EXIT_COUNT-countdown)/(EXIT_COUNT/16));
            game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            game.shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            game.shapeRenderer.end();
            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

            //if (toFailed) {
            failed.act(dt);
            failed.draw();
            canvas.drawTextCentered("STAGE FAILED", ui.getTitleFont(), 100);
            //}
        }

        if (theEnd) {
            if (endScreenNum == 0) {
                canvas.draw(endScreen, Color.WHITE, 0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight());
            } else if (endScreenNum == 1) {
                canvas.draw(theEndScreen, Color.WHITE, 0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight());
            }

            if (endScreenNum == 0)
                canvas.draw(endFrame, Color.WHITE, 0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight());

            if (endText == 0) {
                canvas.draw(endText1, Color.WHITE, (float) canvas.getWidth()/2-endText1.getRegionWidth()/3, 27.5f, endText1.getRegionWidth()/1.5f, 35);
            } else if (endText == 1) {
                canvas.draw(endText2, Color.WHITE, (float) canvas.getWidth()/2-endText2.getRegionWidth()/3, 27.5f, endText2.getRegionWidth()/1.5f, 35);
            }
        }

        // fps
//        String fps = "FPS: "+Double.toString((cycles-59)/sumDelta);
//        canvas.drawText(fps, ui.getFont(), 0, canvas.getHeight()-10);

        canvas.end();
        fbo.end();

        Texture texture = fbo.getColorBufferTexture();
        TextureRegion textureRegion = new TextureRegion(texture);
        textureRegion.flip(false, true);

        canvas.clear();
        canvas.begin();
        canvas.shaderOn();
        effects.draw(canvas, dt);
        canvas.draw(textureRegion, 0, 0);
        canvas.end();


        if (debug) {
            canvas.beginDebug();
            for(GameObject obj : objects) {
                obj.drawDebug(canvas);
            }
            canvas.endDebug();
        }
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        worldStep = 0;
        paused = true;
        Gdx.input.setInputProcessor(pauseScreen);
    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        if (!isComplete && !isFailed) {
            worldStep = 1/60.0f;
            paused = false;
            Gdx.input.setInputProcessor(null);
        }
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

    private void doStarBlast(){
        numStars--;
        float range = REWARD_BLAST_RANGE;
        blastBackEnemies(ship.getPosition(), REWARD_FORCE, range, true);

    }
}

