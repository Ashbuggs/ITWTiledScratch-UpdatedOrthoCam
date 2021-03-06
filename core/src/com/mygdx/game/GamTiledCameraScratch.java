package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;

//Sources: Orthographic Camera Properties: http://www.gamefromscratch.com/post/2014/04/16/LibGDX-Tutorial-11-Tiled-Maps-Part-1-Simple-Orthogonal-Maps.aspx
          //Translating Orthographic Camera: https://github.com/libgdx/libgdx/wiki/Orthographic-camera
          //Getting TileMap Resolution: http://gamedev.stackexchange.com/questions/57325/how-to-get-width-and-height-of-tiledmap-in-the-latest-version-of-libgdx
          //Clamping Camera: http://gamedev.stackexchange.com/questions/74926/libgdx-keep-camera-within-bounds-of-tiledmap

public class GamTiledCameraScratch extends ApplicationAdapter {
	private static final int nCols = 4;
	private static final int nRows = 4;

	int nMapWidth, nMapHeight, nTileWidth, nTileHeight, nMapTileWidth, nMapTileHeight;
	SpriteBatch sbBatch;
	Texture txSprite;
	TextureAtlas taSprite;
	TextureRegion[] artrFrames;
	TextureRegion trCurrentFrame;
	float fSpriteX = 0;
	float fSpriteY = 0;
	float fSpriteSpeed = 100f;
	float fTime = 0f;
	Animation aniMain;
	TiledMap tmGameMap;
	OrthogonalTiledMapRenderer orthotmrRenderer;
	OrthographicCamera ocMainCam;

	ArrayList<Rectangle> arlRectObjectBounds = new ArrayList<Rectangle>();
	Rectangle rectSprite;
	String sDirection;
	MapProperties mpBounds;

	RectangleMapObject rmoCollisionRect;
	MapObjects moCollisionDetection;
	Rectangle rectObjectBounds;
	@Override
	public void create() {
		rectSprite = new Rectangle();
		sbBatch = new SpriteBatch();
		mpBounds = new MapProperties();
		//BackGround = new Texture(Gdx.files.internal("lostwoods2.jpg"));
		txSprite = new Texture(Gdx.files.internal("CinderellaSpriteSheet.png"));
		taSprite = new TextureAtlas("PackedCinderellaSpriteSheet.pack");
		TextureRegion[][] tmp = TextureRegion.split(txSprite, txSprite.getWidth() / nCols, txSprite.getHeight() / nRows);
		artrFrames = new TextureRegion[nCols * nRows];
		int index = 0;
		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {
				artrFrames[index++] = tmp[i][j];
			}
		}

		aniMain = new Animation(1f, artrFrames);

		//Setting Up Orthographic Camera- define its viewport height and width
		ocMainCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		ocMainCam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		ocMainCam.update();

		//Setting Up TiledMap- load the .tmx file created in Tiled and add it to the Orthogonal Tiled Map Renderer
		tmGameMap = new TmxMapLoader().load("CameraPanMap.tmx");
		orthotmrRenderer = new OrthogonalTiledMapRenderer(tmGameMap);

		//Get the properties of the Tiled map- since we have not defined any properties, this will find the predefined properties
		mpBounds = tmGameMap.getProperties();

		//This section of code is used to help
		nMapWidth = mpBounds.get("width", Integer.class);
		nMapHeight = mpBounds.get("height", Integer.class);
		nTileWidth = mpBounds.get("tilewidth", Integer.class);
		nTileHeight = mpBounds.get("tileheight", Integer.class);

		nMapTileWidth = nMapWidth * nTileWidth;
		nMapTileHeight = nMapHeight * nTileHeight;

		/*tmtlBounds = (TiledMapTileLayer) tmGameMap.getLayers().get("Foreground");
		for (int i = 0; i < tmtlBounds.getWidth(); i++) {
			for (int j = 0; j < tmtlBounds.getHeight(); j++) {
				if (tmtlBounds.getCell(i, j) != null) {
					//tmtlBounds.getProperties().get("Walkable");
					cCurrentCell = tmtlBounds.getCell(i, j);
					objProperty = cCurrentCell.getTile().getProperties().containsKey("Blocked");
					if (objProperty != null) {
						System.out.println("Rectangle added!");
						arlRectCollisionDetection.add(new Rectangle(i * 64, j * 64, 32, 32));
					}
				}
			}
		}*/
		//Get all of the map objects on the collision layer of the Tiled Map
		moCollisionDetection = tmGameMap.getLayers().get("Collision Layer").getObjects();
		//Loop through the objects in the object layer, creating a rectangle from each object's information (height, width, etc)
		for (int i = 0; i < moCollisionDetection.getCount(); i++) {
			rmoCollisionRect = (RectangleMapObject) moCollisionDetection.get(i);
			rectObjectBounds = rmoCollisionRect.getRectangle();
			//Add this rectangle made from the Object into an arraylist
			arlRectObjectBounds.add(rectObjectBounds);
			System.out.println("Rectangle Added!");
		}
	}

	@Override
	public void render() {
		//Rendering Sprite
		if (fTime < 4) {
			fTime += Gdx.graphics.getDeltaTime();
		} else {
			fTime = 0;
		}

		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		trCurrentFrame = aniMain.getKeyFrame(0);
		rectSprite.set(fSpriteX, fSpriteY, trCurrentFrame.getRegionWidth(), trCurrentFrame.getRegionHeight());
		if (Gdx.input.isKeyPressed(Input.Keys.DPAD_LEFT)) {
			fSpriteX -= Gdx.graphics.getDeltaTime() * fSpriteSpeed;
			trCurrentFrame = aniMain.getKeyFrame(4 + fTime);
			sDirection = "Left";
		}
		if (Gdx.input.isKeyPressed(Input.Keys.DPAD_RIGHT)) {
			fSpriteX += Gdx.graphics.getDeltaTime() * fSpriteSpeed;
			trCurrentFrame = aniMain.getKeyFrame(8 + fTime);
			sDirection = "Right";
		}
		if (Gdx.input.isKeyPressed(Input.Keys.DPAD_UP)) {
			fSpriteY += Gdx.graphics.getDeltaTime() * fSpriteSpeed;
			//System.out.println("Player Sprite Y:" + fSpriteY);
			trCurrentFrame = aniMain.getKeyFrame(12 + fTime);
			sDirection = "Up";
		}
		if (Gdx.input.isKeyPressed(Input.Keys.DPAD_DOWN)) {
			fSpriteY -= Gdx.graphics.getDeltaTime() * fSpriteSpeed;
			trCurrentFrame = aniMain.getKeyFrame(0 + fTime);
			sDirection = "Down";
		}

		ocMainCam.position.set(fSpriteX, fSpriteY, 0);
		sbBatch.setProjectionMatrix(ocMainCam.combined);
		ocMainCam.position.x = MathUtils.clamp(ocMainCam.position.x, 0 + (Gdx.graphics.getWidth() / 2), nMapTileWidth - (Gdx.graphics.getWidth() / 2));
		ocMainCam.position.y = MathUtils.clamp(ocMainCam.position.y, 0 + (Gdx.graphics.getHeight() / 2), nMapTileHeight - (Gdx.graphics.getHeight() / 2));
		ocMainCam.update();

		fSpriteX = MathUtils.clamp(fSpriteX, 0 + 16, nMapTileWidth - 16);
		fSpriteY = MathUtils.clamp(fSpriteY, 0 + 32, nMapTileHeight -32);

		//Rendering Tiled Map
		orthotmrRenderer.setView(ocMainCam);
		orthotmrRenderer.render();
		ocMainCam.update();

		//Draw Sprites
		sbBatch.begin();
		//batch.draw(BackGround, 0, 0);
		sbBatch.draw(trCurrentFrame, (int) fSpriteX, (int) fSpriteY);
		sbBatch.end();

		//Check through all of the rectangles in the Arraylist of Rectangles
		for (int i = 0; i < arlRectObjectBounds.size(); i++) {
			if(rectSprite.overlaps(arlRectObjectBounds.get(i))) { //Checking to see if the sprite rectangle intersects any of the rectangles in the arraylist of rectangles from the object layer
				//Using the Sprite's current direction, reposition the Sprite accordingly
				if(sDirection == "Up"){
					//fSpriteY -= 5f;
					fSpriteY -= Gdx.graphics.getDeltaTime() * fSpriteSpeed;
				}
				else if(sDirection == "Down"){
					//fSpriteY += 5f;
					fSpriteY += Gdx.graphics.getDeltaTime() * fSpriteSpeed;
				}
				else if(sDirection == "Right") {
					//fSpriteX -= 5f;
					fSpriteX -= Gdx.graphics.getDeltaTime() * fSpriteSpeed;
				}
				else if(sDirection == "Left"){
					//fSpriteX += 5f;
					fSpriteX += Gdx.graphics.getDeltaTime() * fSpriteSpeed;
				}

			}

		}

	}
}


