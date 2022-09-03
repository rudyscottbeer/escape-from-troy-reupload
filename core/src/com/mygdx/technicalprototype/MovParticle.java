/*
 * BubbleGenerator.java
 *
 * This object is only a body.  It does not have a fixture and does not collide.
 * It is a physics object so that we can weld it to the ragdoll mask.  That
 * way it always looks like bubles are coming from the snorkle, no matter
 * which way the head moves.
 *
 * This is another example of a particle system.  Like the photons in the first lab,
 * it preallocates all of its objects ahead of time.  However, this time we use
 * the built-in memory pool from LibGDX to do it.
 * 
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package com.mygdx.technicalprototype;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;
import com.mygdx.technicalprototype.physics.obstacle.WheelObstacle;

import java.util.Random;

/**
 * Physics object that generates non-physics bubble shapes.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class MovParticle extends WheelObstacle {
	/** World position in Box2D coords, as opposed to screen position */
	private Vector2 worldPosition;

	/** Representation of the bubbles for drawing purposes. */
	private class Particle implements Pool.Poolable {
		/** Position of the bubble in Box2d space */
		public Vector2 position;
		/** The number of animation frames left to live */
		public int life;
		public float angle;

		/** Creates a new Particle with no lifespace */
		public Particle() {
			Random r = new Random();
			position = new Vector2();
			angle = (float)(Math.PI/6*r.nextGaussian());
			life = -1;
		}

		/** Resets the particle so it can be reclaimed by the pool */
		public void reset() {
			position.set(0,0);
			life = -1;
		}
	}

	/**
	 * Memory pool supporting the particle system.
	 *
	 * This pool preallocates all of the particles.  When a particle dies, it is
	 * released back to the pool for reuse.
	 */
	private class ParticlePool extends Pool<Particle> {
		/**
		 * That is all we got
		 */
		private static final int MAX_PARTICLES = 3;
		/**
		 * The backing list of particles
		 */
		private Particle[] particles;
		/**
		 * The current allocation position in the array
		 */
		private int offset;

		/**
		 * Creates a new pool to allocate Particles
		 * <p>
		 * This constructor preallocates the objects
		 */
		public ParticlePool() {
			super();
			particles = new Particle[MAX_PARTICLES];
			for (int ii = 0; ii < MAX_PARTICLES; ii++) {
				particles[ii] = new Particle();
			}
			offset = 0;
		}

		/**
		 * Returns the backing list (so that we can iterate over it)
		 *
		 * @return the backing list
		 */
		public Particle[] getPool() {
			return particles;
		}

		/**
		 * Returns the next available object in the backing list
		 * <p>
		 * If the backing list is exhausted, we return null
		 *
		 * @return the next available object in the backing list
		 */
		protected Particle newObject() {
			if (offset < particles.length) {
				offset++;
				return particles[offset - 1];
			}
			return null;  // OUT OF MEMORY
		}
	}

	/** The initializing data (to avoid magic numbers) */
	private final JsonValue data;

	// Dimensional information for drawing texture.
	/** The size dimension of a bubble */
	private Vector2 dimension;

	/** How long bubbles live after creation */
	private int lifespan;
	/** The maximum time beteween bubbles */
	private final int timelimit;
	/** How long until we can make another bubble */
	private int cooldown;
	/** Whether or not we made a bubble this animation frame */
	private boolean bubbled;
	private float size;
	private float angle;
	private boolean isMoving = true;

	/** Cache to safely return dimension information */
	protected Vector2 sizeCache = new Vector2();
	/** Memory pool to allocate new particles */
	private final ParticlePool memory;

	/**
	 * Returns the dimensions of this box
	 *
	 * This method does NOT return a reference to the dimension vector. Changes to this
	 * vector will not affect the shape.  However, it returns the same vector each time
	 * its is called, and so cannot be used as an allocator.
	 *
	 * @return the dimensions of this box
	 */
	public Vector2 getDimension() {
		return sizeCache.set(dimension);
	}

	/**
	 * Sets the dimensions of this box
	 *
	 * This method does not keep a reference to the parameter.
	 *
	 * @param value  the dimensions of this box
	 */
	public void setDimension(Vector2 value) {
		dimension.set(value);
	}

	/**
	 * Sets the dimensions of this box
	 *
	 * @param width   The width of this box
	 * @param height  The height of this box
	 */
	public void setDimension(float width, float height) {
		dimension.set(width,height);
	}

	/**
	 * Returns the box width
	 *
	 * @return the box width
	 */
	public float getWidth() {
		return dimension.x;
	}

	/**
	 * Sets the box width
	 *
	 * @param value  the box width
	 */
	public void setWidth(float value) {
		sizeCache.set(value,dimension.y);
		setDimension(sizeCache);
	}

	/**
	 * Returns the box height
	 *
	 * @return the box height
	 */
	public float getHeight() {
		return dimension.y;
	}

	/**
	 * Sets the box height
	 *
	 * @param value  the box height
	 */
	public void setHeight(float value) {
		sizeCache.set(dimension.x,value);
		setDimension(sizeCache);
	}

	public void setAngle(float value) {
		angle = value;
	}

	public void setMoving(boolean value) {
		isMoving = value;
	}

	/**
	 * Returns the lifespan of a generated bubble.
	 *
	 * @return the lifespan of a generated bubble.
	 */
	public int getLifeSpan() {
		return lifespan;
	}

	/**
	 * Sets the lifespan of a generated bubble.
	 *
	 * Changing this does not effect bubbles already generated.
	 *
	 * @param value the lifespan of a generated bubble.
	 */
	public void setLifeSpan(int value) {
		lifespan = value;
	}


	/**
	 * Creates a new bubble generator with the given physics data
	 *
	 * @param data  	The physics constants for this bubbler
	 * @param x  		The x-coordinate of the parent
	 * @param y  		The x-coordinate of the parent
	 */
	public MovParticle(Vector2 pos, JsonValue data, float x, float y) {
		super(x, y, data.getFloat("size",0));
		size = data.getFloat("size",0);
		worldPosition = new Vector2(pos.x, pos.y);

		setName("movParticle");
		this.data = data;

		// Initialize
		lifespan = data.getInt("lifespan",0);
		timelimit = data.getInt("cooldown",0);
		cooldown = 0;
		bubbled = false;
		memory = new ParticlePool();
	}

	/** Generates a new bubble object and put it on the screen. */
	public void bubble(Vector2 startPos) {
		Particle p = memory.obtain();
		if (p != null) {
			p.position.set(startPos);
			p.life = lifespan;
		}
	}
	
    /**
     * Returns true if we generated a bubble this animation frame.
     *
     * @return true if we generated a bubble this animation frame.
     */
    public boolean didBubble() { 
    	return bubbled; 
    }

	/**
	 * Updates the object's physics state (NOT GAME LOGIC).
	 *
	 * We use this method for cooldowns and bubble movement.
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void update(float dt, Vector2 startPos) {
		for(Particle p : memory.getPool()) {
			if (p.life > 0) {
				p.position.x -= (Math.cos(p.angle+angle));
				p.position.y += (Math.sin(p.angle+angle));
				p.life -= 1;
				if (p.life == 0) {
					memory.free(p);
				}
			}
		}

		if (cooldown == 0) {
	        bubbled = true;
			bubble(startPos);
			cooldown = timelimit;
		} else if (cooldown > 0) {
	        bubbled = false;
			cooldown--;
		}
		super.update(dt);
	}

	/**
	 * Sets the obstacle's position relative to the ship
	 * @param canvas the game canvas
	 * @param shipPos the ship's position
	 */
	public void setLocation(GameCanvas canvas, Vector2 shipPos) {
		super.setPosition(((canvas.getWidth() / 2f) - shipPos.x) / drawScale.x + worldPosition.x,
				((canvas.getHeight() / 2f) - shipPos.y) / drawScale.y + worldPosition.y);
	}

	/**
	 * Draws the physics object.
	 *
	 * @param canvas Drawing context
	 */
	public void draw(GameCanvas canvas, Vector2 startPos) {
		if (texture == null) {
			return;
		}

		for(Particle p : memory.getPool()) {
			float a = (float)p.life / (float)lifespan;
			Color c = new Color(1,1,1,a);
			if (p.life > 0) {
				canvas.draw(texture,c,origin.x,origin.y,
							p.position.x + ((canvas.getWidth() / 2)  - startPos.x),p.position.y + ((canvas.getHeight() / 2) - startPos.y),0.0f,size,size);
			}
		}
	}
}
