package com.capstone.game.Tools;

import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.capstone.game.CockfightGame;

public class B2WorldCreator {
    /* LAYER INDICES FROM WORLD.TMX */
    private static final int GROUND_LAYER = 1;

    public B2WorldCreator(World world, TiledMap map) {
        createLayer(world, map, GROUND_LAYER);
    }

    private void createLayer(World world, TiledMap map, int layerIndex) {
        BodyDef bodyDef = new BodyDef();
        PolygonShape polygonShape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        Body body;

        for (RectangleMapObject object : map.getLayers().get(layerIndex).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rectangle = object.getRectangle();

            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set(
                    (rectangle.getX() + rectangle.getWidth() / 2) / CockfightGame.PPM,
                    (rectangle.getY() + rectangle.getHeight() / 2) / CockfightGame.PPM);

            body = world.createBody(bodyDef);

            polygonShape.setAsBox(rectangle.getWidth() / (2 * CockfightGame.PPM), rectangle.getHeight() / (2 * CockfightGame.PPM));
            fixtureDef.shape = polygonShape;
            body.createFixture(fixtureDef);
        }
    }
}