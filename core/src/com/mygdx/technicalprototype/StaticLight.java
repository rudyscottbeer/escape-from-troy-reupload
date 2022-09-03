package com.mygdx.technicalprototype;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

/***
 * A light structure to be used in conjuction with Lighting Renderer
 */
public class StaticLight implements Light{

    //Light coordinate
    private Vector3 pos = new Vector3(0, 0, 0);
    //Light attenuation (float constant, float linear, float quadratic)
    private Vector3 attenuation = new Vector3(0, 0, 1);
    //Light color (r, g, b)
    private Color color = Color.WHITE;

    public StaticLight(){ }

    public StaticLight(Vector3 pos, Vector3 attenuation, Color color){
        this.pos = pos;
        this.attenuation = attenuation;
        this.color = color;
    }

    public StaticLight(float x, float y, float z, float constantAtt, float linearAtt, float quadAtt, Color color){
        this(new Vector3(x , y, z), new Vector3(constantAtt, linearAtt, quadAtt), color);
    }

    public StaticLight(float x, float y, float z){
        this(new Vector3(x , y, z), new Vector3(0, 0f, 1f), Color.WHITE);
    }

    public Vector3 getPosition(){
        return pos;
    }
    public Color getColor(){
        return color;
    }
    public Vector3 getAttenuation(){
        return attenuation;
    }


    public void setPosition(Vector3 pos){
        this.pos = pos;
    }
    public void setPosition(float x, float y, float z){
        this.pos.set(x, y, z);
    }

    public void setAttenuation(Vector3 pos){
        this.attenuation = attenuation;
    }
    public void setAttenuation(float x, float y, float z){
        this.attenuation.set(x, y, z);
    }

    public void setColor(Color color){
        this.color = color;
    }
    public void setColor(float r, float g, float b){
        this.color.set(r, g, b, 1);
    }

    public void update(){
        return;
    };


}
