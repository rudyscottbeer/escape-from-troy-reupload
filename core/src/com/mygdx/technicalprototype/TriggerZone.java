package com.mygdx.technicalprototype;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class TriggerZone {

    public Rectangle bounds;
    public ArrayList<Vector2> directions;
    public int id;
    public int amount;
    public float distance;
    public float speed;
    public boolean respawn;
    public boolean triggered = false;

    public TriggerZone(float x, float y, float width, float height, int id, int amount, float distance, float speed,
    boolean respawn){
        directions = new ArrayList<>();
        bounds = new Rectangle();
        bounds.x = x;
        bounds.y = y;
        bounds.width = width;
        bounds.height = height;
        this.id = id;
        this.amount = amount;
        this.distance = distance;
        this.speed = speed;
        this.respawn = respawn;
    }

    public void addDirection(Vector2 direction){
        this.directions.add(direction);
    }

    public void addDirections(ArrayList<Vector2> directions){
        this.directions.addAll(directions);
    }

}
