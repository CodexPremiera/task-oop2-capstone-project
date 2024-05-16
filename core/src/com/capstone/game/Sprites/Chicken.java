package com.capstone.game.Sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.capstone.game.CockfightGame;
import com.capstone.game.Screens.PlayScreen;

public class Chicken extends Sprite {
    public enum State {FALLING, JUMPING, STANDING, RUNNING};
    public State currentState;
    public State previousState;

    public World world;
    public Body body;
    private TextureRegion chickenStand;
    private Animation<?> chickenRun;
    private Animation<?> chickenJump;
    private float stateTimer;
    private boolean runningRight;
    private final BodyDef bodyDef;
    private boolean isFaceRight;


    public Chicken(World world, PlayScreen screen, TextureAtlas.AtlasRegion chickenAtlasRegion, float posX, float posY, boolean isFaceRight) {
        // get sprite map from screen
        super(chickenAtlasRegion);
        this.world = world;
        this.isFaceRight = isFaceRight;

        // initialize mario state variables
        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        runningRight = true;

        defineTextures();

        // define mario player
        this.bodyDef = new BodyDef();
        defineChicken(posX, posY);
        setBounds(0, 0, 48 / CockfightGame.PPM, 48 / CockfightGame.PPM);
        setRegion(chickenStand);
    }

    private void defineTextures() {
        // create an array of texture regions for running animation
        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 0; i < 4; i++) {
            TextureRegion region = new TextureRegion(getTexture(), i * 32, 0, 32, 28);
            region.flip(isFaceRight, false);
            frames.add(region);
        }
        this.chickenRun = new Animation(0.1f, frames);
        this.chickenJump = new Animation(0.1f, frames);
        frames.clear();

        // create a texture region for standing
        this.chickenStand = new TextureRegion(getTexture(), 0, 0, 32, 28);
        chickenStand.flip(isFaceRight, false);
    }

    private void defineChicken(float posX, float posY) {
        // Create the body definition and set its position and type
        bodyDef.position.set(posX, posY);
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        // Create the body in the world using the body definition
        body = world.createBody(bodyDef);

        // Create the fixture definition
        FixtureDef fixtureDef = new FixtureDef();

        // Define the circle shape
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(32 / 2 / CockfightGame.PPM); // Radius of the circle

        // Set the shape to the fixture definition
        fixtureDef.shape = circleShape;

        // Create the fixture on the body
        body.createFixture(fixtureDef);

        // Set user data if needed
        body.setUserData(this);
    }

    public void update(float dt) {
        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2 + 0.1f);
        setRegion(getFrame(dt));
    }

    public TextureRegion getFrame(float dt) {
        currentState = getState();

        TextureRegion region;
        switch (currentState) {
            case JUMPING:
                region = (TextureRegion) chickenJump.getKeyFrame(stateTimer);
                break;
            case RUNNING:
                region = (TextureRegion) chickenRun.getKeyFrame(stateTimer, true);
                break;
            case FALLING:
            case STANDING:
            default:
                region = chickenStand;
                break;
        }


        if ((body.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()) {
            runningRight = false;
        } else if ((body.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()) {
            runningRight = true;
        }

        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
        return region;
    }

    public State getState() {
        if (body.getLinearVelocity().y > 0 || (body.getLinearVelocity().y < 0 && previousState == State.JUMPING)) {
            return State.JUMPING;
        } else if (body.getLinearVelocity().y < 0) {
            return State.FALLING;
        } else if (body.getLinearVelocity().x != 0) {
            return State.RUNNING;
        } else {
            return State.STANDING;
        }
    }
    public void setIsFaceRight(boolean isFaceRight) {
        this.isFaceRight = isFaceRight;
        defineTextures();
    }

}
