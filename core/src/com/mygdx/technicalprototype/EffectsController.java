package com.mygdx.technicalprototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class EffectsController {

    private static final float APPROX_VALUE = 0.05f;

    private  Color alertColor = Color.FIREBRICK;

    private Vector2 temp = new Vector2();
    private Vector3 temp1 = new Vector3();

    private Vector2 far = new Vector2(10000000, 100000000);




    /*
    * Variables for a shockwave
     */
    private Vector2 shockCenter = new Vector2();
    private float shockTime = 0f;
    private float tChangeSpeedShock = 1f;
    private float maxTimeShock = 1f;
    private boolean inShockwave = false;

    /*
     * FOR TOMORROW:
     * The two shaders are linked. Change Color I believe will work
     * Pull request
     * Link explode to shockwave
     * Link alert to spawn of swarm to
     */

    /*
     * Variables for an alert
     */
    private Vector2 alertCenter = new Vector2();
    private float alertTime = 0f;
    private float tChangeSpeedAlert = 0.25f;
    private float alertSize = 0.75f;
    private boolean inAlert = false;
    private boolean alertFallback = false;





    public void triggerShock(Vector2 center){
        this.shockCenter = center;
        shockTime = 0f;
        inShockwave = true;

    }

    public void triggerAlert(Vector2 alertDirection, Color color){
        this.alertCenter.set(alertDirection.x, alertDirection.y);
        this.alertFallback = false;
        this.alertColor = color;
        alertTime = 0f;
        inAlert = true;
    }

    public void update(){

    }

    public void draw(GameCanvas canvas, float dt){
        canvas.shader.setUniformf("iResolution", temp.set(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        if(inShockwave){
            shockTime+=tChangeSpeedShock*dt;
            if(shockTime >= maxTimeShock){
                shockTime = 0;
                inShockwave = false;
            }
            canvas.shader.setUniformf("iTime", shockTime);
            canvas.shader.setUniformf("iCenter", shockCenter);
        }else{
            canvas.shader.setUniformf("iTime", 0);
            canvas.shader.setUniformf("iCenter", far);
        }

        if(inAlert){
            if(alertTime >= alertSize - APPROX_VALUE){
                alertFallback = true;
            }

            if(alertFallback){
                alertTime = Interpolation.bounceIn.apply(alertTime, 0, tChangeSpeedAlert);
                //alertTime = MathUtils.lerp(alertTime, 0, tChangeSpeedAlert*dt);
            }else{
                alertTime = Interpolation.bounceIn.apply(alertTime, alertSize, tChangeSpeedAlert);
                //alertTime = MathUtils.lerp(alertTime, alertSize, tChangeSpeedAlert*dt);
            }

            if(alertTime <= 0+APPROX_VALUE && alertFallback){
                alertTime = 0;
                inAlert = false;
            }

            canvas.shader.setUniformf("iAlertColor", temp1.set(alertColor.r, alertColor.g, alertColor.b));
            canvas.shader.setUniformf("iAlertCenter", alertCenter);
            canvas.shader.setUniformf("iAlertInnerSize", alertTime);
            canvas.shader.setUniformf("iAlertOuterSize", 0f);
        } else {
            canvas.shader.setUniformf("iAlertInnerSize", -1f);
        }
    }

}
