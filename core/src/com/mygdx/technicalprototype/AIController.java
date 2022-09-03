package com.mygdx.technicalprototype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.mygdx.technicalprototype.AssetController.enemyTexture;

public class AIController {
    private ArrayList<Enemy> enemies;
    private Rocket ship;
    private Vector2 scale;
    private JsonValue defaults;

    /*
     * Swarm variables
     */
    public boolean inPursuit = false;
    public Vector2 directionFrom;
    public Vector2 directionTo;
    private ArrayList<Enemy> swarm;
    private ArrayList<Swarm> swarmList = new ArrayList<>();
    private int swarmNum;
    private int currSwarm;


    ArrayList<Enemy> enemiesToAdd = new ArrayList<>();

    private static final float MAX_STUN_TIME = 0.85f;
    private float stunTime;

    private Random random = new Random();



    public AIController(ArrayList<Enemy> es, Rocket s, JsonValue defaults, Vector2 scale) {
        enemies = es;
        ship = s;
        this.defaults = defaults;
        this.scale = scale;
        stunTime = 0;


        for (Enemy e : enemies) {
            e.setDrawScale(scale);
            e.setBodyType(BodyDef.BodyType.DynamicBody);
            e.setGravityScale(0);
            e.setDensity(defaults.getFloat("density", 0.0f));
            e.setFriction(defaults.getFloat("friction", 0.0f));
            e.setRestitution(defaults.getFloat("restitution", 0.0f));
            e.setTexture(enemyTexture);
            e.setVelocity(new Vector2());
            e.setSensor(false);
            e.setName("enemy");
            e.setLocation(s.getPos());
        }
        currSwarm+=enemies.size();
    }

    /**
     * Returns whether there are enemies being controlled and removes any
     * enemies from the list that were removed from the level.
     *
     * @return whether there are enemies being controlled
     */
    public boolean hasEnemies() {
        return enemies.size() > 0;
    }

    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    public void removeEnemy(Enemy e) {
        e.markRemoved(true);
        enemies.remove(e);
        currSwarm--;
    }

    public Enemy addEnemy(Vector2 position, float speed){
        currSwarm++;
        Enemy e = new Enemy(position, new Vector2[]{}, ship.getPos(), scale, speed);
        enemies.add(e);
        e.setDrawScale(scale);
        e.setBodyType(BodyDef.BodyType.DynamicBody);
        e.setGravityScale(0);
        e.setDensity(defaults.getFloat("density", 0.0f));
        e.setFriction(defaults.getFloat("friction", 0.0f));
        e.setRestitution(defaults.getFloat("restitution", 0.0f));
        e.setTexture(enemyTexture);
        e.setVelocity(new Vector2());
        e.setSensor(false);
        e.setName("enemy");
        e.setLocation(ship.getPos());
        return e;
    }

//    public ArrayList<Enemy> addSwarm(Vector2 position, int amount){
//        Random r = new Random();
//        ArrayList<Enemy> el = new ArrayList<>();
//        for(int i = 0; i <= amount; i++){
//
//            el.add(addEnemy(position.cpy().add(r.nextFloat(), r.nextFloat())));
//        }
//        return el;
//    }

    public void triggerSwarm(Vector2 directionFrom, int amount, float distance, float speed, boolean respawn){
        Swarm swarm = new Swarm(amount, speed, respawn);
        swarm.screenCoord.set(screenEdge(directionFrom, distance));
        swarm.worldCoord.set(toRelativePoint(swarm.screenCoord.cpy(), swarm.screenCoord));
        inPursuit = true;
        swarmList.add(swarm);
        swarmNum+=amount;
        //swarm = addSwarm(worldPoint, amount);
        //return swarm;
    }

    public Vector2 toRelativePoint(Vector2 p, Vector2 edgePoint){
        return p.set((edgePoint.x - Gdx.graphics.getWidth()/2 + ship.getPos().x)/scale.x, (edgePoint.y- Gdx.graphics.getHeight()/2 + ship.getPos().y)/scale.y);

    }

    public void removeAllEnemy() {
        inPursuit = false;
        for(Enemy i : enemies){
            i.markRemoved(true);
        }
        enemies = new ArrayList<>();
    }

    /**
     * Updates ai.
     * @return Returns either newly added enemies or null
     */
    public ArrayList<Enemy> update() {
        if(this.hasEnemies()){
            for (Enemy e : enemies) {
                e.nextMove(ship.getPos());
                updateState(e);
            }
        }

            enemiesToAdd.clear();


            if (inPursuit && currSwarm < swarmNum) {
                int rand = random.nextInt(swarmList.size());
                Swarm s = swarmList.get(rand);

                if(s.respawn || s.initAmmount < swarmNum) {
                    s.worldCoord.set(
                            (s.screenCoord.x - Gdx.graphics.getWidth() / 2 +
                                    ship.getPos().x) / scale.x,
                            (s.screenCoord.y - Gdx.graphics.getHeight() / 2 +
                                    ship.getPos().y) / scale.y);
                    enemiesToAdd.add(addEnemy(s.worldCoord, s.speed));
                    enemiesToAdd.add(addEnemy(s.worldCoord, s.speed));
                    enemiesToAdd.add(addEnemy(s.worldCoord, s.speed));
                    s.initAmmount+=3;
                }

            }



        if (enemiesToAdd.size() > 0) {
            return enemiesToAdd;
        }
        return null;

    }

    private void updateState(Enemy enemy) {
        switch (enemy.getState()) {
            case ATTACK:
                if (enemy.isGravitating()) {
                    enemy.setState(Enemy.State.ESCAPE, null);
                } else if (enemy.isPatroller() && !enemy.detectsShip(ship.getPos())) {
//                    enemy.setState(Enemy.State.PATROL, ship.getPos());
                }
                break;
            case ESCAPE:
                if (!enemy.isGravitating()) {
                    if (!enemy.isPatroller() || enemy.detectsShip(ship.getPos())) {
                        enemy.setState(Enemy.State.ATTACK, null);
                    } else {
                        enemy.setState(Enemy.State.ATTACK, ship.getPos());
                    }
                }
                break;
            case PATROL:
                assert (!enemy.isPatroller());
                if (enemy.isGravitating()) {
                    enemy.setState(Enemy.State.ESCAPE, null);
                } else if (enemy.detectsShip(ship.getPos())) {
                    enemy.setState(Enemy.State.ATTACK, null);
                }
                break;
            case STUN:
                if (stunTime < MAX_STUN_TIME) {
                    stunTime += Gdx.graphics.getDeltaTime() * 3; // hardcoded rate
                    enemy.setClampVelocity(false);
                } else {
                    stunTime = 0;
                    enemy.setState(Enemy.State.ATTACK, ship.getPos());
                    enemy.setClampVelocity(true);
                }
        }
    }

    public void updateGravity(ArrayList<Planet> resizables) {
        Vector2 scale = ship.getDrawScale();

        for (Enemy e : enemies) {
            boolean grav = false;
            for (Planet p : resizables) {
                if (p.getPosition().dst(e.getPosition()) < 1.25*p.getRadius() / scale.x + e.getRadius()) {
                    grav = true;
                    applyGravity(e, p.getPosition());

                    if (!e.isGravitating()) {
                        Vector2 destination = new Vector2(e.getPosition().x-p.getPosition().x,e.getPosition().y-p.getPosition().y);
                        destination.nor();
                        destination.scl(p.getRadius());
                        destination.add(p.getPosition());
                        e.setDestination(destination);
                    }
                    break;
                }
            }
            e.setGravitating(grav);
        }
    }

//    public static Vector2 screenEdge(Vector2 point, float distance) {
//        Vector2 edge;
//        float d1 = point.dst(new Vector2(1000, point.y));
//        float d2 = point.dst(new Vector2(point.x, Gdx.graphics.getHeight()/2));
//        float d3 = point.dst(new Vector2(0, point.y));
//        float d4 = point.dst(new Vector2(point.x, 0));
//        Array<Float> dSort = new Array<>();
//        dSort.add(d1, d3, d3, d4);
//        dSort.sort();
//        float smallest = dSort.get(0).floatValue();
//
//        if (d1 == smallest) {
//            edge = new Vector2(Gdx.graphics.getWidth() + distance, point.y);
//        } else if (d2 == smallest) {
//            edge = new Vector2(point.x, Gdx.graphics.getHeight() + distance);
//        } else if (d4 == smallest) {
//            edge = new Vector2(point.x, 0 - distance);
//        } else {
//            edge = new Vector2(0 - distance, point.y);
//            //edge = new Vector2(point.x, 0);
//        }
//
//        return edge;
//    }

    public static Vector2 screenEdge(Vector2 point, float distance) {
        float dx = Gdx.graphics.getWidth()/2 - point.x;
        float dy = Gdx.graphics.getHeight()/2 - point.y;
        boolean xBigger = dx*dx >= dy*dy;

        if(dx < 0 && dy < 0){
            if(xBigger){
                return point.set(Gdx.graphics.getWidth() + distance, point.y);
            }else{
                return point.set(point.x, Gdx.graphics.getHeight() + distance);
            }

        }else if(dx < 0 && dy > 0){
            if(xBigger){
                return point.set(Gdx.graphics.getWidth() + distance, point.y);
            }else{
                return point.set(point.x, 0- distance);
            }

        }else if(dx > 0 && dy < 0){
            if(xBigger){
                return point.set(0- distance, point.y);
            }else{
                return point.set(point.x, Gdx.graphics.getHeight() + distance);
            }
        }else if(dx > 0 && dy > 0){
            if(xBigger){
                return point.set(0- distance, point.y);
            }else{
                return point.set(point.x, 0- distance);
            }
        }else{
            return point.set(0- distance, point.y);
        }




    }

    public void applyGravity(Enemy e, Vector2 location) {
        Vector2 force = new Vector2(location);
        force.sub(e.getPosition()).nor().scl(e.getMass());
        e.getBody().applyForce(force, e.getPosition(), true);

    }
}
