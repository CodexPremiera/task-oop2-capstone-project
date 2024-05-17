package com.capstone.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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

public class PlayScreen implements Screen {
    /* GAME CONSTANTS */
    private static final String MAP_FILE = "arena1_world.tmx";
    private static final float WORLD_GRAVITY = -20f;
    private static final float JUMP_IMPULSE = 8f;
    private static final float MOVE_IMPULSE = 0.4f;
    private static final float MAX_SPEED = 2.8f;
    private static final float TIME_STEP = 1/60f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;


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


    /* CONSTRUCTOR */
    public PlayScreen(CockfightGame game) {
        this.game = game;

        // create the camera used to follow mario through the game world
        this.gameCam = new OrthographicCamera();

        // create a FitViewport to maintain virtual aspect ratio despite screen size
        float worldWidth = CockfightGame.V_WIDTH / CockfightGame.PPM / 2;
        float worldHeight = CockfightGame.V_HEIGHT / CockfightGame.PPM / 2;
        this.gamePort = new FitViewport(worldWidth, worldHeight, gameCam);

        // create the hud for the game
        this.hud = new Hud(game.spriteBatch);

        // load the map and set up the renderers
        loadMap();

        // set the camera to the center of the viewport
        this.gameCam.position.set(gamePort.getWorldWidth() / 2 , gamePort.getWorldHeight() / 2, 0);

        // create the box2D world
        this.world = new World(new Vector2(0, WORLD_GRAVITY), true);
        this.box2DRenderer = new Box2DDebugRenderer();

        // add bodies and fixtures to the world
        new B2WorldCreator(world, map);

        TextureAtlas chicken1Atlas = new TextureAtlas("chicken_pack/l_brown_chicken.txt");
        TextureAtlas.AtlasRegion chicken1Region = chicken1Atlas.findRegion("chicken_attack");
        float chicken1X = worldWidth / 4f;
        float chicken1Y = 64 / CockfightGame.PPM;
        chicken1 = new Chicken(world, this, chicken1Region, chicken1X, chicken1Y, true);

        TextureAtlas chicken2Atlas = new TextureAtlas("chicken_pack/d_brown_chicken.txt");
        TextureAtlas.AtlasRegion chicken2Region = chicken2Atlas.findRegion("chicken_attack");
        float chicken2X = worldWidth * 3 / 4f;
        float chicken2Y = 64 / CockfightGame.PPM;
        chicken2 = new Chicken(world, this, chicken2Region, chicken2X, chicken2Y, false);
        chicken2.setIsFaceRight(false);


        // set the contact listener
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Body bodyA = contact.getFixtureA().getBody();
                Body bodyB = contact.getFixtureB().getBody();

                if ((bodyA.getUserData() instanceof Chicken && bodyB.getUserData() instanceof Chicken)) {
                    Chicken chickenA = (Chicken) bodyA.getUserData();
                    Chicken chickenB = (Chicken) bodyB.getUserData();

                    Vector2 velocityA = chickenA.body.getLinearVelocity();
                    Vector2 velocityB = chickenB.body.getLinearVelocity();

                    float bounceValue = 0.8f;
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
    public void show() {

    }

    public void handleChicken1Input(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.W))
            jumpChicken(chicken1);
        if (Gdx.input.isKeyPressed(Input.Keys.A))
            moveLeft(chicken1);
        if (Gdx.input.isKeyPressed(Input.Keys.D))
            moveRight(chicken1);
    }

    public void handleChicken2Input(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP))
            jumpChicken(chicken2);
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            moveLeft(chicken2);
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            moveRight(chicken2);
    }

    private void jumpChicken(Chicken chicken) {
        if (!(chicken.getState() == Chicken.State.JUMPING))
            chicken.body.applyLinearImpulse(new Vector2(0, JUMP_IMPULSE), chicken.body.getWorldCenter(), true);
    }

    private void moveLeft(Chicken chicken) {
        if (chicken.body.getLinearVelocity().x >= -MAX_SPEED && !(chicken.getState() == Chicken.State.JUMPING))
            chicken.body.applyLinearImpulse(new Vector2(-MOVE_IMPULSE, 0), chicken.body.getWorldCenter(), true);
    }

    private void moveRight(Chicken chicken) {
        if (chicken.body.getLinearVelocity().x <= MAX_SPEED && !(chicken.getState() == Chicken.State.JUMPING))
            chicken.body.applyLinearImpulse(new Vector2(MOVE_IMPULSE, 0), chicken.body.getWorldCenter(), true);
    }

    private float getDistance() {
        return (float) Math.sqrt(Math.pow(chicken1.body.getPosition().x - chicken2.body.getPosition().x, 2) +
                Math.pow(chicken1.body.getPosition().y - chicken2.body.getPosition().y, 2));
    }


    public void update(float dt) {
        handleChicken1Input(dt);
        handleChicken2Input(dt);

        if (getDistance() < 0.8f) {
            chicken1.setAttacking(true);
            chicken2.setAttacking(true);
        } else {
            chicken1.setAttacking(false);
            chicken2.setAttacking(false);
        }

        world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        chicken1.update(dt);
        chicken2.update(dt);

        // update game cam position
        float cameraHalfWidth = gameCam.viewportWidth / 2;
        float mapWidth = map.getProperties().get("width", Integer.class) / CockfightGame.PPM + 2.15f * cameraHalfWidth;
        float chickensPosition = (chicken1.body.getPosition().x + chicken2.body.getPosition().x )/ 2;

        if ( chickensPosition <= cameraHalfWidth )
            gameCam.position.x = cameraHalfWidth;
        else
            gameCam.position.x = Math.min(chickensPosition, mapWidth);

        // check chicken facing direction
        if (chicken1.body.getPosition().x < chicken2.body.getPosition().x) {
            chicken1.setIsFaceRight(true);
            chicken2.setIsFaceRight(false);
        } else {
            chicken1.setIsFaceRight(false);
            chicken2.setIsFaceRight(true);
        }

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