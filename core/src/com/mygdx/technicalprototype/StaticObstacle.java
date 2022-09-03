package com.mygdx.technicalprototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.technicalprototype.physics.obstacle.WheelObstacle;
import com.mygdx.technicalprototype.util.Helper;

public class StaticObstacle extends WheelObstacle {
    /** World position in pixel units */
    private Vector2 worldPosition;
    private boolean entryAnimationOver;
    private boolean collected;
    private boolean disappear= false;


    public StaticObstacle(Vector2 pos, float radius, Vector2 shipPos, boolean hasEntranceAnimation) {
        super((Gdx.graphics.getWidth() / 2) - shipPos.x + pos.x,
                (Gdx.graphics.getHeight() / 2) - shipPos.y + pos.y, radius);
        worldPosition = new Vector2(pos.x, pos.y);
        if (hasEntranceAnimation) {
            entryAnimationOver = false;
        } else {
            entryAnimationOver = true;
        }
    }

    public StaticObstacle(float radius) {
        super(radius);
    }

    public Vector2 getWorldPosition() {
        return worldPosition;
    }

    public float getMass() {
        return 0;
    }

    public float getRadius() {
        return super.getRadius() * drawScale.x;
    }

    public Vector2 getDrawScale() { return drawScale; }

    public Vector2 getPos() {
        return Helper.boxCoordsToWorldCoords(drawScale, worldPosition);
    }

    public boolean getCollected() {
        return collected;
    }

    public boolean getDisappear() {
        return disappear;
    }

    public boolean getEntryAnimation() {
        return entryAnimationOver;
    }

    public void setEntryAnimation(boolean b) {
        entryAnimationOver = b;
    }

    /**
     * Updates the planet location relative to ship
     *
     */
    public void setLocation(GameCanvas canvas, Vector2 shipPos) {
        super.setPosition(((canvas.getWidth() / 2) - shipPos.x) / drawScale.x + worldPosition.x,
                ((canvas.getHeight() / 2) - shipPos.y) / drawScale.y + worldPosition.y);
    }

    public void setColleted(boolean b) {
        collected = b;
    }

    public void setDisappear(boolean b) {
        disappear = b;
    }

    /**
     * Draws the resizable object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas, TextureRegion tr, float scl) {
        float scale = scl * 2 * super.getRadius() * drawScale.x / tr.getRegionWidth();
        canvas.draw(tr, Color.WHITE, origin.x + tr.getRegionWidth() / 2, origin.y + tr.getRegionHeight() / 2, getX()*drawScale.x,
                getY()*drawScale.y, 0.0f, scale, scale);
    }

    public void draw(GameCanvas canvas) {
        // hardcoded radius to scale
        float scale = 2 * super.getRadius() * drawScale.x / texture.getRegionWidth();
        canvas.draw(texture, Color.WHITE, origin.x + texture.getRegionWidth() / 2, origin.y + texture.getRegionHeight() / 2, getX()*drawScale.x,
                getY()*drawScale.y, 0.0f, scale, scale);
    }

    public void draw(GameCanvas canvas, TextureRegion tr) {
        // hardcoded radius to scale
        float scale = 2 * super.getRadius() * drawScale.x / tr.getRegionWidth();
        canvas.draw(tr, Color.WHITE, origin.x + tr.getRegionWidth() / 2, origin.y + tr.getRegionHeight() / 2, getX()*drawScale.x,
                getY()*drawScale.y, 0.0f, scale, scale);
    }

    public void drawFlare(GameCanvas canvas, TextureRegion tr, float scl, Vector2 offset, float angle) {
        // hardcoded radius to scale
        float scale = scl * drawScale.x / 300;
        canvas.draw(tr, Color.WHITE, origin.x + tr.getRegionWidth() / 2, origin.y + tr.getRegionHeight() / 2,
                getX()*drawScale.x + offset.x,getY()*drawScale.y + offset.y, (float)(-angle), scale, scale);
    }
}
