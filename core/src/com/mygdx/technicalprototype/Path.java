package com.mygdx.technicalprototype;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.technicalprototype.util.Helper;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Path {
    private final int MAX_POINTS = 5000;
    public static final int ORBIT_DISTANCE = 500;
    public static final float ORBIT_DIST_FACTOR = 1.7f;

    public ArrayList<Vector2> forwardPts = new ArrayList<>();
    public ArrayList<Vector2> backwardPts = new ArrayList<>();
    public ArrayList<Vector2> forwardVels = new ArrayList<>();
    public ArrayList<Vector2> backwardVels = new ArrayList<>();
    public Vector2 lastOrbitPt = null;
    public Vector2 lastOrbitVel = null;
    public Vector2 secondLastOrbitPt = null;
    public Vector2 secondLastOrbitVel = null;
    public int index = -1;
    public TextureRegion pixel;

    public Path() {
    }

    /**
     * Calculates the path of the ship using the gravitational force between the ship and the bodies,
     * but only when the ship is within the radius of the bodies. Otherwise, the ship moves straight.
     * 
     * @param forwardPathPos position of the path
     * @param forwardPathVel velocity of the path
     * @param bodies List of bodies in the simulation
     */
    public void calculatePath(Vector2 forwardPathPos, Vector2 forwardPathVel, Vector2 backwardPathPos, Vector2 backwardPathVel, ArrayList<Planet> bodies) {
        Vector2 forwardPathPosition = new Vector2(forwardPathPos);
        Vector2 backwardPathPosition = new Vector2(backwardPathPos);
        Vector2 forwardPathVelocity = new Vector2(forwardPathVel);
        Vector2 backwardPathVelocity = new Vector2(-backwardPathVel.x, -backwardPathVel.y);
        forwardPts.clear();
        forwardPts.add(new Vector2(forwardPathPosition));
        backwardPts.clear();
        backwardPts.add(new Vector2(backwardPathPosition));
        forwardVels.clear();
        forwardVels.add(new Vector2(forwardPathVelocity));
        backwardVels.clear();
        backwardVels.add(new Vector2(backwardPathVelocity));

        for (int i = 0; i < MAX_POINTS; i++) {
            // calculate gravity attraction if planets nearby
            for (Planet body : bodies) {
                if (forwardPathPosition.dst(body.getPos()) <= body.getRadiusAfterChange() * ORBIT_DIST_FACTOR) {
                    addOrbitingPath(forwardPathPosition, forwardPathVelocity, body, forwardPts, forwardVels, false);
                }
                if (backwardPathPosition.dst(body.getPos()) <= body.getRadiusAfterChange() * ORBIT_DIST_FACTOR) {
                    addOrbitingPath(backwardPathPosition, backwardPathVelocity, body, backwardPts, backwardVels, true);
                }
            }
            // move forwards if no nearby planets
            forwardPathPosition.add(forwardPathVelocity);
            forwardPts.add(new Vector2(forwardPathPosition));
            backwardPathPosition.add(backwardPathVelocity);
            backwardPts.add(new Vector2(backwardPathPosition));

            forwardVels.add(new Vector2(forwardPathVelocity));
            backwardVels.add(new Vector2(backwardPathVelocity));
        }
    }

    /**
     * Adds an orbiting path around a given planet body
     *
     * @param pathPosition the position of the path
     * @param pathVelocity the velocity of the path (direction)
     * @param body the body of the planet
     * @param posPts the ArrayList of positions along the path
     * @param velPts the ArrayList of velocities along the path
     */
    private void addOrbitingPath(Vector2 pathPosition, Vector2 pathVelocity, Planet body, ArrayList<Vector2> posPts, ArrayList<Vector2> velPts, boolean back) {
        Vector2 planetCenter = body.getPos();
        float angle, dist, posX = pathPosition.x, posY = pathPosition.y, newPosX, newPosY;
        // angle that the ship is coming in
        angle = (float) ((Math.toDegrees(Math.atan2(posX - planetCenter.x, posY - planetCenter.y)) + 630.0) % 360.0);
        // distance along the orbit that the ship is in
        dist = (float) (angle * body.getRadiusAfterChange() * 2 * Math.PI / 360);
        // calculates if the ship should turn clockwise in the orbit
        // rightTurn = 1 indicates true (Python truthy), -1 indicates false
        Vector2 toPlanetCenter = new Vector2(planetCenter).sub(pathPosition);
        Vector2 pathVelocityVector = Helper.boxCoordsToWorldCoords(body.getDrawScale(), new Vector2(pathVelocity)).scl(toPlanetCenter.len());
        float newPosXClock = (float)(planetCenter.x + body.getRadiusAfterChange() * Math.cos(- (dist + 0.5) / body.getRadiusAfterChange()) * ORBIT_DIST_FACTOR);
        float newPosYClock = (float)(planetCenter.y + body.getRadiusAfterChange() * Math.sin(- (dist + 0.5) / body.getRadiusAfterChange()) * ORBIT_DIST_FACTOR);
        float newPosXAnti = (float)(planetCenter.x + body.getRadiusAfterChange() * Math.cos(- (dist - 0.5) / body.getRadiusAfterChange()) * ORBIT_DIST_FACTOR);
        float newPosYAnti = (float)(planetCenter.y + body.getRadiusAfterChange() * Math.sin(- (dist - 0.5) / body.getRadiusAfterChange()) * ORBIT_DIST_FACTOR);
        int clockwise;
        if (new Vector2(newPosXClock, newPosYClock).sub(pathVelocityVector).len() < new Vector2(newPosXAnti, newPosYAnti).sub(pathVelocityVector).len()) {
            clockwise = 1;
        } else {
            clockwise = -1;
        }
        // adding to dist makes clockwise turns
        for (int i = 0; i < ORBIT_DISTANCE; i++) {
            dist += (float)(clockwise * 0.5);
            newPosX = (float)(planetCenter.x + body.getRadiusAfterChange() * Math.cos(- dist / body.getRadiusAfterChange()) * ORBIT_DIST_FACTOR);
            newPosY = (float)(planetCenter.y + body.getRadiusAfterChange() * Math.sin(- dist / body.getRadiusAfterChange()) * ORBIT_DIST_FACTOR);
            velPts.add(new Vector2(newPosX - posX, newPosY - posY));
            posX = newPosX;
            posY = newPosY;
            posPts.add(new Vector2(posX, posY));
        }
        Vector2 lastVel = velPts.get(velPts.size() - 1);
        Vector2 lastPt = new Vector2(0, 0);
        for (int i = 0; i < 100; i++) {
            lastPt = posPts.get(posPts.size() - 1);
            velPts.add(new Vector2(lastVel));
            posPts.add(new Vector2(lastPt.x + lastVel.x, lastPt.y + lastVel.y));
        }
        pathPosition.set(lastPt);
        pathVelocity.set(lastVel);
    }

    public void moveForward(int amount) {
        if (amount < 0) {
            for (int i = 0; i < -amount; i++) {
                Vector2 backwardsVel = backwardVels.remove(0);
                Vector2 backwardsPt = backwardPts.remove(0);
                forwardPts.add(0, backwardsPt);
                forwardVels.add(0, new Vector2(-backwardsVel.x, -backwardsVel.y));
            }
        } else if (amount > 0) {
            for (int i = 0; i < amount; i++) {
                Vector2 forwardsVel = forwardVels.remove(0);
                Vector2 forwardsPt = forwardPts.remove(0);
                backwardPts.add(0, forwardsPt);
                backwardVels.add(0, new Vector2(-forwardsVel.x, -forwardsVel.y));
            }
        }
    }

    public void calculateLastTwoPoints(Planet p) {
        for (int i = 0; i < backwardPts.size(); i++) {
            if (backwardPts.get(i + 1).dst(p.getPos()) > p.getRadiusAfterChange() * ORBIT_DIST_FACTOR + 1) {
                lastOrbitPt = backwardPts.get(i);
                lastOrbitVel = new Vector2(backwardVels.get(i)).scl(-1);
                secondLastOrbitPt = backwardPts.get(i + 2);
                secondLastOrbitVel = new Vector2(backwardVels.get(i + 2)).scl(-1);
//                LevelMode.debugPts.clear();
//                LevelMode.debugPts.add(new Vector2(lastOrbitPt));
//                LevelMode.debugPts.add(new Vector2(secondLastOrbitPt));
                index = i;
                if (index >= ORBIT_DISTANCE - 10) {
                    index -= 10;
                }
                return;
            }
        }
    }

    public void calculateLastTwoPointsBackwards(Planet p) {
        for (int i = 0; i < backwardPts.size() - 1; i++) {
            if (backwardVels.get(0).equals(backwardVels.get(1))) {
                Vector2 lastPos = new Vector2(backwardPts.get(0));
                Vector2 lastVel = new Vector2(backwardVels.get(0)).scl(-1);
                index = i;
                if (index >= ORBIT_DISTANCE - 10) {
                    index -= 10;
                }
                while (backwardPts.get(0).dst(p.getPos()) > p.getRadiusAfterChange() * ORBIT_DIST_FACTOR &&
                    backwardPts.get(0).dst(p.getPos()) < backwardPts.get(1).dst(p.getPos())) {
                    lastPos.add(lastVel);
                    backwardPts.add(0, new Vector2(lastPos));
                    backwardVels.add(0, new Vector2(lastVel).scl(-1));
                }
                break;
            }
            backwardPts.remove(0);
            backwardVels.remove(0);
        }

        for (int i = 0; i < backwardPts.size() - 2; i++) {
            if (backwardPts.get(i + 1).dst(p.getPos()) > p.getRadiusAfterChange() * ORBIT_DIST_FACTOR + 1) {
                lastOrbitPt = backwardPts.get(i);
                lastOrbitVel = new Vector2(backwardVels.get(i)).scl(-1);
                secondLastOrbitPt = backwardPts.get(i + 2);
                secondLastOrbitVel = new Vector2(backwardVels.get(i + 2)).scl(-1);
                return;
            }
        }
    }

    public void addPath(Vector2 pathPosition, Vector2 pathVelocity, ArrayList<Planet> bodies) {
        if (forwardPts.size() > MAX_POINTS + 500) {
            forwardPts.subList(forwardPts.size() - 50, forwardPts.size()).clear();
            forwardVels.subList(forwardVels.size() - 50, forwardVels.size()).clear();
        }
        if (backwardPts.size() > MAX_POINTS + 500) {
            backwardPts.subList(backwardPts.size() - 50, backwardPts.size()).clear();
            backwardVels.subList(backwardVels.size() - 50, backwardVels.size()).clear();
        }
        Vector2 forwardPathPosition = forwardPts.get(forwardPts.size() - 1);
        Vector2 backwardPathPosition = backwardPts.get(backwardPts.size() - 1);
        Vector2 forwardPathVelocity = forwardVels.get(forwardVels.size() - 1);
        Vector2 backwardPathVelocity = backwardVels.get(backwardVels.size() - 1);
        while (forwardPts.size() < MAX_POINTS) {
            // calculate gravity attraction if planets nearby
            for (Planet body : bodies) {
                if (forwardPathPosition.dst(body.getPos()) < body.getRadiusAfterChange() * ORBIT_DIST_FACTOR) {
                    addOrbitingPath(forwardPathPosition, forwardPathVelocity, body, forwardPts, forwardVels, false);
                }
            }
            // move forwards if no nearby planets
            forwardPathPosition.add(forwardPathVelocity);
            forwardPts.add(new Vector2(forwardPathPosition));

            forwardVels.add(new Vector2(forwardPathVelocity));
        }
        while (backwardPts.size() < MAX_POINTS) {
            // calculate gravity attraction if planets nearby
            for (Planet body : bodies) {
                if (backwardPathPosition.dst(body.getPos()) < body.getRadiusAfterChange() * ORBIT_DIST_FACTOR) {
                    addOrbitingPath(backwardPathPosition, backwardPathVelocity, body, backwardPts, backwardVels, true);
                }
            }
            // move forwards if no nearby planets
            backwardPathPosition.add(backwardPathVelocity);
            backwardPts.add(new Vector2(backwardPathPosition));

            backwardVels.add(new Vector2(backwardPathVelocity));
        }
    }

    /**
     * Given the Vector2 velocity, scales it down such that the magnitude
     * is 0.5
     * 
     * @param velocity Vector2 to scale down
     * @return Scaled Vector2
     */
    private Vector2 scaleVelocity(Vector2 velocity) {
        float magnitude = velocity.len();
        return velocity.scl(0.5f/magnitude);
    }

    /**
     * Returns list of forwards points that draw out the path.
     *
     * @return list of Vector2 objects representing the points that draw out the path
     */
    public ArrayList<Vector2> getAllForwardPoints() {
        return forwardPts;
    }

    /**
     * Returns list of backwards points that draw out the path.
     *
     * @return list of Vector2 objects representing the points that draw out the path
     */
    public ArrayList<Vector2> getAllBackwardPoints() {
        return backwardPts;
    }

    /**
     * Returns forwards velocities that draw out the path.
     *
     * @return vector2 of the next velocity backwards movement
     */
    public ArrayList<Vector2> getAllForwardVels() {
        return forwardVels;
    }

    /**
     * Returns backwards velocities that draw out the path.
     *
     * @return vector2 of the next velocity forwards movement
     */
    public ArrayList<Vector2> getAllBackwardVels() {
        return backwardVels;
    }

}
