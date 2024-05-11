package com.capstone.game.Sprites;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.capstone.game.CockfightGame;

public abstract class InteractiveTileObject {
    protected World world;
    protected TiledMap map;
    protected TiledMapTile tile;
    protected Rectangle bounds;
    protected Body body;

    public InteractiveTileObject(World world, TiledMap map, Rectangle bounds) {
        this.world = world;
        this.map = map;
        this.bounds = bounds;

        BodyDef bodyDef = new BodyDef();
        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape polygonShape = new PolygonShape();

        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(
                (bounds.getX() + bounds.getWidth() / 2) / CockfightGame.PPM,
                (bounds.getY() + bounds.getHeight() / 2) / CockfightGame.PPM);

        body = world.createBody(bodyDef);

        polygonShape.setAsBox(bounds.getWidth() / (2 * CockfightGame.PPM), bounds.getHeight() / (2 * CockfightGame.PPM));
        fixtureDef.shape = polygonShape;
        body.createFixture(fixtureDef);
    }
}
