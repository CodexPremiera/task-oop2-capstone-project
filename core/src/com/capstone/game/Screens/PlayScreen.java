package com.capstone.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.capstone.game.CockfightGame;
import com.capstone.game.Scenes.Hud;
import com.capstone.game.Sprites.Chicken;
import com.capstone.game.Tools.B2WorldCreator;

import java.util.Random;

public class PlayScreen implements Screen {
    /* GAME CONSTANTS */
    private static final String MAP_FILE = "cockpit/cockpit-new.tmx";
    private static final float TIME_STEP = 1/60f;
    private static final int VELOCITY_ITERATIONS = 12;
    private static final int POSITION_ITERATIONS = 4;
    private static long time = System.currentTimeMillis();


    /* CAMERA ATTRIBUTES */
    private final OrthographicCamera gameCam;
    private final Viewport gamePort;
    private final Hud hud;


    /* TILED MAP ATTRIBUTES */
    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;


    /* BOX 2D ATTRIBUTES */
    private World world;
    private Box2DDebugRenderer box2DRenderer;


    /* GAME ATTRIBUTES */
    private final CockfightGame game;
    private Chicken chicken1;
    private Chicken chicken2;
    private final Random random = new Random();


    /* CONSTRUCTOR */
    public PlayScreen(CockfightGame game, World world, Chicken chicken1, Chicken chicken2) {
        this.game = game;

        // create the camera used to follow mario through the game world
        this.gameCam = new OrthographicCamera();

        // create a FitViewport to maintain virtual aspect ratio despite screen size


        this.gamePort = new FitViewport(CockfightGame.worldWidth, CockfightGame.worldHeight, gameCam);

        // create the hud and load the map
        this.hud = new Hud(game.spriteBatch);
        loadMap();

        // set the camera to the center of the viewport
        this.gameCam.position.set(gamePort.getWorldWidth() / 2 , gamePort.getWorldHeight() / 2, 0);

        // create the box2D world
        this.world = world;
        this.box2DRenderer = new Box2DDebugRenderer();
        new B2WorldCreator(world, map);

        // set chickens
        this.chicken1 = chicken1;
        this.chicken2 = chicken2;


        // set the contact listener
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Body bodyA = contact.getFixtureA().getBody();
                Body bodyB = contact.getFixtureB().getBody();

                if ((bodyA.getUserData() instanceof Chicken && bodyB.getUserData() instanceof Chicken)) {
                    if (random.nextBoolean()) {
                        chicken1.attack(chicken2);
                        chicken2.attack(chicken1);
                    }
                    else {
                        chicken2.attack(chicken1);
                        chicken1.attack(chicken2);
                    }

                    Chicken chickenA = (Chicken) bodyA.getUserData();
                    Chicken chickenB = (Chicken) bodyB.getUserData();

                    Vector2 velocityA = chickenA.body.getLinearVelocity();
                    Vector2 velocityB = chickenB.body.getLinearVelocity();



                    float bounceValue = 0.2f;
                    Vector2 bounceImpulseA = new Vector2(velocityB.x - velocityA.x, velocityB.y - velocityA.y).scl(bounceValue);
                    Vector2 bounceImpulseB = new Vector2(velocityA.x - velocityB.x, velocityA.y - velocityB.y).scl(bounceValue);

                    chickenA.body.applyLinearImpulse(bounceImpulseA, chickenA.body.getWorldCenter(), true);
                    chickenB.body.applyLinearImpulse(bounceImpulseB, chickenB.body.getWorldCenter(), true);
                }
            }

            @Override
            public void endContact(Contact contact) {
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });
    }

    private void loadMap() {
        this.mapLoader = new TmxMapLoader();
        try {
            this.map = this.mapLoader.load(MAP_FILE);
        } catch (Exception e) {
            System.out.println("Error loading map: " + e.getMessage());
            return;
        }
        this.renderer = new OrthogonalTiledMapRenderer(this.map, 1 / CockfightGame.PPM);
    }


    @Override
    public void show() {}

    public void handleChicken1Input() {
        if (!chicken1.isAlive())
            return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.W))
            chicken1.jump();
        if (Gdx.input.isKeyPressed(Input.Keys.A))
            chicken1.moveLeft();
        if (Gdx.input.isKeyPressed(Input.Keys.D))
            chicken1.moveRight();
    }

    public void handleChicken2Input() {
        if (!chicken2.isAlive())
            return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP))
            chicken2.jump();
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            chicken2.moveLeft();
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            chicken2.moveRight();
    }


    private float getDistance() {
        return (float) Math.sqrt(Math.pow(chicken1.body.getPosition().x - chicken2.body.getPosition().x, 2) +
                Math.pow(chicken1.body.getPosition().y - chicken2.body.getPosition().y, 2));
    }

    public void fight() {

    }


    public void update(float dt) {
        // Call the fight method to automate chicken movement

        if (random.nextBoolean()) {
            chicken2.moveBackAndForthLoop(chicken1);
            chicken1.moveBackAndForthLoop(chicken2);
        } else {
            chicken1.moveBackAndForthLoop(chicken2);
            chicken2.moveBackAndForthLoop(chicken1);
        }

        handleChicken2Input();
        handleChicken1Input();


        if (getDistance() < 1.6f && chicken1.isAlive() && chicken2.isAlive()) {
            if (random.nextBoolean()) {
                chicken2.jump();
                chicken1.jump();
            } else {
                chicken1.jump();
                chicken2.jump();
            }

            if (getDistance() < 1.0f) {
                chicken1.setAttacking(true);
                chicken2.setAttacking(true);
            }
        } else {
            chicken1.setAttacking(false);
            chicken2.setAttacking(false);
        }

        // check chicken facing direction
        if (chicken1.body.getPosition().x < chicken2.body.getPosition().x) {
            chicken1.setIsFaceRight(true);
            chicken2.setIsFaceRight(false);
        } else {
            chicken1.setIsFaceRight(false);
            chicken2.setIsFaceRight(true);
        }

        world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        chicken1.update(dt);
        chicken2.update(dt);

        // update game cam position
        float cameraHalfWidth = gameCam.viewportWidth / 2;
        float mapWidth = map.getProperties().get("width", Integer.class) / CockfightGame.PPM + 1.90f * cameraHalfWidth;
        float chickensPosition = (chicken1.body.getPosition().x + chicken2.body.getPosition().x )/ 2;

        if ( chickensPosition <= cameraHalfWidth )
            gameCam.position.x = cameraHalfWidth;
        else
            gameCam.position.x = Math.min(chickensPosition, mapWidth);

        // update game cam to correct coordinates after changes
        gameCam.update();

        // tell the renderer to draw only what the camera can see in the game world
        renderer.setView(gameCam);
    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height);
    }

    @Override
    public void render(float delta) {
        // separate the update logic from the render logic
        update(delta);

        // render game map
        clearScreen();
        renderer.render();

        // render box2d world
        box2DRenderer.render(world, gameCam.combined);

        // draw the player texture at the center of the screen
        game.spriteBatch.setProjectionMatrix(gameCam.combined);
        game.spriteBatch.begin();
        chicken1.draw(game.spriteBatch);
        chicken2.draw(game.spriteBatch);
        game.spriteBatch.end();

        // follow mario with the camera
        game.spriteBatch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        world.dispose();
        box2DRenderer.dispose();
        hud.dispose();
    }
}