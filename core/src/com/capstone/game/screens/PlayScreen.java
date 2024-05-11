package com.capstone.game.screens;

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
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.capstone.game.CockfightGame;
import com.capstone.game.Scenes.Hud;
import com.capstone.game.Sprites.Mario;
import com.capstone.game.Tools.B2WorldCreator;

public class PlayScreen implements Screen {
    /* GAME ATTRIBUTES */
    private final CockfightGame game;
    private TextureAtlas atlas;

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
    private Mario player;

    /* GAME CONSTANTS */
    private static final String MAP_FILE = "world.tmx";
    private static final float WORLD_GRAVITY = -10f;
    private static final float JUMP_IMPULSE = 4f;
    private static final float MOVE_IMPULSE = 0.1f;
    private static final float MAX_SPEED = 2;
    private static final float TIME_STEP = 1/60f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;

    /* CONSTRUCTOR */
    public PlayScreen(CockfightGame game) {
        this.game = game;
        this.atlas = new TextureAtlas("sprites/mario_sprites.txt");

        // create the camera used to follow mario through the game world
        gameCam = new OrthographicCamera();

        // create a FitViewport to maintain virtual aspect ratio despite screen size
        gamePort = new FitViewport((float) CockfightGame.V_WIDTH / CockfightGame.PPM / 2, (float) CockfightGame.V_HEIGHT / CockfightGame.PPM / 2, gameCam);

        // create the hud for the game
        hud = new Hud(game.spriteBatch);

        // load the map and set up the renderer
        mapLoader = new TmxMapLoader();
        try {
            map = mapLoader.load(MAP_FILE);
        } catch (Exception e) {
            System.out.println("Error loading map: " + e.getMessage());
            return;
        }
        renderer = new OrthogonalTiledMapRenderer(map, 1 / CockfightGame.PPM);

        // set the camera to the center of the viewport
        gameCam.position.set(gamePort.getWorldWidth() / 2, gamePort.getWorldHeight() / 2, 0);

        // create the box2d world
        world = new World(new Vector2(0, WORLD_GRAVITY), true);
        box2DRenderer = new Box2DDebugRenderer();

        // add bodies and fixtures to the world
        new B2WorldCreator(world, map);
        this.player = new Mario(world, this);
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

    public TextureAtlas getAtlas() {
        return atlas;
    }

    @Override
    public void show() {

    }

    public void handleInput(float dt) {
        handleJump();
        handleMoveLeft();
        handleMoveRight();
    }

    private void handleJump() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.W))
            player.body.applyLinearImpulse(new Vector2(0, JUMP_IMPULSE), player.body.getWorldCenter(), true);
    }

    private void handleMoveLeft() {
        if (Gdx.input.isKeyPressed(Input.Keys.A) && player.body.getLinearVelocity().x >= -MAX_SPEED)
            player.body.applyLinearImpulse(new Vector2(-MOVE_IMPULSE, 0), player.body.getWorldCenter(), true);
    }

    private void handleMoveRight() {
        if (Gdx.input.isKeyPressed(Input.Keys.D) && player.body.getLinearVelocity().x <= MAX_SPEED)
            player.body.applyLinearImpulse(new Vector2(MOVE_IMPULSE, 0), player.body.getWorldCenter(), true);
    }


    public void update(float dt) {
        handleInput(dt);

        world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        player.update(dt);

        // update mario's position
        gameCam.position.x = player.body.getPosition().x;

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
        player.draw(game.spriteBatch);
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