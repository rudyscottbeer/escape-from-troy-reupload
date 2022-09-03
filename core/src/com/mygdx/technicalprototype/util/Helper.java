package com.mygdx.technicalprototype.util;

import com.badlogic.gdx.math.Vector2;

public class Helper {
    public static Vector2 boxCoordsToWorldCoords(Vector2 drawScale, Vector2 coords) {
        return new Vector2(coords.x*drawScale.x, coords.y*drawScale.y);
    }

    public static Vector2 worldCoordsToBoxCoords(Vector2 drawScale, Vector2 coords) {
        return new Vector2(coords.x/drawScale.x, coords.y/drawScale.y);
    }

    // TODO: make helper for all asset setups
//    public static void setUpAsset(Obstacle o, ) {
//
//    }
}
