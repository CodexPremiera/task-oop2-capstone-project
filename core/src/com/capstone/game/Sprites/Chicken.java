package com.capstone.game.Sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.capstone.game.CockfightGame;

import java.util.Random;

public class Chicken extends Sprite {
    public enum State {DEAD, JUMPING, STANDING, RUNNING, ATTACKING, CHARGING};
    public State currentState;
    public State previousState;

    public World world;
    public Body body;

    private Animation<TextureRegion> chickenRun;
    private Animation<TextureRegion> chickenJump;
    private Animation<TextureRegion> chickenIdle;
    private Animation<TextureRegion> chickenAttack;
    private Animation<TextureRegion> chickenDie;

    private float stateTimer;
    private boolean runningRight;
    private final BodyDef bodyDef;
    private boolean isFaceRight;

    public enum PowerUps {ATTACK, DEFENSE, HEAL, SPEED};
    private boolean isAttacking, isCharging, isJumping, isRunning, isStanding, isDead;
    private float hp;
    private float speed;
    private float damage;
    private long time;
    private boolean isGoingRight;

    private float characterWidth;
    private float characterHeight;

    private final Random random = new Random();

    /* STATISTICS */
    private static final float JUMP_IMPULSE = 1.6f;
    private static final float MOVE_IMPULSE = 0.8f;
    private static final float MAX_SPEED = 3.2f;

    private Chicken(ChickenBuilder builder) {
        // get sprite map from screen
        super(builder.chickenAtlasRegion);
        this.world = builder.world;

        // define chicken stats and states
        this.hp = builder.hp;
        this.isFaceRight = builder.isFaceRight;
        this.damage = builder.damage;
        this.speed = builder.speed;
        this.initializeStates();

        this.bodyDef = new BodyDef();
        this.defineTextures();
        this.defineChicken(builder.posX, builder.posY);
        this.resizeCharacterTexture(72f, 0, 0);
    }

    public static class ChickenBuilder {
        private World world;
        private TextureAtlas.AtlasRegion chickenAtlasRegion;
        private boolean isFaceRight;
        private float posX;
        private float posY;
        private float hp;
        private float damage;
        private float speed;

        public ChickenBuilder(World world, TextureAtlas.AtlasRegion chickenAtlasRegion) {
            this.world = world;
            this.chickenAtlasRegion = chickenAtlasRegion;
            this.isFaceRight = true;
            this.posX = 0;
            this.posY = 0;
            this.hp = 100;
            this.damage = 10;
            this.speed = 1;
        }

        public ChickenBuilder setPosX(float posX) {
            this.posX = posX;
            return this;
        }

        public ChickenBuilder setPosY(float posY) {
            this.posY = posY;
            return this;
        }

        public ChickenBuilder setIsFaceRight(boolean isFaceRight) {
            this.isFaceRight = isFaceRight;
            return this;
        }

        public ChickenBuilder setHp(float hp) {
            this.hp = hp;
            return this;
        }

        public ChickenBuilder setDamage(float damage) {
            this.damage = damage;
            return this;
        }

        public ChickenBuilder setSpeed(float speed) {
            this.speed = speed;
            return this;
        }

        public Chicken build() {
            return new Chicken(this);
        }
    }

    private void defineTextures() {
        // create an array of texture regions for running animation
        this.chickenRun = defineTexture(680, 4, 20, 20, 23);
        this.chickenJump = defineTexture(420, 5, 32, 32, 32);
        this.chickenIdle = defineTexture(580, 5, 20, 20, 20);
        this.chickenAttack = defineTexture(102, 6, 34, 34, 34);
        this.chickenDie = defineTexture(340, 4, 20, 20, 21);
    }

    private Animation<TextureRegion> defineTexture(int frameStart, int frameCount, int frameGap, int frameWidth, int frameHeight) {
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < frameCount; i++) {
            TextureRegion region = new TextureRegion(getTexture(), i * frameGap + frameStart, 0, frameWidth, frameHeight);
            region.flip(!this.isFaceRight, false);
            frames.add(region);
        }
        Animation<TextureRegion> animation = new Animation<>(0.1f, frames);
        frames.clear();
        return animation;
    }

    private void defineChicken(float posX, float posY) {
        // create the body definition and set its position and type
        bodyDef.position.set(posX, posY);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        // create the fixture definition
        initializeFixture(24);
        body.setUserData(this);
    }

    private void initializeStates() {
        // initialize chicken state variables
        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        runningRight = true;

        this.isJumping = false;
        this.isAttacking = false;
        this.isCharging = false;
        this.isRunning = false;
        this.isStanding = true;
        this.isDead = false;
        this.isGoingRight = true;
    }

    private void initializeFixture(float radius) {
        // Create the fixture definition
        FixtureDef fixtureDef = new FixtureDef();
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(radius / CockfightGame.PPM);

        // Set the shape to the fixture definition
        fixtureDef.shape = circleShape;
        body.createFixture(fixtureDef);
    }

    private void setWorld(World world) {
        this.world = world;
    }


    /* IN-GAME METHODS */
    public void update(float dt) {
        TextureRegion region;
        float xOffset = isFaceRight ? 0.1f : -0.1f;
        float smallSize = 72f;
        float bigSize = 96f;

        previousState = currentState;
        currentState = getState();
        switch (currentState) {
            case DEAD:
                region = chickenDie.getKeyFrame(stateTimer, false);
                resizeCharacterTexture(smallSize, xOffset,0.04f);
                break;
            case RUNNING:
            case CHARGING:
                region = chickenRun.getKeyFrame(stateTimer, true);
                resizeCharacterTexture(smallSize, 0,0.0f);
                break;
            case JUMPING:
                region = chickenJump.getKeyFrame(stateTimer, true);
                resizeCharacterTexture(bigSize, xOffset,-0.15f);
                break;
            case ATTACKING:
                region = chickenAttack.getKeyFrame(stateTimer, true);
                resizeCharacterTexture(bigSize, xOffset,-0.2f);
                break;
            case STANDING:
            default:
                region = chickenIdle.getKeyFrame(stateTimer, true);
                resizeCharacterTexture(smallSize, 0,0.08f);
                break;
        }

        if ((body.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()) {
            runningRight = false;
        } else if ((body.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()) {
            runningRight = true;
        }

        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        setRegion(region);
    }

    public State getState() {
        if (hp <= 0) {
            return State.DEAD;
        } else if (isAttacking) {
            return State.ATTACKING;
        } else if (isCharging) {
            return State.CHARGING;
        } else if (body.getLinearVelocity().y > 0 || (body.getLinearVelocity().y < 0 && previousState == State.JUMPING)) {
            return State.JUMPING;
        } else if (body.getLinearVelocity().x != 0) {
            return State.RUNNING;
        } else {
            return State.STANDING;
        }
    }


    private void resizeCharacterTexture(float size, float xOffset, float yOffset) {
        characterWidth = characterHeight = size;
        setBounds(0, 0, characterWidth / CockfightGame.PPM, characterWidth / CockfightGame.PPM);
        setPosition(body.getPosition().x - getWidth() / 2 + xOffset, body.getPosition().y - getHeight() / 2 + yOffset);
    }


    public void setAttacking(boolean isAttacking) {
        this.isAttacking = isAttacking;
    }

    public void setIsFaceRight(boolean isFaceRight) {
        if (this.isFaceRight == isFaceRight || !this.isAlive())
            return;

        this.isFaceRight = isFaceRight;
        defineTextures();
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void decreaseHp(float damage) {
        if (this.hp < 0) return;

        this.hp -= (damage);
        if (this.hp < 0)
            this.hp = 0;
    }

    /* MOVEMENT */
    public void jump() {
        jump(1f);
    }

    public void jump(float jumpMultiplier) {
        if (this.body.getPosition().y < 1.0f && this.getState() != Chicken.State.JUMPING)
            this.body.applyLinearImpulse(
                    new Vector2(0, JUMP_IMPULSE * jumpMultiplier),
                    this.body.getWorldCenter(),
                    true
            );
    }

    public void moveLeft() {
        moveLeft(1f);
    }

    public void moveLeft(float speedMultiplier) {
        if (this.body.getLinearVelocity().x >= -MAX_SPEED && !(this.getState() == Chicken.State.JUMPING)) {
            this.body.applyLinearImpulse(
                    new Vector2(-MOVE_IMPULSE * speedMultiplier * this.speed, 0),
                    this.body.getWorldCenter(),
                    true
            );
        }
    }

    public void moveRight() {
        moveRight(1f);
    }

    public void moveRight(float speedMultiplier) {
        if (this.body.getLinearVelocity().x <= MAX_SPEED && !(this.getState() == Chicken.State.JUMPING)) {
            this.body.applyLinearImpulse(new Vector2(MOVE_IMPULSE * speedMultiplier, 0), this.body.getWorldCenter(), true);
        }
    }

    public void attack(Chicken enemyChicken) {
        if (!this.isAlive() || !enemyChicken.isAlive())
            return;

        this.isAttacking = true;
        float multiplier = 1;

        if (this.body.getPosition().y > enemyChicken.body.getPosition().y)
            multiplier *= 1.8f;
        enemyChicken.decreaseHp(this.damage * multiplier);

        System.out.println(this.hp);
    }

    public void moveBackAndForthLoop(Chicken enemyChicken) {
        if (!this.isAlive() || !enemyChicken.isAlive())
            return;

        while (time > System.currentTimeMillis() + 800) {
            if (isGoingRight) {
                if (isFaceRight)
                    moveRight();
                else
                    moveLeft();
            } else {
                if (isFaceRight)
                    moveLeft();
                else
                    moveRight();
            }
        }

        if (random.nextBoolean() && this.currentState != State.ATTACKING) {
            if (isFaceRight)
                moveRight(2.0f);
            else
                moveLeft(2.0f);
            this.isGoingRight = true;
        } else {
            if (isFaceRight)
                moveLeft(0.5f);
            else
                moveRight(0.5f);
            this.isGoingRight = false;
        }

        time = System.currentTimeMillis();
    }
}