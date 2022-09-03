package com.mygdx.technicalprototype.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mygdx.technicalprototype.GameCanvas;
import com.mygdx.technicalprototype.LevelMode;
import com.mygdx.technicalprototype.Rocket;

import java.util.ArrayList;

public class PathShader {

    /*
    * DEPRECATED
     */

    //What the line is comprised of
    public TextureRegion pixel;

    private Vector2 drawScale = new Vector2();

    public PathShader(TextureRegion pathPixel) {
        pixel = pathPixel;
    }

    public void setDrawScale(Vector2 drawScale) {
        this.drawScale = drawScale;
    }

    /**
     * Creates a path by drawing a pixel at each point in arrayPath
     * @param canvas The game canvas
     * @param arrayPath The path generated from physics
     */
    public void drawPath(GameCanvas canvas, ArrayList<Vector2> arrayPath, Vector2 shipPos) {
        Vector2 origin = new Vector2(pixel.getRegionWidth()/2.0f, pixel.getRegionHeight()/2.0f);
        for (int i = 0; i < arrayPath.size(); i += 10) {
            canvas.draw(pixel, Color.WHITE, origin.x, origin.y,
                    arrayPath.get(i).x-shipPos.x+canvas.getWidth() / 2, arrayPath.get(i).y-shipPos.y+canvas.getHeight() / 2, 0.0f, 2, 2);
        }
        for (int i = 0; i < LevelMode.debugPts.size(); i++) {
            canvas.draw(pixel, Color.RED, origin.x, origin.y,
                    arrayPath.get(i).x-shipPos.x+canvas.getWidth() / 2, arrayPath.get(i).y-shipPos.y+canvas.getHeight() / 2, 0.0f, 5, 5);
        }
    }

    /***
     * A helper for shader creation.
     * @param vert A string vertex shader (not the file locale)
     * @param frag A string fragment shader (not the file locale)
     * @return A shader program from the frag and vert strings.
     */
    public static ShaderProgram createShader(String vert, String frag) {
        ShaderProgram prog = new ShaderProgram(vert, frag);
        if (!prog.isCompiled())
            throw new GdxRuntimeException("could not compile "+ vert + " or " + frag + ": " + prog.getLog());
        if (prog.getLog().length() != 0)
            Gdx.app.log("GpuOutput", prog.getLog());
        return prog;
    }

}
