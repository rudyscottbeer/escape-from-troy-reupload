package com.mygdx.technicalprototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.technicalprototype.physics.obstacle.BoxObstacle;
import com.mygdx.technicalprototype.physics.obstacle.WheelObstacle;
import com.mygdx.technicalprototype.util.Helper;

public class Checkpoint extends BoxObstacle {
    private Vector2 worldPosition;
    /** If checkpoint is in the collected animation */
    private boolean inCollectedAnimation;
    /** If checkpoint was collected */
    private boolean collected;

    public Checkpoint(Vector2 pos, Vector2 shipPos) {
        super((Gdx.graphics.getWidth() / 2) - shipPos.x + pos.x, (Gdx.graphics.getHeight() / 2) - shipPos.y + pos.y, 1.7f, 3.4f);
        worldPosition = new Vector2(pos.x, pos.y);
        collected = false;
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

    public Vector2 getWorldPosition() {
        return worldPosition;
    }

    public void collect() {
        collected = true;
    }

    public boolean isCollected() {
        return collected;
    }

    public void changeCollectAnimation(boolean b) {
        inCollectedAnimation = b;
    }

    public boolean isCollectedAnimation() {
        return inCollectedAnimation;
    }

    /**
     * Updates the planet location relative to ship
     *
     */
    public void setLocation(GameCanvas canvas, Vector2 shipPos) {
        super.setPosition(((canvas.getWidth() / 2) - shipPos.x) / drawScale.x + worldPosition.x,
                ((canvas.getHeight() / 2) - shipPos.y) / drawScale.y + worldPosition.y);
    }

    /**
     * Draws the resizable object.
     *
     * @param canvas Drawing context
     * @param tr The TextureRegion to draw
     */
    public void draw(GameCanvas canvas, TextureRegion tr) {
        // hardcoded radius to scale
        float scale = 3f * super.getWidth() * drawScale.x / tr.getRegionWidth();
        canvas.draw(tr, Color.WHITE, origin.x + tr.getRegionWidth() / 2, origin.y + tr.getRegionHeight() / 2, getX()*drawScale.x,
                getY()*drawScale.y, 0.0f, scale, scale);
    }
}
