package com.mygdx.technicalprototype;
import com.badlogic.gdx.utils.JsonValue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.technicalprototype.physics.obstacle.WheelObstacle;

public class Rocket extends WheelObstacle {

    /** Position of the ship */
    private Vector2 pos;
    /** Velocity of the ship */
    private Vector2 vel;
    /** The angle of the ship for drawing */
    private float drawAngle;
    /** Linear velocity of the ship */
    private float linearVel;
    /** Texture asset for rocket */
    private TextureRegion ship1Texture;
    private TextureRegion ship2Texture;
    private TextureRegion ship3Texture;

    /** Health of ship */
    private float health;

    private GameCanvas canvas;
    /** If the rocket is invincible after being hurt */
    boolean invincible;

    private Sprinkler sprinkler;

    /**
     * Initializes the variables
     */

    public Rocket(Vector2 pos, float radius, TextureRegion ship1Texture, TextureRegion ship2Texture, TextureRegion ship3Texture, JsonValue data, Vector2 scale, float health){

        super(Gdx.graphics.getWidth() / 2 / scale.x,Gdx.graphics.getHeight() / 2 / scale.y, radius);

        this.pos = pos;
        vel = new Vector2(0, 0);
        linearVel = 0;
        this.ship1Texture = ship1Texture;
        this.ship2Texture = ship2Texture;
        this.ship3Texture = ship3Texture;
        float ox = Gdx.graphics.getWidth() / 2 / scale.x;
        float oy = Gdx.graphics.getHeight() / 2 / scale.y;
        sprinkler = new Sprinkler(data.get("particles"),ox,oy);
        sprinkler.setDrawScale(scale);
        this.health = health;
    }

    /**
     * Returns the bubble generator welded to the mask
     *
     * @return the bubble generator welded to the mask
     */
    public Sprinkler getSprinkler() {
        return sprinkler;
    }

    /** */
    public float getDrawAngle() {
        return drawAngle;
    }

    /**
     * Getter for position of ship
     * @return Vector2 position of ship
     */
    public Vector2 getPos() {
        return new Vector2(pos);
    }

    /**
     * Updates the ship location in the world
     *
     */
    public void setPos(Vector2 position) {
        pos = position;
    }

    public boolean getInvincible() {
        return invincible;
    }

    public void setInvincible(boolean b) {
        invincible = b;
    }

    /**
     * Adds the ship location in the world
     *
     */
    public void addPos(Vector2 position) {
        pos.add(position);
    }

    /**
     * Getter for velocity of ship
     * @return Vector2 velocity of ship
     */
    public Vector2 getVelocity() {
        return vel;
    }

    /**
     * Setter for velocity of ship
     * @param vel Vector2 velocity of ship
     */
    public void setVelocity(Vector2 vel) {
        this.vel = vel;
    }

    /**
     * Getter for linear velocity of ship
     * @return float velocity of ship
     */
    public float getLinearVelocity() {
        return linearVel;
    }

    /**
     * Setter for linear velocity of ship
     */
    public void setLinearVelocity(float linearVel) {
        this.linearVel = linearVel;
    }

    /**
     * Adder for linear velocity of ship
     * @param linearVel float velocity of ship
     */
    public void addLinearVelocity(float linearVel) {
        this.linearVel += linearVel;
    }

    /**
     * Getter for mass of ship
     * @return float mass of ship
     */
    public float getMass() {
        return 5.0f;
    }

    /**
     * Setter for health of ship
     * @param health float health of ship
     */
    public void setHealth(float health){this.health=health;}

    /**
     * Getter for health of ship
     * @return float health of ship
     */
    public float getHealth(){return health;}

    public void draw(GameCanvas canvas) {
        // Draw the ship
        float scale = 20 * getRadius() * drawScale.x / this.getTexture().getRegionWidth();
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        if (vel.x > 0) {
            drawAngle = (float)(Math.atan(vel.y / vel.x));
        } else {
            drawAngle = (float)(Math.atan(vel.y / vel.x) + Math.PI);
        }
        sprinkler.draw(canvas, pos);
        if (getHealth() == 1) {
            canvas.draw(ship1Texture, Color.WHITE, origin.x, origin.y, canvasWidth / 2,
                    canvasHeight / 2, drawAngle, scale, scale);
        } else if (getHealth() < 1 && getHealth() > 0.5) {
            canvas.draw(ship2Texture, Color.WHITE, origin.x, origin.y, canvasWidth / 2,
                    canvasHeight / 2, drawAngle, scale, scale);
        } else {
            canvas.draw(ship3Texture, Color.WHITE, origin.x, origin.y, canvasWidth / 2,
                    canvasHeight / 2, drawAngle, scale, scale);
        }
    }
}
