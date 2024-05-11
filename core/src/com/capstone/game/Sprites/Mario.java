package com.capstone.game.Sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.capstone.game.CockfightGame;
import com.capstone.game.screens.PlayScreen;

public class Mario extends Sprite {
    public enum State {FALLING, JUMPING, STANDING, RUNNING};
    public State currentState;
    public State previousState;

    public World world;
    public Body body;
    private TextureRegion marioStand;
    private Animation marionRun;
    private Animation marioJump;
    private float stateTimer;
    private boolean runningRight;


    public Mario(World world, PlayScreen screen) {
        // get sprite map from screen
        super(screen.getAtlas().findRegion("little_mario"));
        this.world = world;

        // initialize mario state variables
        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        runningRight = true;

        // create an array of texture regions for running animation
        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(getTexture(), i * 16, 0, 16, 28));
        }
        marionRun = new Animation(0.1f, frames);
        frames.clear();

        // create an array of texture regions for jumping animation
        for (int i = 4; i < 6; i++) {
            frames.add(new TextureRegion(getTexture(), i * 16, 0, 16, 28));
        }
        marioJump = new Animation(0.1f, frames);


        // define mario player
        defineMario();
        this.marioStand = new TextureRegion(getTexture(), 0, 0, 16, 28);
        setBounds(0, 0, 16 / CockfightGame.PPM, 16 / CockfightGame.PPM);
        setRegion(marioStand);
    }

    private void defineMario() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(64 / CockfightGame.PPM, 64 / CockfightGame.PPM);
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        body = world.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(10/ CockfightGame.PPM);

        fixtureDef.shape = shape;
        body.createFixture(fixtureDef);
    }

    public void update(float dt) {
        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
        setRegion(getFrame(dt));
    }

    public TextureRegion getFrame(float dt) {
        currentState = getState();

        TextureRegion region;
        switch (currentState) {
            case JUMPING:
                region = (TextureRegion) marioJump.getKeyFrame(stateTimer);
                break;
            case RUNNING:
                region = (TextureRegion) marionRun.getKeyFrame(stateTimer, true);
                break;
            case FALLING:
            case STANDING:
            default:
                region = marioStand;
                break;
        }

        if ((body.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()) {
            region.flip(true, false);
            runningRight = false;
        } else if ((body.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()) {
            region.flip(true, false);
            runningRight = true;
        }

        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
        return region;
    }

    private State getState() {
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
}
