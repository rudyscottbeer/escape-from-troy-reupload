package com.mygdx.technicalprototype;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.technicalprototype.assets.AssetDirectory;
import com.mygdx.technicalprototype.util.PooledList;

public class Minimap extends ScreenViewport {

    /** The Pixmap for the minimap texture */
    private Pixmap pmap;
    /** The minimap texture */
    private TextureRegion textureCache;
    /** The minimap width in pixels, including padding */
    private int width;
    /** The minimap height in pixels, including padding */
    private int height;
    /** The number of pixels in the padding */
    private int padding;
    /** The border width */
//    private static final int BORDER_WIDTH = 1;
    /** The positions of the objects to be drawn */
    private PooledList<Vector2> planets;
    private PooledList<Vector2> goals;
    private PooledList<Vector2> entries;
    private PooledList<Vector2> checkpoints;
    /** Whether the positions of the objects are set yet */
    private boolean loaded;

    /** The rightmost and topmost x,y positions of the objects */
    private Vector2 topRight;
    /** The left and bottommost x,y positions of the objects */
    private Vector2 bottomLeft;
    /** The offset to center the minimap */
    private Vector2 middleOffset;

    /** How much to scale the positions of the objects in the map */
    private Vector2 posScale;
    /** The minimap texture objects */
//    private TextureRegion shipTexture;
//    private TextureRegion entryTexture;
//    private TextureRegion goalTexture;
//    private TextureRegion planetTexture;
//    private TextureRegion checkpointTexture;

    private Pixmap shipPix;
    private Pixmap goalPix;
    private Pixmap entryPix;
    private Pixmap planetPix;
    private Pixmap checkpointPix;


    /**
     * The constructor of the minimap
     *
     * @param width  the minimap width without padding
     * @param height the minimap height without padding
     */
    public Minimap(int width, int height, int padding) {
        this.padding = padding;
        this.width = width + 2 * padding;
        this.height = height + 2 * padding;
        this.loaded = false;
        this.pmap = new Pixmap(this.width, this.height, Pixmap.Format.RGBA8888);
        this.planets = new PooledList<>();
        this.goals = new PooledList<>();
        this.entries = new PooledList<>();
        this.checkpoints = new PooledList<>();
        this.posScale = new Vector2();
        this.topRight = new Vector2();
        this.middleOffset = new Vector2();
        this.bottomLeft = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);
    }

    /**
     * Fills the object position arrays. It only loads once.
     *
     * @param obstacles
     */
    public void loadPositions(PooledList<GameObject> obstacles) {
        Vector2 pos;
        if (!loaded) {
            for (GameObject obs : obstacles) {
                if (obs instanceof Planet) {
                    pos = ((Planet) obs).getWorldPosition();
                    planets.add(pos.cpy());
                    findFurthest(pos);
                } else if (obs instanceof Enemy) {

                } else if (obs.getName().equals("goal")) {
                    pos = ((StaticObstacle) obs).getWorldPosition();
                    goals.add(pos.cpy());
                    findFurthest(pos);
                } else if (obs.getName().equals("entry")) {
                    pos = ((StaticObstacle) obs).getWorldPosition();
                    entries.add(pos.cpy());
                    findFurthest(pos);
                } else if (obs instanceof Checkpoint) {
                    pos = ((Checkpoint) obs).getWorldPosition();
                    checkpoints.add(pos.cpy());
                    findFurthest(pos);
                }
            }

//            float horDiff = (topRight.x - bottomLeft.x) < 1 ? 1 : (topRight.x - bottomLeft.x);
//            float vertDiff = (topRight.y - bottomLeft.y) < 1 ? 1 : (topRight.y - bottomLeft.y);

            float horDiff = (topRight.x - bottomLeft.x);
            float vertDiff = (topRight.y - bottomLeft.y);

            // get smallest scale value
            float scaleVal = Math.min((width - 2 * padding) / horDiff, (height - 2 * padding) / vertDiff);
            posScale.x = scaleVal;
            posScale.y = scaleVal;

            middleOffset.x = ((width-2*padding)/scaleVal- (topRight.x + bottomLeft.x))/2f;
            middleOffset.y = ((height-2*padding)/scaleVal - (topRight.y + bottomLeft.y))/2f;

            for (Vector2 pl : planets) {
                pl.add(middleOffset);
                pl.scl(posScale);
                pl.x += padding;
                pl.y += padding;
            }
            for (Vector2 goal : goals) {
                goal.add(middleOffset);
                goal.scl(posScale);
                goal.x += padding;
                goal.y += padding;
            }
            for (Vector2 entr : entries) {
                entr.add(middleOffset);
                entr.scl(posScale);
                entr.x += padding;
                entr.y += padding;
            }
            for (Vector2 checks : checkpoints) {
                checks.add(middleOffset);
                checks.scl(posScale);
                checks.x += padding;
                checks.y += padding;
            }
        }
        loaded = true;
    }

    /**
     * Sets the min and max position attributes.
     *
     * @param pos the position in consideration
     */
    private void findFurthest(Vector2 pos) {
        topRight.x = Math.max(topRight.x, pos.x);
        topRight.y = Math.max(topRight.y, pos.y);
        bottomLeft.x = Math.min(bottomLeft.x, pos.x);
        bottomLeft.y = Math.min(bottomLeft.y, pos.y);
    }

    /**
     * Redraws the map with updated position information from the ship
     *
     * @param rocket the ship to be drawn
     * @param scale  the ship draw scale
     */
    public void updateMap(Rocket rocket, Vector2 scale) {
        pmap.setColor(new Color(0, 0, 0, 0.5f));
        pmap.fill();
        // draw objects
        for (Vector2 pos : planets) {
            pmap.drawPixmap(planetPix,(int) pos.x, (int) pos.y);
        }
        for (Vector2 pos : entries) {
            pmap.drawPixmap(goalPix,(int) pos.x, (int) pos.y);
        }
        for (Vector2 pos : goals) {
            pmap.drawPixmap(goalPix,(int) pos.x, (int) pos.y);
        }
        for (Vector2 pos : checkpoints) {
            pmap.drawPixmap(checkpointPix,(int) pos.x, (int) pos.y);
        }
        // draw ship
        Vector2 shipPos = rocket.getPos();
        shipPos.x /= scale.x;
        shipPos.y /= scale.y;
        shipPos.add(middleOffset);
        shipPos.scl(posScale);
        pmap.drawPixmap(shipPix, (int) shipPos.x + padding, (int) shipPos.y + padding);

        // draw border
        pmap.setColor(new Color(255, 255, 255, 0.5f));
        pmap.drawRectangle(0, 0, width, height);
        pmap.drawRectangle(1, 1, width-2, height-2);

    }

    /**
     * Returns a new texture of the minimap
     *
     * @return the minimap texture
     */
    public TextureRegion generateMap() {
        if (textureCache != null) {
            textureCache.getTexture().dispose();
        }
        textureCache = new TextureRegion(new Texture(pmap));
        return textureCache;
    }

    /**
     * Loads the textures and Pixmaps of the map object textures
     * @param directory
     */
    public void gatherAssets(AssetDirectory directory) {
        TextureRegion shipTexture = new TextureRegion(directory.getEntry("minimap:ship", Texture.class));
        TextureRegion planetTexture = new TextureRegion(directory.getEntry("minimap:planet", Texture.class));
        TextureRegion goalTexture = new TextureRegion(directory.getEntry("minimap:goal", Texture.class));
        TextureRegion entryTexture = new TextureRegion(directory.getEntry("minimap:goal", Texture.class));
        TextureRegion checkpointTexture = new TextureRegion(directory.getEntry("minimap:checkpoint", Texture.class));

        TextureData shipTexData = shipTexture.getTexture().getTextureData();
        TextureData planetTexData = planetTexture.getTexture().getTextureData();
        TextureData goalTexData = goalTexture.getTexture().getTextureData();
        TextureData entryTexData = entryTexture.getTexture().getTextureData();
        TextureData checkpointTexData = checkpointTexture.getTexture().getTextureData();

        if (!shipTexData.isPrepared()) {
            shipTexData.prepare();
        }
        if (!planetTexData.isPrepared()) {
            planetTexData.prepare();
        }
        if (!goalTexData.isPrepared()) {
            goalTexData.prepare();
        }
        if (!entryTexData.isPrepared()) {
            entryTexData.prepare();
        }
        if (!checkpointTexData.isPrepared()) {
            checkpointTexData.prepare();
        }
        shipPix = shipTexData.consumePixmap();
        goalPix = goalTexData.consumePixmap();
        planetPix = planetTexData.consumePixmap();
        entryPix = goalPix;
        checkpointPix = checkpointTexData.consumePixmap();
    }
}
