package com.capstone.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.capstone.game.Screens.PlayScreen;
import com.capstone.game.Sprites.Chicken;

public class CockfightGame extends Game {
	public static final int V_WIDTH = 640;
	public static final int V_HEIGHT = 320;
	public static final float PPM = 100;
	public static final float worldWidth = V_WIDTH / PPM;
	public static final float worldHeight = V_HEIGHT / PPM;
	private final float mapWidth = 936 / CockfightGame.PPM;
	private static final float WORLD_GRAVITY = -28f;

	public SpriteBatch spriteBatch;

	@Override
	public void create () {
		spriteBatch = new SpriteBatch();
		World world = new World(new Vector2(0, WORLD_GRAVITY), true);

		// create the chickens
		TextureAtlas chicken1Atlas = new TextureAtlas("chicken_pack/black_chicken.txt");
		TextureAtlas.AtlasRegion chicken1Region = chicken1Atlas.findRegion("chicken_attack");
		Chicken chicken1 = new Chicken.ChickenBuilder(world, chicken1Region)
				.setPosX((2 * mapWidth - worldWidth) / 4f)
				.setPosY(64 / CockfightGame.PPM)
				.setDamage(20)
				.build();

		TextureAtlas chicken2Atlas = new TextureAtlas("chicken_pack/white_chicken.txt");
		TextureAtlas.AtlasRegion chicken2Region = chicken2Atlas.findRegion("chicken_attack");
		Chicken chicken2 = new Chicken.ChickenBuilder(world, chicken2Region)
				.setPosX((2 * mapWidth + worldWidth) / 4f)
				.setPosY(64 / CockfightGame.PPM)
				.setIsFaceRight(false)
				.build();

		PlayScreen playScreen = new PlayScreen(this, world, chicken1, chicken2);
		setScreen(playScreen);
	}

	@Override
	public void render () {
		super.render();
	}
}


