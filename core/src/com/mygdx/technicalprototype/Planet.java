package com.mygdx.technicalprototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.technicalprototype.physics.obstacle.WheelObstacle;
import com.mygdx.technicalprototype.util.Helper;

public class Planet extends WheelObstacle {
    /** World position in Box2d units, as opposed to screen position */
    private Vector2 worldPosition;

    /** The minimum planet radius size in box2d units */

    public int MAX_INCREASE;
    public int MAX_DECREASE;
    public int currentSize;
    private static final float FLARE_SCALE = 2;


    private boolean isGrowing;
    private float radiusAfterChange;
    private PlanetColor color;
    private StaticObstacle solarFlare;
    private float solarFlareAngle;
    private float solarFlareDistance;


    enum PlanetColor {
        BLUE,
        GREEN,
        PINK,
        PURPLE,
        SUN,
        YELLOW
    }

    public Planet(Vector2 pos, float radius, Vector2 shipPos, PlanetColor c, int max_size, int min_size, int degrees) {
        super(((Gdx.graphics.getWidth() / 2) - shipPos.x) + pos.x,
                ((Gdx.graphics.getHeight() / 2) - shipPos.y) + pos.y, radius);
        worldPosition = new Vector2(pos.x, pos.y);
        solarFlare = new StaticObstacle(radius / FLARE_SCALE);
        color = c;
        solarFlareAngle = (float)(Math.PI / 180 * degrees);
        radiusAfterChange = radius;
        currentSize = 0;
        MAX_INCREASE = max_size;
        MAX_DECREASE = min_size;
    }

    public PlanetColor getColor() {
        return color;
    }

    public boolean changeSize(int change) {
        if (currentSize + change > MAX_INCREASE || currentSize + change < MAX_DECREASE) {
            return false;
        } else {
            currentSize += change;
            return true;
        }
    }

    public StaticObstacle getSolarFlare() { return solarFlare; }

    public Vector2 getWorldPosition() {
        return new Vector2(worldPosition);
    }

    public float getMass() {
        return (float)(1000 + Math.pow(getRadius(), 1.2));
    }

    /** Returns the radius of the planet in pixel units
     *
     * @return the radius of the planet */
    public float getRadius() {
        return super.getRadius() * drawScale.x;
    }

    public void setRadius(float newRadius) {
        super.setRadius(newRadius);
    }

    /** Returns the radius before change of the planet in pixel units
     *
     * @return the radius of the planet */
    public float getRadiusAfterChange() {
        return radiusAfterChange * drawScale.x;
    }

    public void setRadiusAfterChange(float f) {
        radiusAfterChange = f;
    }

    public Vector2 getDrawScale() { return drawScale; }

    public void setTexture(TextureRegion t) { texture = t; }

    /**
     * Updates the planet location relative to ship
     *
     */
    public void setLocation(GameCanvas canvas, Vector2 shipPos) {
        super.setPosition(((canvas.getWidth() / 2) - shipPos.x) / drawScale.x + worldPosition.x,
                ((canvas.getHeight() / 2) - shipPos.y) / drawScale.y + worldPosition.y);
        solarFlare.setPosition((float) (((canvas.getWidth() / 2) - shipPos.x) / drawScale.x + worldPosition.x + (super.getRadius() + solarFlareDistance) * Math.sin(solarFlareAngle)),
                (float) (((canvas.getHeight() / 2) - shipPos.y) / drawScale.y + worldPosition.y + (super.getRadius() + solarFlareDistance) * Math.cos(solarFlareAngle)));
    }

    public void setSolarFlareDistance(float index) {
//        System.out.println(percent);
//        if (percent < 1.0f) {
            solarFlareDistance = super.getRadius() * index / FLARE_SCALE;
//        } else {
//            solarFlareDistance = 0;
//        }
    }

    public Vector2 getPos() {
        return Helper.boxCoordsToWorldCoords(drawScale, worldPosition);
    }

    public void setGrowing(boolean b) {
        super.setAnimating(b);
        isGrowing = b;
    }

    public void setShrinking(boolean b) {
        super.setAnimating(b);
        isGrowing = false;
    }

    public boolean isGrowing() {
        return isGrowing;
    }

    public boolean isShrinking() {
        return !isGrowing && super.isAnimating();
    }

    /**
     * Resize the planet by change
     *
     * @param change the amount to change the planet size
     */
    public void resize(float change, Rocket ship) {
        float newRadius = super.getRadius() + change;
        this.setRadius(newRadius);

        solarFlare.setRadius(newRadius / FLARE_SCALE);
    }

    /**
     * Draws the resizable object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas, float offset, Color c) {
        if (collided) {
            c = Color.BLACK;
        }
        float scale = 3f * super.getRadius() * drawScale.x / this.getTexture().getRegionWidth();
        canvas.draw(texture, c, origin.x, origin.y, getX()*drawScale.x - offset,
                getY()*drawScale.y - offset, 0.0f, scale, scale);
    }

    public void draw(GameCanvas canvas, float offset, TextureRegion flare) {
        // hardcoded radius to scale
        Color c = Color.WHITE;
        if (collided) {
            c = Color.BLACK;
        }
        float scale = 3f * super.getRadius() * drawScale.x / this.getTexture().getRegionWidth();
        canvas.draw(texture, c, origin.x, origin.y, getX()*drawScale.x - offset,
                getY()*drawScale.y - offset, 0.0f, scale, scale);
        solarFlare.drawFlare(canvas, flare, super.getRadius(), new Vector2((float)((super.getRadius() - solarFlareDistance) * Math.sin(solarFlareAngle) * drawScale.x / 5), (float)((super.getRadius() - solarFlareDistance) * Math.cos(solarFlareAngle) * drawScale.y / 5)), solarFlareAngle);
    }
}
