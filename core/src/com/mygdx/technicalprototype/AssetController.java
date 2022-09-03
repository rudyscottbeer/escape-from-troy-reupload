package com.mygdx.technicalprototype;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.technicalprototype.assets.AssetDirectory;

public class AssetController {

    /**
     * Physics constants for initialization
     */
    public static JsonValue constants;

    /**
     * The texture asset for the planets
     */
    public static TextureRegion bluePlanetTexture;
    public static TextureRegion greenPlanetTexture;
    public static TextureRegion pinkPlanetTexture;
    public static TextureRegion purplePlanetTexture;
    public static TextureRegion sunPlanetTexture;
    public static TextureRegion yellowPlanetTexture;
    public static TextureRegion highlight;

    public static TextureRegion l1text;
    public static TextureRegion l2text;
    public static TextureRegion l2text2;
    public static TextureRegion l3text;
    public static TextureRegion l4text;
    public static TextureRegion l5text;
    public static TextureRegion l5text2;
    public static TextureRegion ml1text;
    public static TextureRegion ml2text2;
    public static TextureRegion ml2text;

    /**
     * The texture asset for the entry/goal
     */
    public static TextureRegion entryTile;
    public static TextureRegion goalTile;
    /**
     * The texture asset for the background
     */
    public static TextureRegion backgroundTexture;

    /**
     * The texture assets for the boundary
     */

    public static TextureRegion boundaryTextureLeft;
    public static TextureRegion boundaryTextureRight;
    public static TextureRegion boundaryTextureTop;
    public static TextureRegion boundaryTextureBot;

    public static TextureRegion starIcon;

    public static TextureRegion endFrame;
    public static TextureRegion endScreen;
    public static TextureRegion theEndScreen;
    public static TextureRegion endText1;
    public static TextureRegion endText2;

    /**
     * The texture asset for the path pixel
     */
    public static TextureRegion pixel;
    public static TextureRegion debugPlanet;
    /**
     * The texture asset for the path pixel
     */
    public static TextureRegion pathTexture;
    /**
     * The texture assets for the rocket
     */
    public static TextureRegion rocketTexture;
    /** Texture asset for debris */
    public static TextureRegion[] debrisTextures;
    /** Texture assets for comets */
    public static TextureRegion[] cometTextures;
    /** The texture assets for the checkpoint */
    public static TextureRegion[] checkpointTextures;

    /** The texture assets for the star collected */
    public static TextureRegion[] starCollectTextures;

    /** The texture assets for the enemy exploded */
    public static TextureRegion[] enemyExplodeTextures;

    /** The texture assets for the star collected */
    public static TextureRegion[] starLoopTextures;
    /**
     * The texture assets for the checkpoint collected
     */
    public static TextureRegion[] checkpointCollectedTextures;
    /**
     * The texture assets for the white/black holes
     */
    public static TextureRegion[] whiteholeStartTextures;
    public static TextureRegion[] whiteholeLoopTextures;
    public static TextureRegion[] blackholeStartTextures;
    public static TextureRegion[] blackholeLoopTextures;
    /**
     * The texture assets for the ship collision
     */
    public static TextureRegion[] shipHurtTextures;
    /**
     * The texture assets for the planet resizing
     */
    public static TextureRegion[] bluePlanetResizeTextures;
    public static TextureRegion[] greenPlanetResizeTextures;
    public static TextureRegion[] pinkPlanetResizeTextures;
    public static TextureRegion[] purplePlanetResizeTextures;
    public static TextureRegion[] sunPlanetResizeTextures;
    public static TextureRegion[] sunPlanetTextures;
    public static TextureRegion[] solarFlareTextures;
    public static TextureRegion[] yellowPlanetResizeTextures;
    public static TextureRegion[] bluePlanetResizeTexturesReverse;
    public static TextureRegion[] greenPlanetResizeTexturesReverse;
    public static TextureRegion[] pinkPlanetResizeTexturesReverse;
    public static TextureRegion[] purplePlanetResizeTexturesReverse;
    public static TextureRegion[] sunPlanetResizeTexturesReverse;
    public static TextureRegion[] yellowPlanetResizeTexturesReverse;

    /** Texture asset for the enemy */
    public static TextureRegion enemyTexture;
    public static TextureRegion particle;

    /**
     * The texture asset for the planet
     */
    public static TextureRegion planetTexture;
    /**
     * The texture assets for the rocket
     */
    public static TextureRegion[] ship1Textures;
    public static TextureRegion[] ship2Textures;
    public static TextureRegion[] ship3Textures;
    
    /**
     * The intro sounds
     */
    public static Music easyIntro;
    /**
     * The loop sounds
     */
    public static Music easyLoop;

    /**
     * The enemy sounds
     */
    public static Music enemyMusic;

    public static Music shipExplodeSound;
    public static Music[] musics = new Music[4];

    /**
     * Sound effects
     */
    public static Sound decreaseSize;
    public static Sound increaseSize;
    public static Sound decelerate;
    public static Sound accelerate;
    public static Sound click;
    public static Sound exit;
    public static Sound enter;
    public static Sound checkSound;
    public static Sound collisionSound;
    public static Sound starCollectSound;
    public static Sound shockwaveSound;
    public static Sound alertSound;
    public static Sound[] sounds = new Sound[12];

    private static float[] volumes = new float[3];

    public static int toLoad;
    /**
     * Checkpoint animation controller
     */
    public static Animation checkpointAnimation;
    /**
     * Checkpoint collected animation controller
     */
    public static Animation checkpointCollectedAnimation;
    /**
     * White/black hole animation controllers
     */
    public static Animation whiteholeStartAnimation;
    public static Animation whiteholeLoopAnimation;
    public static Animation blackholeStartAnimation;
    public static Animation blackholeLoopAnimation;
    /**
     * Ship collision animation controller
     */
    public static Animation shipHurtAnimation;

    /**
     * Star loop animation controller
     */
    public static Animation starLoopAnimation;

    /**
     * Star collection animation controller
     */
    public static Animation starCollectAnimation;

    /**
     * Enemy explosion animation controller
     */
    public static Animation enemyExplodeAnimation;

    /**
     * Resizing animation controllers
     */
    public static Animation bluePlanetResizeAnimation;
    public static Animation greenPlanetResizeAnimation;
    public static Animation pinkPlanetResizeAnimation;
    public static Animation purplePlanetResizeAnimation;
    public static Animation sunPlanetResizeAnimation;
    public static Animation sunPlanetAnimation;
    public static Animation solarFlareAnimation;
    public static Animation yellowPlanetResizeAnimation;
    public static Animation bluePlanetResizeAnimationReverse;
    public static Animation greenPlanetResizeAnimationReverse;
    public static Animation pinkPlanetResizeAnimationReverse;
    public static Animation purplePlanetResizeAnimationReverse;
    public static Animation sunPlanetResizeAnimationReverse;
    public static Animation yellowPlanetResizeAnimationReverse;

    /**
     * Time elapsed for checkpoint animations
     */
    public static float checkpointElapsedTime;
    /**
     * Time elapsed for white/black hole animations
     */
    public static float holeElapsedTime;
    /**
     * Time elapsed for ship collision animations
     */
    public static float shipHurtElapsedTime;

    /**
     * Time elapsed for star collection animations
     */
    public static float starCollectElpasedTime;

    /**
     * Time elapsed for planet resize animations
     */
    public static float planetResizeElapsedTime;
    /**
     * Time elapsed for planet resize animations
     */
    public static float planetElapsedTime;
    /**
     * Time elapsed for solar flare animations
     */
    public static float solarFlareElapsedTime;
    /**
     * Time elapsed for universal animations
     */
    public static float universalElapsedTime;

    /**
     * Time elapsed for enemy explode animations
     */
    public static float enemyElapsedTime;

    /**
     * ship1 animation controller
     */
    public static Animation ship1Animation;

    /**
     * ship2 animation controller
     */
    public static Animation ship2Animation;

    /**
     * ship3 animation controller
     */
    public static Animation ship3Animation;
    /**
     * Debris animation controller
     */
    public static Animation debrisAnimation;

    /**
     * comet animation controller
     */
    public static Animation cometAnimation;
    
    /** The Level Currently Loaded*/
    public static LevelLoader level;

    public static void gatherAssets(AssetDirectory directory, Vector2 scale) {
        int index;

        constants  = directory.getEntry( "rocket:constants", JsonValue.class );

        backgroundTexture = new TextureRegion(directory.getEntry( "prototype:background", Texture.class ));
        bluePlanetTexture = new TextureRegion(directory.getEntry( "prototype:blue_planet", Texture.class ));
        greenPlanetTexture = new TextureRegion(directory.getEntry( "prototype:green_planet", Texture.class ));
        pinkPlanetTexture = new TextureRegion(directory.getEntry( "prototype:pink_planet", Texture.class ));
        purplePlanetTexture = new TextureRegion(directory.getEntry( "prototype:purple_planet", Texture.class ));
        sunPlanetTexture = new TextureRegion(directory.getEntry( "prototype:sun_planet", Texture.class ));
        yellowPlanetTexture = new TextureRegion(directory.getEntry( "prototype:yellow_planet", Texture.class ));
        rocketTexture = new TextureRegion( directory.getEntry( "prototype:ship", Texture.class ) );
        boundaryTextureLeft = new TextureRegion(directory.getEntry("prototype:boundary_left", Texture.class));
        boundaryTextureRight = new TextureRegion(directory.getEntry("prototype:boundary_right", Texture.class));
        boundaryTextureTop = new TextureRegion(directory.getEntry("prototype:boundary_top", Texture.class));
        boundaryTextureBot = new TextureRegion(directory.getEntry("prototype:boundary_bot", Texture.class));


        StringBuilder levelName = new StringBuilder("level1");
        //levelName.append(toLoad);
        StringBuilder key = new StringBuilder("level:level");
        key.append(toLoad);
        level = new LevelLoader(levelName.toString(), directory.getEntry( key.toString(), JsonValue.class ), scale);

        TextureRegion[][] ship1Temp = TextureRegion.split(directory.getEntry( "prototype:ship1", Texture.class ), 120, 120);
        ship1Textures = new TextureRegion[10];

        index = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 5; j++) {
                ship1Textures[index++] = ship1Temp[i][j];
            }
        }

        TextureRegion[][] ship2Temp = TextureRegion.split(directory.getEntry( "prototype:ship2", Texture.class ), 120, 120);
        ship2Textures = new TextureRegion[10];

        index = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 5; j++) {
                ship2Textures[index++] = ship2Temp[i][j];
            }
        }

        TextureRegion[][] ship3Temp = TextureRegion.split(directory.getEntry( "prototype:ship3", Texture.class ), 120, 120);
        ship3Textures = new TextureRegion[10];

        index = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 5; j++) {
                ship3Textures[index++] = ship3Temp[i][j];
            }
        }

        TextureRegion[][] checkpointTemp = TextureRegion.split(directory.getEntry( "prototype:checkpoint", Texture.class ), 200, 200);
        checkpointTextures = new TextureRegion[12];

        index = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                checkpointTextures[index++] = checkpointTemp[i][j];
            }
        }

        TextureRegion[][] checkpointCollectedTemp = TextureRegion.split(directory.getEntry( "prototype:checkpoint_collected", Texture.class ), 200, 200);
        checkpointCollectedTextures = new TextureRegion[12];

        index = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                checkpointCollectedTextures[index++] = checkpointCollectedTemp[i][j];
            }
        }

        TextureRegion[][] cometTemp = TextureRegion.split(directory.getEntry( "prototype:comet", Texture.class ), 400, 400);
        cometTextures = new TextureRegion[14];

        index = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 7; j++) {
                cometTextures[index++] = cometTemp[i][j];
            }
        }

        TextureRegion[][] starLoopTemp = TextureRegion.split(directory.getEntry( "prototype:star-loop", Texture.class ), 200, 200);
        starLoopTextures = new TextureRegion[12];

        index = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                starLoopTextures[index++] = starLoopTemp[i][j];
            }
        }

        TextureRegion[][] starCollectTemp = TextureRegion.split(directory.getEntry( "prototype:star-collect", Texture.class ), 200, 200);
        starCollectTextures = new TextureRegion[12];

        index = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                starCollectTextures[index++] = starCollectTemp[i][j];
            }
        }

        TextureRegion[][] enemyExplodeTemp = TextureRegion.split(directory.getEntry( "prototype:enemy-explode", Texture.class ), 250, 250);
        enemyExplodeTextures = new TextureRegion[7];

        index = 0;
        for (int i = 0; i < 1; i++) {
            for(int j = 0 ; j < 7; j++){
                System.out.println(enemyExplodeTemp);
                enemyExplodeTextures[index++] = enemyExplodeTemp[i][j];
            }
        }

        TextureRegion[][] debrisTemp = TextureRegion.split(directory.getEntry( "shared:debris", Texture.class ), 300, 300);
        debrisTextures = new TextureRegion[12];

        index = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                debrisTextures[index++] = debrisTemp[i][j];
            }
        }

        enemyTexture = new TextureRegion(directory.getEntry("prototype:enemy", Texture.class));

        pathTexture = new TextureRegion(directory.getEntry( "prototype:pixel", Texture.class ));

        endFrame = new TextureRegion(directory.getEntry("prototype:loreframe", Texture.class));
        endScreen = new TextureRegion(directory.getEntry("prototype:endscreen", Texture.class));
        theEndScreen = new TextureRegion(directory.getEntry("prototype:theend", Texture.class));
        endText1 = new TextureRegion(directory.getEntry("prototype:endtext1", Texture.class));
        endText2 = new TextureRegion(directory.getEntry("prototype:endtext2", Texture.class));

        l1text = new TextureRegion(directory.getEntry("prototype:l1text", Texture.class));
        l2text = new TextureRegion(directory.getEntry("prototype:l2text", Texture.class));
        l2text2 = new TextureRegion(directory.getEntry("prototype:l2text2", Texture.class));
        l3text = new TextureRegion(directory.getEntry("prototype:l3text", Texture.class));
        l4text = new TextureRegion(directory.getEntry("prototype:l4text", Texture.class));
        l5text = new TextureRegion(directory.getEntry("prototype:l5text", Texture.class));
        l5text2 = new TextureRegion(directory.getEntry("prototype:l5text2", Texture.class));
        ml1text = new TextureRegion(directory.getEntry("prototype:ml1text", Texture.class));
        ml2text2 = new TextureRegion(directory.getEntry("prototype:ml2text2", Texture.class));
        ml2text = new TextureRegion(directory.getEntry("prototype:ml2text", Texture.class));
        highlight = new TextureRegion(directory.getEntry("prototype:highlight", Texture.class));

        goalTile  = new TextureRegion(directory.getEntry( "shared:goal", Texture.class ));
        entryTile  = new TextureRegion(directory.getEntry( "shared:entry", Texture.class ));
        particle = new TextureRegion(directory.getEntry("prototype:particle", Texture.class));

        TextureRegion[][] whiteholeStartTemp = TextureRegion.split(directory.getEntry( "prototype:whitehole_start", Texture.class ), 200, 200);
        whiteholeStartTextures = new TextureRegion[12];

        index = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                whiteholeStartTextures[index++] = whiteholeStartTemp[i][j];
            }
        }

        TextureRegion[][] whiteholeLoopTemp = TextureRegion.split(directory.getEntry( "prototype:whitehole_loop", Texture.class ), 200, 200);
        whiteholeLoopTextures = new TextureRegion[12];

        index = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                whiteholeLoopTextures[index++] = whiteholeLoopTemp[i][j];
            }
        }

        TextureRegion[][] blackholeStartTemp = TextureRegion.split(directory.getEntry( "prototype:blackhole_start", Texture.class ), 200, 200);
        blackholeStartTextures = new TextureRegion[12];

        index = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                blackholeStartTextures[index++] = blackholeStartTemp[i][j];
            }
        }

        TextureRegion[][] blackholeLoopTemp = TextureRegion.split(directory.getEntry( "prototype:blackhole_loop", Texture.class ), 200, 200);
        blackholeLoopTextures = new TextureRegion[12];

        index = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                blackholeLoopTextures[index++] = blackholeLoopTemp[i][j];
            }
        }

        TextureRegion[][] shipHurtTemp = TextureRegion.split(directory.getEntry( "prototype:ship_collision", Texture.class ), 120, 120);
        shipHurtTextures = new TextureRegion[3];

        index = 0;
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 3; j++) {
                shipHurtTextures[index++] = shipHurtTemp[i][j];
            }
        }

        TextureRegion[][] bluePlanetResizeTemp = TextureRegion.split(directory.getEntry( "prototype:planet_resize_blue", Texture.class ), 1000, 1000);
        bluePlanetResizeTextures = new TextureRegion[8];
        bluePlanetResizeTexturesReverse = new TextureRegion[8];

        index = 0;
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 8; j++) {
                bluePlanetResizeTextures[index++] = bluePlanetResizeTemp[i][j];
                bluePlanetResizeTexturesReverse[bluePlanetResizeTextures.length - index] = bluePlanetResizeTemp[i][j];
            }
        }

        TextureRegion[][] greenPlanetResizeTemp = TextureRegion.split(directory.getEntry( "prototype:planet_resize_green", Texture.class ), 1000, 1000);
        greenPlanetResizeTextures = new TextureRegion[8];
        greenPlanetResizeTexturesReverse = new TextureRegion[8];

        index = 0;
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 8; j++) {
                greenPlanetResizeTextures[index++] = greenPlanetResizeTemp[i][j];
                greenPlanetResizeTexturesReverse[greenPlanetResizeTextures.length - index] = greenPlanetResizeTemp[i][j];
            }
        }

        TextureRegion[][] pinkPlanetResizeTemp = TextureRegion.split(directory.getEntry( "prototype:planet_resize_pink", Texture.class ), 1000, 1000);
        pinkPlanetResizeTextures = new TextureRegion[8];
        pinkPlanetResizeTexturesReverse = new TextureRegion[8];

        index = 0;
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 8; j++) {
                pinkPlanetResizeTextures[index++] = pinkPlanetResizeTemp[i][j];
                pinkPlanetResizeTexturesReverse[pinkPlanetResizeTextures.length - index] = pinkPlanetResizeTemp[i][j];
            }
        }

        TextureRegion[][] purplePlanetResizeTemp = TextureRegion.split(directory.getEntry( "prototype:planet_resize_purple", Texture.class ), 1000, 1000);
        purplePlanetResizeTextures = new TextureRegion[8];
        purplePlanetResizeTexturesReverse = new TextureRegion[8];

        index = 0;
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 8; j++) {
                purplePlanetResizeTextures[index++] = purplePlanetResizeTemp[i][j];
                purplePlanetResizeTexturesReverse[purplePlanetResizeTextures.length - index] = purplePlanetResizeTemp[i][j];
            }
        }

        TextureRegion[][] sunPlanetResizeTemp = TextureRegion.split(directory.getEntry( "prototype:planet_resize_sun", Texture.class ), 1000, 1000);
        sunPlanetResizeTextures = new TextureRegion[8];
        sunPlanetResizeTexturesReverse = new TextureRegion[8];

        index = 0;
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 8; j++) {
                sunPlanetResizeTextures[index++] = sunPlanetResizeTemp[i][j];
                sunPlanetResizeTexturesReverse[sunPlanetResizeTextures.length - index] = sunPlanetResizeTemp[i][j];
            }
        }

        TextureRegion[][] sunPlanetTemp = TextureRegion.split(directory.getEntry( "prototype:sun_planet", Texture.class ), 1000, 1000);
        sunPlanetTextures = new TextureRegion[14];

        index = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 5; j++) {
                if (!(i == 2 && j == 4)) {
                    sunPlanetTextures[index++] = sunPlanetTemp[i][j];
                }
            }
        }

        TextureRegion[][] solarFlareTemp = TextureRegion.split(directory.getEntry( "prototype:solar_flare", Texture.class ), 500, 500);
        solarFlareTextures = new TextureRegion[10];

        index = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 5; j++) {
                solarFlareTextures[index++] = solarFlareTemp[i][j];
            }
        }

        TextureRegion[][] yellowPlanetResizeTemp = TextureRegion.split(directory.getEntry( "prototype:planet_resize_yellow", Texture.class ), 1000, 1000);
        yellowPlanetResizeTextures = new TextureRegion[8];
        yellowPlanetResizeTexturesReverse = new TextureRegion[8];

        index = 0;
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 8; j++) {
                yellowPlanetResizeTextures[index++] = yellowPlanetResizeTemp[i][j];
                yellowPlanetResizeTexturesReverse[yellowPlanetResizeTextures.length - index] = yellowPlanetResizeTemp[i][j];

            }
        }

        volumes[0] = 1;
        volumes[1] = 1;
        volumes[2] = 1;

        // add loop sound
        easyIntro= directory.getEntry( "rocket:intro", Music.class );
        easyLoop= directory.getEntry( "rocket:loop", Music.class );
        enemyMusic= directory.getEntry( "rocket:enemyMusic", Music.class );
        shipExplodeSound = directory.getEntry( "rocket:shipExplode", Music.class );
        musics[0] = easyIntro;
        musics[1] = easyLoop;
        musics[2] = enemyMusic;
        musics[3] = shipExplodeSound;

        decreaseSize = directory.getEntry( "rocket:decreaseSize", Sound.class );
        increaseSize = directory.getEntry( "rocket:increaseSize", Sound.class );
        decelerate = directory.getEntry( "rocket:decelerate", Sound.class );
        accelerate = directory.getEntry( "rocket:accelerate", Sound.class );
        click = directory.getEntry( "rocket:click", Sound.class );
        exit = directory.getEntry( "rocket:exit", Sound.class );
        checkSound = directory.getEntry( "rocket:checkpoint", Sound.class );
        enter = directory.getEntry( "rocket:enter", Sound.class );
        collisionSound = directory.getEntry( "rocket:collision", Sound.class );
        starCollectSound = directory.getEntry( "rocket:starCollect", Sound.class );
        alertSound = directory.getEntry( "rocket:alert", Sound.class );
        shockwaveSound = directory.getEntry( "rocket:shockwave", Sound.class );
        shipExplodeSound = directory.getEntry( "rocket:shipExplode", Music.class );
        sounds[0] = decreaseSize;
        sounds[1] = increaseSize;
        sounds[2] = decelerate;
        sounds[3] = accelerate;
        sounds[4] = click;
        sounds[5] = exit;
        sounds[6] = checkSound;
        sounds[7] = enter;
        sounds[8] = collisionSound;
        sounds[9] = starCollectSound;
        sounds[10] = shockwaveSound;
        sounds[11] = alertSound;

        starIcon = new TextureRegion(directory.getEntry("prototype:staricon", Texture.class));

        pixel = new TextureRegion(directory.getEntry( "prototype:pixel", Texture.class ));
        debugPlanet = new TextureRegion(directory.getEntry( "prototype:debug_planet", Texture.class ));

        ship1Animation = new Animation(1f/8f, ship1Textures);
        ship2Animation = new Animation(1f/8f, ship1Textures);
        ship3Animation = new Animation(1f/8f, ship1Textures);
        cometAnimation = new Animation(1f/8f, cometTextures);
        debrisAnimation = new Animation(1f/3f, debrisTextures);
        checkpointAnimation = new Animation(1f/8f, checkpointTextures);
        checkpointCollectedAnimation = new Animation(1f/8f, checkpointCollectedTextures);
        starLoopAnimation = new Animation(1f/8f, starLoopTextures);
        starCollectAnimation = new Animation(1f/8f, starCollectTextures);
        enemyExplodeAnimation = new Animation(1f/6f, enemyExplodeTextures);
        whiteholeStartAnimation = new Animation(1f/8f, whiteholeStartTextures);
        whiteholeLoopAnimation = new Animation(1f/8f, whiteholeLoopTextures);
        blackholeStartAnimation = new Animation(1f/8f, blackholeStartTextures);
        blackholeLoopAnimation = new Animation(1f/8f, blackholeLoopTextures);
        shipHurtAnimation = new Animation(1f/8f, shipHurtTextures);
        bluePlanetResizeAnimation = new Animation(1f/20f, bluePlanetResizeTextures);
        greenPlanetResizeAnimation = new Animation(1f/20f, greenPlanetResizeTextures);
        pinkPlanetResizeAnimation = new Animation(1f/20f, pinkPlanetResizeTextures);
        purplePlanetResizeAnimation = new Animation(1f/20f, purplePlanetResizeTextures);
        sunPlanetResizeAnimation = new Animation(1f/20f, sunPlanetResizeTextures);
        sunPlanetAnimation = new Animation(1f/8f, sunPlanetTextures);
        yellowPlanetResizeAnimation = new Animation(1f/20f, yellowPlanetResizeTextures);
        bluePlanetResizeAnimationReverse = new Animation(1f/20f, bluePlanetResizeTexturesReverse);
        greenPlanetResizeAnimationReverse = new Animation(1f/20f, greenPlanetResizeTexturesReverse);
        pinkPlanetResizeAnimationReverse = new Animation(1f/20f, pinkPlanetResizeTexturesReverse);
        purplePlanetResizeAnimationReverse = new Animation(1f/20f, purplePlanetResizeTexturesReverse);
        sunPlanetResizeAnimationReverse = new Animation(1f/20f, sunPlanetResizeTexturesReverse);
        yellowPlanetResizeAnimationReverse = new Animation(1f/20f, yellowPlanetResizeTexturesReverse);
        solarFlareAnimation = new Animation(1f/12f, solarFlareTextures);
        solarFlareAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }

    public static void updateVolume(int n, float v) {
        volumes[n] = v;

        if (n != 2) {
            for (Music m : musics) {
                m.setVolume(volumes[0]*volumes[1]);
            }
        }
    }

}
