package com.mygdx.technicalprototype.physics.rocket;

import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.technicalprototype.assets.AssetDirectory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.technicalprototype.physics.GameMode;
import com.mygdx.technicalprototype.GameObject;
import com.mygdx.technicalprototype.Rocket;

/**
 * Gameplay specific controller for gameplay prototype.
 */
public class RocketController extends GameMode implements ContactListener {

	/** Texture assets for the rocket */
	private TextureRegion rocketTexture;
	/** The texture asset for the background */
	private TextureRegion backgroundTexture;

	/** Object for the rocket */
	private Rocket ship;

	/** Physics constants for initialization */
	private JsonValue constants;

	/**
	 * Creates and initialize a new instance of the rocket lander game
	 *
	 * The game has default gravity and other settings
	 */
	public RocketController() {
		setDebug(false);
		setComplete(false);
		setFailure(false);
		world.setContactListener(this);
	}

	/**
	 * Gather the assets for this controller.
	 *
	 * This method extracts the asset variables from the given asset directory. It
	 * should only be called after the asset directory is completed.
	 *
	 * @param directory	Reference to global asset manager.
	 */
	public void gatherAssets(AssetDirectory directory) {
		constants  = directory.getEntry( "rocket:constants", JsonValue.class );
		rocketTexture = new TextureRegion( directory.getEntry( "prototype:ship", Texture.class ) );
		backgroundTexture = new TextureRegion(directory.getEntry( "prototype:background", Texture.class ));

		super.gatherAssets(directory);
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		Vector2 gravity = new Vector2(world.getGravity() );

		for(GameObject obj : objects) {
			obj.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();

		world = new World(gravity,false);
		world.setContactListener(this);
		setComplete(false);
		setFailure(false);
		populateLevel();
	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {
		// Create the rocket avatar
		float dwidth  = rocketTexture.getRegionWidth()/scale.x;
		float dheight = rocketTexture.getRegionHeight()/scale.y;

//		JsonValue rockjv = constants.get("rocket");

		Vector2 pos = new Vector2(0, 0);

		JsonValue defaults = constants.get("defaults");
	}

	/**
	 * The core gameplay loop of this world.
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class WorldController.
	 * This method is called after input is read, but before collisions are resolved.
	 * The very last thing that it should do is apply forces to the appropriate objects.
	 *
	 * @param dt	Number of seconds since last animation frame
	 */
	public void update(float dt) {
		return;
	}

	public void draw(float dt){
		canvas.clear();

		// Draw background unscaled.
		canvas.begin();
		canvas.draw(backgroundTexture, Color.WHITE, 0, 0,canvas.getWidth(),canvas.getHeight());
		canvas.end();

		canvas.begin();
		for(GameObject obj : objects) {
			obj.draw(canvas);
		}
		canvas.end();

		if (isDebug()) {
			canvas.beginDebug();
			for(GameObject obj : objects) {
				obj.drawDebug(canvas);
			}
			canvas.endDebug();
		}
	}

	/**
	 * Updates that animation for a single burner
	 *
	 * This method is here instead of the the rocket model because of our philosophy
	 * that models should always be lightweight.  Animation includes sounds and other
	 * assets that we do not want to process in the model
	 *
	 * @param  burner   The rocket burner to animate
	 * @param  on       Whether to turn the animation on or off
	 */
	private void updateBurner(RocketModel.Burner burner, boolean on) {
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
	}

	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when two objects cease to touch.  We do not use it.
	 */
	public void endContact(Contact contact) {}

	private final Vector2 cache = new Vector2();

	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}

	/**
	 * Handles any modifications necessary before collision resolution
	 *
	 * This method is called just before Box2D resolves a collision.  We use this method
	 * to implement sound on contact, using the algorithms outlined similar to those in
	 * Ian Parberry's "Introduction to Game Physics with Box2D".
	 *
	 * However, we cannot use the proper algorithms, because LibGDX does not implement
	 * b2GetPointStates from Box2D.  The danger with our approximation is that we may
	 * get a collision over multiple frames (instead of detecting the first frame), and
	 * so play a sound repeatedly.  Fortunately, the cooldown hack in SoundController
	 * prevents this from happening.
	 *
	 * @param  contact  	The two bodies that collided
	 * @param  oldManifold  The collision manifold before contact
	 */
	public void preSolve(Contact contact, Manifold oldManifold) {
	}

	/**
	 * Plays a bump sound ensuring we never exceed MAX_BUMPS.
	 *
	 * The longest playing sound is evicted if necessary. To see why this
	 * is needed, make MAX_BUMPS large and watch it blow your speakers.
	 */
	public void playBump() {
	}

	/**
	 * Called when the Screen is paused.
	 *
	 * We need this method to stop all sounds when we pause.
	 * Pausing happens when we switch game modes.
	 */
	public void pause() {
	}
}