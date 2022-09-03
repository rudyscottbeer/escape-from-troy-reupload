package com.mygdx.technicalprototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.technicalprototype.physics.obstacle.WheelObstacle;

import java.util.LinkedList;
import java.util.Queue;

public class Enemy extends WheelObstacle {
    private float maxSpeed = 5.0f;
    private final float HANDLING = 2.0f; // exceeding 2.0 causes movement bugs
    private final float MAX_ACCELERATION = HANDLING * 25;

    private final float MASS = 2.0f;
    private final float VISION = 6;
    private final float WAYPOINT_HIT = 0.75f; // if the ship gets caught in a loop, increase by 0.1
    private final Vector2 CENTER_SCREEN;
    private final Vector2 SHIP_START;

    private Vector2 previousShipPos;

    /** World position, as opposed to screen position */
    private Vector2 worldPosition;
    /** The angle the enemy is facing */
    private float angle = 0.0f;
    /** The angle the enemy is facing */
    private Vector2 dir = new Vector2();
    /** The path that the enemy patrols when searching for the ship */
    private Queue<Vector2> patrolPath;
    private boolean patroller = true;

    /** The current target destination of the enemy */
    private Vector2 destination;
    /** The current state of the ship */
    private State state;
    private boolean isGravitating;

    /** Whether to clamp the velocity */
    private boolean clampVelocity;

    private boolean explode;

    private Vector2 force = new Vector2();

    public Enemy(Vector2 pos, Vector2[] patrol, Vector2 shipPos, Vector2 scale, float speed) {
        this(pos, patrol, shipPos, scale);
        maxSpeed = speed;
    }

    public Enemy(Vector2 pos, Vector2[] patrol, Vector2 shipPos, Vector2 scale) {
        super((Gdx.graphics.getWidth() / 2) - shipPos.x + pos.x,
                (Gdx.graphics.getHeight() / 2) - shipPos.y + pos.y, 0.75f);
        setDrawScale(scale);
        setMass(MASS);
        setPath(patrol);

        CENTER_SCREEN = new Vector2((Gdx.graphics.getWidth() / 2) / drawScale.x, (Gdx.graphics.getHeight() / 2) / drawScale.y);
        SHIP_START = shipPos;
        previousShipPos = shipPos;
        worldPosition = new Vector2(pos.x, pos.y);
        clampVelocity = true;

        if (patroller)
            setState(State.ATTACK, null);
//            setState(State.PATROL, SHIP_START);
        else
            setState(State.ATTACK, null);
    }

    public void setPath(Vector2[] patrol){
        if (patrol != null || patrol.length > 0) {
            patrolPath = new LinkedList<>();
            for (Vector2 p : patrol) {
                patrolPath.add(p);
            }
            patroller = true;
        } else {
            patroller = false;
        }
    }


    public void setLocation(Vector2 shipPos) {
        super.setPosition(((Gdx.graphics.getWidth() / 2) - shipPos.x) / drawScale.x + worldPosition.x,
                ((Gdx.graphics.getHeight() / 2) - shipPos.y) / drawScale.y + worldPosition.y);
    }

    public State getState() {
        return state;
    }

    public boolean getExplode() {
        return explode;
    }

    public void setExplode(boolean b){
        explode = b;
    }

    public boolean isPatroller() {
        return patroller;
    }

    public void setGravitating(boolean value) {
        isGravitating = value;
    }

    public boolean isGravitating() {
        return isGravitating;
    }

    public boolean detectsShip(Vector2 shipPos) {
        float dst = shipPos.dst((new Vector2(getPosition().x-((Gdx.graphics.getWidth()/2)-shipPos.x)/drawScale.x,getPosition().y-((Gdx.graphics.getHeight()/2)-shipPos.y)/drawScale.y)).scl(drawScale))/drawScale.len();
        return (dst < VISION);
    }

    public void setDestination(Vector2 dest) {
        destination = dest;
    }

    public void setState(State s, Vector2 shipPos) {
        state = s;
        if (state == State.ATTACK) {
            destination = CENTER_SCREEN;
        } else if (state == State.PATROL) {
            destination = nextPatrol(shipPos);
        }
    }

    public void setClampVelocity(boolean clampVelocity) {
        this.clampVelocity = clampVelocity;
    }

    public void setDir(Vector2 dir) {
        this.dir.x = dir.x;
        this.dir.y = dir.y;
    }

    public void setForce(Vector2 force) {
        this.force.x = force.x;
        this.force.y = force.y;
    }

    public Vector2 nextPatrol(Vector2 shipPos) {
        if (patrolPath.size() <= 0) {
            return destination;
        }
        Vector2 next = patrolPath.remove();
        patrolPath.add(next);
        next = new Vector2(next);
        Vector2 offset = new Vector2((SHIP_START.x-shipPos.x)/drawScale.x, (SHIP_START.y-shipPos.y)/drawScale.y);
        return next.add(offset);
    }

    public void nextMove(Vector2 shipPos) {
        worldPosition = new Vector2(getPosition().x-((Gdx.graphics.getWidth()/2)-shipPos.x)/drawScale.x,getPosition().y-((Gdx.graphics.getHeight()/2)-shipPos.y)/drawScale.y);

        if (state == State.PATROL && (destination.dst(getPosition()) < WAYPOINT_HIT)) {
            destination = nextPatrol(shipPos);
        } else if (state == State.PATROL) {
            Vector2 offset = new Vector2((previousShipPos.x-shipPos.x)/drawScale.x, (previousShipPos.y-shipPos.y)/drawScale.y);
            previousShipPos = shipPos;
            destination.add(offset);
        }

        dir = new Vector2(destination.x-getPosition().x,destination.y-getPosition().y);
        angle = dir.angleRad();
        if (dir.len() > 2) {
            dir.sub(getVelocity().nor().scl(HANDLING));
        }
        dir.nor();

        if (clampVelocity) {
            body.setLinearVelocity(body.getLinearVelocity().nor().scl(maxSpeed));
            body.applyForce(dir.scl(MAX_ACCELERATION), getPosition(),true);
        } else {
            body.applyLinearImpulse(force, getPosition(),true);
        }
    }

    @Override
    public void draw(GameCanvas canvas) {
        // Draw the ship
        float scale = 2 * super.getRadius()
                * drawScale.x /
                this.getTexture().getRegionWidth();
        canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX()*drawScale.x,
                getY()*drawScale.y, angle-(float)(Math.PI/2), scale, scale);
    }

    enum State {
        ATTACK, ESCAPE, PATROL, STUN
    }
}
