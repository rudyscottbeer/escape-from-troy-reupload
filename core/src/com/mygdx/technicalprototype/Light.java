package com.mygdx.technicalprototype;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public interface Light {

    /***
     * A light interface to be used in conjuction with Lighting Renderer
     */


    public Vector3 getPosition();
    public Color getColor();
    public Vector3 getAttenuation();


    public void setPosition(Vector3 pos);
    public void setPosition(float x, float y, float z);

    public void setAttenuation(Vector3 pos);
    public void setAttenuation(float x, float y, float z);

    public void setColor(Color color);
    public void setColor(float r, float g, float b);

    public void update();


}


