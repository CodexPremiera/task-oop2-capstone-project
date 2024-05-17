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
    public enum State {FALLING, JUMPING, STANDING, RUNNING, ATTACKING};
    public State currentState;
    public State previousState;

    public World world;
    public Body body;
    private TextureRegion chickenStand;
    private Animation<?> chickenRun;
    private Animation<?> chickenJump;
    private Animation<?> chickenIdle;
    private Animation<?> chickenAttack;

    private float stateTimer;
    private boolean runningRight;
    private final BodyDef bodyDef;
    private boolean isFaceRight;

    public enum PowerUps {ATTACK, DEFENSE, HEAL, SPEED};
    private boolean isAttacking;
    private float hp;
    private float atkSpeed;
    private float speed;


    public Chicken(World world, PlayScreen screen, TextureAtlas.AtlasRegion chickenAtlasRegion, float posX, float posY, boolean isFaceRight) {
        // get sprite map from screen
        super(chickenAtlasRegion);
        this.world = world;
        this.isFaceRight = isFaceRight;

        // chicken attributes
        this.hp = 0;

        // initialize mario state variables
        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        runningRight = true;

        defineTextures();

        // define CHICKEN player
        this.bodyDef = new BodyDef();
        defineChicken(posX, posY);
        setBounds(0, 0, 48 / CockfightGame.PPM, 48 / CockfightGame.PPM);
        setRegion(chickenStand);
    }

    private void defineTextures() {
        // create an array of texture regions for running animation
        this.chickenRun = defineTexture(680, 4, 20, 20, 23);
        this.chickenJump = defineTexture(420, 5, 32, 32, 32);
        this.chickenIdle = defineTexture(580, 5, 20, 20, 20);
        this.chickenAttack = defineTexture(102, 6, 34, 34, 34);

        // create a texture region for standing
        //this.chickenStand = new TextureRegion(getTexture(), 0, 0, 32, 28);
        this.chickenStand = new TextureRegion(getTexture(), 0, 0, 22, 23);
        chickenStand.flip(!isFaceRight, false);
    }

    private Animation<?> defineTexture(int frameStart, int frameCount, int frameGap, int frameWidth, int frameHeight) {
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < frameCount; i++) {
            TextureRegion region = new TextureRegion(getTexture(), i * frameGap + frameStart, 0, frameWidth, frameHeight);
            region.flip(!this.isFaceRight, false);
            frames.add(region);
        }
        Animation<?> animation = new Animation(0.1f, frames);
        frames.clear();
        return animation;
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
        circleShape.setRadius(36 / 2 / CockfightGame.PPM); // Radius of the circle

        // Set the shape to the fixture definition
        fixtureDef.shape = circleShape;

        // Create the fixture on the body
        body.createFixture(fixtureDef);

        // Set user data if needed
        body.setUserData(this);
    }

    public void update(float dt) {
        // Set the texture region based on the current state
        setRegion(getFrame(dt));

        // Adjust the bounds based on the state
        if (currentState == State.JUMPING) {
            setBounds(getX(), getY(), 76 / CockfightGame.PPM, 76 / CockfightGame.PPM);
            setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2 - 0.1f);
        } else if (currentState == State.ATTACKING) {
            setBounds(getX(), getY(), 76 / CockfightGame.PPM, 76 / CockfightGame.PPM);
            float offset = isFaceRight ? 0.1f : -0.1f;
            setPosition(body.getPosition().x - getWidth() / 2 + offset, body.getPosition().y - getHeight() / 2 - 0.1f);
        } else {
            setBounds(getX(), getY(), 48 / CockfightGame.PPM, 48 / CockfightGame.PPM);
            setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2 + 0.05f);
        }
    }


    public TextureRegion getFrame(float dt) {
        currentState = getState();

        TextureRegion region;
        switch (currentState) {
            case JUMPING:
                region = (TextureRegion) chickenJump.getKeyFrame(stateTimer, true);
                break;
            case RUNNING:
                region = (TextureRegion) chickenRun.getKeyFrame(stateTimer, true);
                break;
            case ATTACKING:
                region = (TextureRegion) chickenAttack.getKeyFrame(stateTimer, true);
                break;
            case STANDING:
            default:
                region = (TextureRegion) chickenIdle.getKeyFrame(stateTimer, true);
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
        if (isAttacking) {
            return State.ATTACKING;
        } else if (body.getLinearVelocity().y > 0 || (body.getLinearVelocity().y < 0 && previousState == State.JUMPING)) {
            return State.JUMPING;
        } else if (body.getLinearVelocity().y < 0) {
            return State.FALLING;
        } else if (body.getLinearVelocity().x != 0) {
            return State.RUNNING;
        } else {
            return State.STANDING;
        }
    }

    public void setAttacking(boolean isAttacking) {
        this.isAttacking = isAttacking;
    }

    public void setIsFaceRight(boolean isFaceRight) {
        this.isFaceRight = isFaceRight;
        defineTextures();
    }

    public float  getHp() {
        return hp;
    }

    public float decreaseHp(int decrement) {
        this.hp -= decrement;
        return hp;
    }
}