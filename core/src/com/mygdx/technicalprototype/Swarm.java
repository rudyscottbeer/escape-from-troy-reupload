package com.mygdx.technicalprototype;

import com.badlogic.gdx.math.Vector2;

public class Swarm {
    public Vector2 screenCoord = new Vector2();
    public Vector2 worldCoord = new Vector2();
    int amount;
    float speed;
    boolean respawn;
    public int initAmmount;

    public Swarm(int amount, float speed, boolean respawn){
        initAmmount = 0;
        this.screenCoord.set(screenCoord);
        this.worldCoord.set(worldCoord);
        this.amount = amount;
        this.speed = speed;
        this.respawn = respawn;
    }
}
