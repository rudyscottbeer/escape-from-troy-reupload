package com.mygdx.technicalprototype;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.technicalprototype.physics.obstacle.ComplexObstacle;
import com.mygdx.technicalprototype.physics.obstacle.WheelObstacle;
/**
 * A class representing an obstacle that moves along a path.
 *
 * This obstacle cannot be resized. It is a kinematic body type.
 *
 * With help from:
 * https://gamedev.stackexchange.com/questions/162163/how-to-set-a-box2d-body-to-follow-a-strict-path
 */
public class DynamicObstacle extends WheelObstacle {
    /** World position in Box2D coords, as opposed to screen position */
    private Vector2 worldPosition;
    /** The path the dynamic obstacle follows */
    private CatmullRomSpline path;
    /** How many seconds it takes to traverse this obstacle's path */
    private float period;
    /** How much time has elapsed since this dynamic obstacle started its path */
    private float time;
    /** Whether to stop dynamic obstacles from moving */
    private boolean stopTime;

    private Vector2 targetPosition;
    private ShapeRenderer shapeRenderer;

    /** Constructor for a dynamic obstacle
     * @param pos the obstacle's position
     * @param radius radius of wheel
     * @param path the path the obstacle follows
     * @param period the distance that the
     */
    public DynamicObstacle(Vector2 pos, float radius, Vector2[] path, float period) {
        super(pos.x, pos.y, radius/2.3f);
        worldPosition = new Vector2(pos.x, pos.y);
        this.setBodyType(BodyDef.BodyType.KinematicBody);
        this.path = new CatmullRomSpline<>(path, true);
        this.period = period;
        this.targetPosition = new Vector2();
        shapeRenderer = new ShapeRenderer();
        stopTime = false;
//        bodies.add(new WheelObstacle(pos.x, pos.y, radius));
    }

    public void setPath(Vector2[] path){
        this.path.set(path, true);

    }

    public float getMass() {
        return 0;
    }

    public void setStopTime(boolean stopTime) {
        this.stopTime = stopTime;
    }

//    @Override
//    protected boolean createJoints(World world) {
//        return false;
//    }

    /**
     * Calculates the dynamic obstacle's position and moves it
     * @param dt how much time has passed since the last frame
     */
    public void move(float dt) {
        worldPosition = targetPosition;
        time += dt;
        float f = time / period;
        if (stopTime) {
            body.setLinearVelocity(new Vector2(0,0));
        } else if (f <= 1.0f) {
            Vector2 bodyPosition = this.body.getWorldCenter();
            path.valueAt(targetPosition, f);
            Vector2 positionDelta = (new Vector2(targetPosition)).sub(bodyPosition);
            body.setLinearVelocity(positionDelta.scl(10));
        } else {
            time = 0;
        }
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
     * Draws the resizable object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        // hardcoded radius to scale
        float scale = 18 * super.getRadius() * drawScale.x / this.getTexture().getRegionWidth();
        canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX()*drawScale.x,
                getY()*drawScale.y, 0.0f, scale, scale);
    }

    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        int k = 100;
        Vector2[] points = new Vector2[k];
        for(int i = 0; i < k; ++i)
        {
            points[i] = new Vector2();
            path.valueAt(points[i], ((float)i)/((float)k-1));
        }

//        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for(int i = 0; i < k-1; ++i)
        {
            shapeRenderer.line(points[i], points[i+1]);
        }

        shapeRenderer.circle(targetPosition.x, targetPosition.y, 1.0f);
        shapeRenderer.end();
    }
}
