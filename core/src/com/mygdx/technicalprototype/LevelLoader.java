package com.mygdx.technicalprototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.technicalprototype.assets.AssetDirectory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class LevelLoader {
//    private static final int backgroundScale = 2;
    private static final float PARALLAX_MIN = 0.5f;
    private static final float PARALLAX_MAX = 0.8f;


    private ArrayList<int[]> levelArray;
    private Array<TextureRegion> tilesets;
    private int tileWidth;
    private int tileHeight;
    private int mapHeight;
    private int mapWidth;

    private int mapPixelHeight;
    private int mapPixelWidth;

    private int backgroundScale = 2;

    private int upper;
    private int sidder;

    private String levelName;

    private AssetDirectory internal;

    private Vector2 scale;

    /** Object lists for use in LevelMode */
    private ArrayList<Planet> resizables = new ArrayList<>();
    private ArrayList<StaticObstacle> staticObstacles = new ArrayList<>();
    private HashMap<Integer, ArrayList<DynamicObstacle>> dynamicObstacles = new HashMap<>();
    private HashMap<Integer, ArrayList<Enemy>> enemies = new HashMap<>();
    private HashMap<Integer, ArrayList<StaticObstacle>> rewards = new HashMap<>();


    private ArrayList<Checkpoint> checkpoints = new ArrayList<>();
    private ArrayList<TriggerZone> enemyAreas = new ArrayList<>();

    private ArrayList<JsonValue> objectLayers = new ArrayList<>();


    private HashMap<Integer, ArrayList<Vector2>> paths = new HashMap<>();

    /** This will be changed in the future, but entry and goal are defined here*/
    StaticObstacle entry;
    StaticObstacle goal;

    public String levelfancyname = "";



    public LevelLoader(String levelName, JsonValue levelFile, Vector2 scale){
        /** Set level name */
        this.levelName = levelName;
        this.scale = scale;
        parseLevel(levelFile);
    }

    public void setScale(Vector2 scale){

        this.scale = scale;
    }

    public int getMapPixelHeight() {
        return mapPixelHeight;
    }

    public int getMapPixelWidth() {
        return mapPixelWidth;
    }

    public void parseLevel(JsonValue levelFile){
        internal = new AssetDirectory("tilesets.json");
        internal.loadAssets();
        internal.finishLoading();

        int cLevel = levelFile.getInt("compressionlevel");
        if(cLevel != -1)
            backgroundScale = cLevel;


        /** Load various level constants */
        mapHeight = levelFile.getInt("height");
        mapWidth = levelFile.getInt("width");

        tileWidth = levelFile.getInt("tilewidth")/backgroundScale;
        tileHeight = levelFile.getInt("tileheight")/backgroundScale;

try {
    if (levelFile.get("properties") != null) {
        JsonValue jv = levelFile.get("properties").get(0);
        if (jv.getString("name") != null) {
            levelfancyname = jv.getString("value");
        }
    }
}catch(IllegalArgumentException e){

    }

//        mapPixelHeight = mapHeight*levelFile.getInt("tileheight");
//        mapPixelWidth = mapWidth*levelFile.getInt("tilewidth");

        mapPixelHeight = mapHeight*tileHeight;
        mapPixelWidth = mapWidth*tileWidth;



        /** Load the tileset textures */
        tilesets = new Array<>();
        parseTilesets(levelFile, internal);


        /** Load the various object and tile layers */
        JsonValue layers = levelFile.get("layers");

        /** Load the layers of the level */
        levelArray = new ArrayList<>();

        for (int i = 0; i < layers.size; i++) {

            JsonValue currLayer = layers.get(i);

            if (currLayer.getString("type").equals("tilelayer")){
                parseTileLayer(currLayer);
            }else if(currLayer.getString("type").equals("objectgroup")){
                objectLayers.add(currLayer);
            }
        }

    }

    public void loadLevel(Vector2 checkpoint){
        for (int i = 0; i < objectLayers.size(); i++) {
            parseObjectGroup(objectLayers.get(i), checkpoint);
        }
    }

    private void parseTilesets(JsonValue levelFile, AssetDirectory internal){
        JsonValue tsets = levelFile.get("tilesets");
        for (int i = 0; i < tsets.size; i++) {

            JsonValue currSet = tsets.get(i);

            try {

                String[] t1 = currSet.getString("source").split(Pattern.quote("."));
                String[] t2 = t1[t1.length - 2].split(Pattern.quote("/"));
                String tsetName = t2[t2.length - 1];

                JsonValue tsjFile = internal.getEntry(levelName + ":"+tsetName, JsonValue.class);
                int tilecount = tsjFile.getInt("tilecount");
                if(tilecount == 1){
                    addTileTexture(tsetName);
                }else{

                    for (int j = 1; j <= tilecount; j++) {
                    addTileTexture(tsetName+j);
                    }
                }
            }catch(IllegalArgumentException e){
                System.err.println("JSon tileset: "+currSet.getString("source")+" not found in tilesets.json.");
            }

            }
    }

    private void addTileTexture(String entryName){
        try {
            tilesets.add(new TextureRegion(internal.getEntry(levelName + ":" + entryName, Texture.class)));
        } catch (IllegalArgumentException e) {
            tilesets.add(null);
            System.out.println("Texture: " + levelName + ":" + entryName + "was not found. This is likely due to an absence in tilesets.json");
        }
    }

    private void parseObjectGroup(JsonValue currLayer, Vector2 checkpoint){
    JsonValue objects = currLayer.get("objects");
        for (int i = 0; i < objects.size; i++) {
            initObject(objects.get(i), checkpoint);
        }

    }

    private void initObject(JsonValue object, Vector2 checkpoint){
        switch (object.getString("type")){
            case "Resizeable":
                Vector2 v = new Vector2(object.getFloat("x")/backgroundScale/scale.x , (mapPixelHeight - (object.getFloat("y")/backgroundScale))/scale.y);
                String color = getProperty(object, "PlanetColor");
                float radius = getRadius(object);
                String min = getProperty(object, "MinRadius");
                int minSize = -1;
                if(min != null){
                    minSize = Integer.parseInt(min);
                }
                String max = getProperty(object, "MaxRadius");
                int maxSize = 2;
                if(max != null){
                    maxSize = Integer.parseInt(max);
                }
                String dgr = getProperty(object, "angle");
                int angle = 180;
                if(dgr != null){
                    angle = Integer.parseInt(dgr);
                }



                Planet p = new Planet(v, radius, checkpoint,
                        stringToColor(color), maxSize, minSize, angle);
                resizables.add(p);
                break;
            case "StaticObstacle":
                loadStaticObstacle(object);
                break;
            case "TriggerArea":
                loadEnemyArea(object);
                break;
            case "PathPoint":
                loadPath(object);
                break;
            case "DynamicObstacle":
                loadDynamicObstacle(object);
                break;
            case "Reward":
                loadReward(object);
                break;
            case "Enemy":
                loadEnemy(object);
                break;

        }

    }

    private String getProperty(JsonValue object, String propertyname){
        JsonValue properties = object.get("properties");
        try {
            if (properties != null && !propertyname.equals("x") && !propertyname.equals("y")
                    && !propertyname.equals("height") && !propertyname.equals("width")) {
                for (int i = 0; i < properties.size; i++) {
                    if (properties.get(i).getString("name").equals(propertyname)) {
                        return properties.get(i).getString("value");
                    }
                }
            } else {
                return object.getString(propertyname);
            }
        }catch (IllegalArgumentException e){
            System.err.println("Unable to load object due to unassigned property: "+propertyname);
            return null;
        }
        return null;
    }

    private void loadEnemyArea(JsonValue object){
        String amm = getProperty(object, "SwarmNumber");
        int amount = 100;
        if(amm != null){
            amount = Integer.parseInt(amm);
        }
        String dis = getProperty(object, "distance");
        float distance = 50;
        if(dis != null){
            distance = Float.parseFloat(dis);
        }
        String spd = getProperty(object, "speed");
        float speed = 5f;
        if(spd != null){
            speed = Float.parseFloat(spd);
        }

        String rspwn = getProperty(object, "respawn");
        boolean respawn = true;
        if(rspwn != null){
            respawn = Boolean.parseBoolean(rspwn);
        }

        float height = Float.parseFloat(getProperty(object, "height"))/backgroundScale;



        TriggerZone z = new TriggerZone(
                Float.parseFloat(getProperty(object, "x"))/backgroundScale,
                (mapPixelHeight - Float.parseFloat(getProperty(object, "y"))/backgroundScale) - height,
                Float.parseFloat(getProperty(object, "width"))/backgroundScale,
                height,
                getId(object),
                amount,
                distance,
                speed,
                respawn
        );

        enemyAreas.add(z);
    }

    private void loadEnemy(JsonValue object){
        Vector2 v = getXY(object);
        v.set(v.x/scale.x, v.y/scale.y);
        float radius = getRadius(object);
        int id = getId(object);
        if(!enemies.containsKey(id))
            enemies.put(id, new ArrayList<Enemy>());
        enemies.get(id).add(new Enemy(v, new Vector2[]{}, Vector2.Zero, scale));
    }

    private void loadStaticObstacle(JsonValue object){
        Vector2 v = new Vector2(object.getFloat("x")/backgroundScale/scale.x , (mapPixelHeight - (object.getFloat("y")/backgroundScale))/scale.y);
        String type = getProperty(object, "StaticType");
        if(type != null) {
            switch (type) {
                case "goal":
                    float r = getRadius(object);
                    if(getRadius(object) <= 2){
                        r = 4;
                    }
                    goal = new StaticObstacle(v, r, Vector2.Zero, true);
                    break;
                case "entry":
                    entry = new StaticObstacle(v, 2, Vector2.Zero, true);
                    break;
                case "checkpoint":
                    Checkpoint checkpoint = new Checkpoint(v, Vector2.Zero);
                    checkpoints.add(checkpoint);
                    break;
                case "debris":
                    StaticObstacle debris = new StaticObstacle(v, getRadius(object), Vector2.Zero, true);
                    staticObstacles.add(debris);
                    break;

            }
        }
    }
    private Planet.PlanetColor stringToColor(String c){
        switch (c){
            case "BLUE":
                return Planet.PlanetColor.BLUE;
            case "GREEN":
                return Planet.PlanetColor.GREEN;
            case "PINK":
                return Planet.PlanetColor.PINK;
            case "PURPLE":
                return Planet.PlanetColor.PURPLE;
            case "SUN":
                return Planet.PlanetColor.SUN;
            case "YELLOW":
                return Planet.PlanetColor.YELLOW;
            default:
                return Planet.PlanetColor.BLUE;
        }
    }
    public int getId(JsonValue object){
        String strId = getProperty(object, "id");
        int id = -1;
        if(strId != null) {
            id = Integer.parseInt(strId);
        }
        return id;
    }
    public float getRadius(JsonValue object){
        String strRadius = getProperty(object, "Radius");
        float radius = 1;
        if(strRadius != null) {
            radius = Float.parseFloat(strRadius);
        }
        return radius;
    }

    public Vector2 getXY(JsonValue object){
        Vector2 v = new Vector2();
        v.x = Float.parseFloat(getProperty(object, "x"))/backgroundScale;
        v.y =  (mapPixelHeight - Float.parseFloat(getProperty(object, "y"))/backgroundScale);
        return v;
    }

    private void loadPath(JsonValue object){
        Vector2 v = getXY(object);
        v.set(v.x/scale.x, v.y/scale.y);
        int id = getId(object);
        if(id != -1){
            if(!paths.containsKey(id))
                paths.put(id, new ArrayList<Vector2>());
            paths.get(id).add(v);
        }
    }

    private void loadReward(JsonValue object){
        Vector2 v = getXY(object);
        float radius = getRadius(object);
        v.set(v.x/scale.x, v.y/scale.y);
        int id = getId(object);
        if(!rewards.containsKey(id))
            rewards.put(id, new ArrayList<StaticObstacle>());
        rewards.get(id).add(new StaticObstacle(v, radius, Vector2.Zero, false));
    }

    private void loadDynamicObstacle(JsonValue object){
        int id = getId(object);
        Vector2 v = getXY(object);
        float r = getRadius(object);
        String strPeriod = getProperty(object, "Period");
        float period = 1.0f;
        if(strPeriod != null)
            period = Float.parseFloat(strPeriod);

        DynamicObstacle d = new DynamicObstacle(v, r, new Vector2[]{Vector2.Zero}, period);
        if(dynamicObstacles.get(id) == null) {
            dynamicObstacles.put(id, new ArrayList<DynamicObstacle>());
        }
        dynamicObstacles.get(id).add(d);
    }

    private void parseTileLayer(JsonValue currLayer){
        JsonValue data = currLayer.get("data");
        int[] tLayer = new int[mapHeight*mapWidth];
        for (int j = 0; j < tLayer.length; j++) {
            tLayer[j] = data.getInt(j);
        }
        levelArray.add(tLayer);
    }


    public ArrayList<Planet> getResizables(){
        return resizables;
    }
    public ArrayList<Checkpoint> getCheckpoints(){
        return checkpoints;
    }
    public ArrayList<TriggerZone> getEnemyAreas(){
        for (TriggerZone tz:
             enemyAreas) {
            if(paths.containsKey(tz.id)){
                tz.addDirections(paths.get(tz.id));
            }
        }

        return enemyAreas;
    }
    public ArrayList<StaticObstacle> getStaticObstacles(){
        return staticObstacles;
    }
    public ArrayList<StaticObstacle> getRewards(){
        ArrayList<StaticObstacle> rList = new ArrayList<>();
        ArrayList<ArrayList<StaticObstacle>> a = new ArrayList(rewards.values());
        for (ArrayList<StaticObstacle> r :
                a) {
            rList.addAll(r);
        }
        return rList;
    }
    public ArrayList<DynamicObstacle> getDynamicObstacles(){
        if(!dynamicObstacles.isEmpty()) {
            Integer[] keys = dynamicObstacles.keySet().toArray(new Integer[1]);
            for (int i = 0; i < keys.length; i++) {
                int id = keys[i];
                for (DynamicObstacle d :
                        dynamicObstacles.get(id)) {
                    d.setPath(paths.get(id).toArray(new Vector2[3]));
                }

            }

            ArrayList<DynamicObstacle> dList = new ArrayList<>();
            ArrayList<ArrayList<DynamicObstacle>> a = new ArrayList(dynamicObstacles.values());
            for (ArrayList<DynamicObstacle> dob :
                    a) {
                dList.addAll(dob);
            }
            return dList;
        }else{
            return new ArrayList<DynamicObstacle>();
        }

    }

    public ArrayList<Enemy> getEnemies(){
        if(!enemies.isEmpty()) {
            Integer[] keys = enemies.keySet().toArray(new Integer[1]);
            for (int i = 0; i < keys.length; i++) {
                int id = keys[i];
                for (Enemy e :
                        enemies.get(id)) {

                    if(paths.containsKey(id)) {
                        Vector2[] vec = paths.get(id).toArray(new Vector2[3]);
                        e.setPath(vec);
                    }

                }
            }

            ArrayList<Enemy> eList = new ArrayList<>();
            ArrayList<ArrayList<Enemy>> a = new ArrayList(enemies.values());
            for (ArrayList<Enemy> en :
                    a) {
                eList.addAll(en);
            }
            return eList;
        }else{
            return new ArrayList<Enemy>();
        }

    }


//    public ArrayList<StaticObstacle> getStaticObstacles(){
//        return staticObstacles;
//    }

    public StaticObstacle getEntry(){
        return entry;
    }
    public StaticObstacle getGoal(){
        return goal;
    }

    public void drawLevel(GameCanvas canvas, Vector2 scale, Vector2 offset){


//        for(Rectangle r : enemyAreas){
//            canvas.draw(tilesets.get(0), Color.RED, 0, 0, r.x*scale.x+offset.x, r.y*scale.y+offset.y, r.width*scale.x, r.height*scale.y);
//            System.out.println(r.x*scale.x + ", " + r.y*scale.y);
//            System.out.println(r.width*scale.x + ", " + r.height*scale.y);
//        }
        for (int k = 0; k < levelArray.size(); k++) {
            int[] layer = levelArray.get(k);
            float p_mult = PARALLAX_MIN + (PARALLAX_MAX - PARALLAX_MIN)/levelArray.size()*k;


            if(layer != null) {
                for (int i = 0; i < mapHeight; i++) {
                    for (int j = 0; j < mapWidth; j++) {
                        int tile = layer[i * mapHeight + j];
                        if(tile != -1 && tile != 0) {
                            canvas.draw(tilesets.get(tile - 1), Color.WHITE,
                                    0, 0, j * tileWidth + offset.x * p_mult,
                                    tileHeight * (mapHeight - 1) - i * tileHeight + offset.y * p_mult,
                                    tileWidth, tileHeight);
                        }
                    }
                }
            }

        }

//        for(Rectangle r : enemyAreas){
//            //canvas.draw(tilesets.get(0), Color.RED, 0, 0, r.x+offset.x, r.y+offset.y, r.width, r.height);
//        }

    }
    public String getLevelName(){
        return levelName;
    }

    public void dispose(){
        resizables = new ArrayList<>();
        staticObstacles = new ArrayList<>();
        checkpoints = new ArrayList<>();
        enemyAreas = new ArrayList<>();
        dynamicObstacles = new HashMap<>();
        paths = new HashMap<>();
        rewards = new HashMap<>();
        enemies = new HashMap<>();
    }


}
