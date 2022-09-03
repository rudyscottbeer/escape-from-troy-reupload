package com.mygdx.technicalprototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;


/**
 * Shadow code inspired greatly by mattdesl at https://github.com/mattdesl/lwjgl-basics/wiki
 *
 * I have written a lighting readme to explain the lighting and shadows as well.
 */

public class LightingController {

    private static final int MAX_LIGHTS = 20;
    private static float UPSCALE = 1f;

    //Lights to be rendered
    private ArrayList<Light> lights;

    //A temporary vector for runtime assignment
    private Vector3 screenPosition = new Vector3(0,0, 0);
    private Vector3 color = new Vector3(1, 0, 0);

    public LightingController(){
        //Init camera, batch, lights, and casters
        lights = new ArrayList<>();
    }


    public void addLight(Light light){
        lights.add(light);
    }

    public Light getLight(int index){
        return lights.get(index);
    }


    public void renderLights(PolygonSpriteBatch batch){
        ShaderProgram shader = batch.getShader();

        //Set the number of lights in shader
        shader.setUniformi("numLights", lights.size());

        //Render each light
        for (int i = 0; i < lights.size(); i++) {
            Light l = lights.get(i);
            l.update();
            renderLight(l, i, batch, shader);
        }





    }

    private void renderLight(Light l, int id, PolygonSpriteBatch batch, ShaderProgram shader){

        /**
         * An important note. The most complicated aspect of this system is mediating
         * between the various different coordinate systems. A more in-depth write up can be found,
         * in the lighting readme, but in brief:
         * there is the camera's coordinate system, which operates in a 400x240 viewport,
         * then a screen coordinate system, which is 1200x720 currently, but
         * should in the future support various screen sizes.
         * You will find various *3 todos in this project,
         * which signal a place to rework a translation between cam and screen coords.
         * To get the proper world position, you must get its initial coordinates, subtract the current
         * bottom left of the camera, and multiply by 3.
         */


        //Get relative light coords
        screenPosition.set(l.getPosition());

//        //Center camera position
//        Vector3 cVec = cameraHandler.getCamera().position;
//
//        //Offsets are essentially the bottom left corner of the camera
//        float xOffset = (cVec.x - cameraHandler.getViewportWidth()/2);
//        float yOffset = (cVec.y - cameraHandler.getViewportHeight()/2);

        //The screen position of the light.
        Vector3 pVec =
                screenPosition.set(
                        //TODO: *3 Location
                        (screenPosition.x - Gdx.graphics.getWidth()/2)*3,
                        (screenPosition.y - Gdx.graphics.getHeight()/2)*3,
                        1);


        //Assign this light's properties
        batch.getShader().setUniformf("lights[" + id + "].lightPosition", pVec);
        batch.getShader().setUniformf("lights[" + id + "].lightColor",
                color.set(l.getColor().r, l.getColor().g, l.getColor().b));
        batch.getShader().setUniformf("lights[" + id + "].attenuation", l.getAttenuation());



//        //This is a unknown force. My best guess is that it allows the framebuffers to be drawn properly
//        //but should be explored further. For now, it just works...
//        batch.begin();
//        batch.end();
    }
}
