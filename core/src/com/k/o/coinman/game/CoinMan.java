package com.k.o.coinman.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;


import java.util.ArrayList;
import java.util.Random;

public class CoinMan extends ApplicationAdapter {
	SpriteBatch batch;
	Texture background;
	Texture[] man;
	Texture bomb;
	int gameState = 0;
	int score = 0;
	int manState = 0;
	int pause = 0;
	float gravity = 1f;
	float velocity = 0;
	int manY = 500;
	Rectangle manRect;
	BitmapFont font;

	//button info
	Stage stage;
	TextButton difficulty;
	TextButton.TextButtonStyle textButtonStyle;
	BitmapFont buttonFont;
	Skin skin;
	TextureAtlas buttonAtlas;
	int difficultyLvl = 5;
	float gyroY;


	//bomb data
	ArrayList<Integer> bombX;
	ArrayList<Integer> bombY;
	int bombSpeed = 30;
	int bombCount = 0;
	ArrayList<Rectangle> bombRect;

	//coin data
	Texture coin;
	ArrayList<Integer> coinX;
	ArrayList<Integer> coinY;
	int coinSpeed = 20;
	int coinCount = 0;
	ArrayList<Rectangle> coinRect;

	Random random;
	Preferences preferences;

	@Override
	public void create () {
		batch = new SpriteBatch();

		/////////////////////
		/*
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		buttonFont = new BitmapFont();
		skin = new Skin();
		buttonAtlas = new TextureAtlas(Gdx.files.internal("assets"));
		skin.addRegions(buttonAtlas);
		textButtonStyle = new TextButton.TextButtonStyle();
		textButtonStyle.font = buttonFont;
		textButtonStyle.up = skin.getDrawable("button.png");
		textButtonStyle.down = skin.getDrawable("button.png");
		textButtonStyle.checked = skin.getDrawable("button.png");
		difficulty = new TextButton("Button1",textButtonStyle);
		stage.addActor(difficulty);
		*/

		///////////////////////

		background = new Texture("bg.png");
		font = new BitmapFont();
		font.setColor(Color.BLACK);
		font.getData().setScale(10);
		boolean gyroscopeAvail = Gdx.input.isPeripheralAvailable(Input.Peripheral.Gyroscope);
		Gdx.app.log("gyroWorking?",String.valueOf(gyroscopeAvail));

		man = new Texture[4];
		man[0] = new Texture("frame-1.png");
		man[1] = new Texture("frame-1.png");
		man[2] = new Texture("frame-3.png");
		man[3] = new Texture("frame-4.png");
		manRect = new Rectangle();

		bomb = new Texture("bomb.png");
		bombX = new ArrayList<>();
		bombY = new ArrayList<>();
		bombRect = new ArrayList<>();

		coin = new Texture("coin.png");
		coinX = new ArrayList<>();
		coinY = new ArrayList<>();
		coinRect = new ArrayList<>();

		random = new Random();

	}

	public void makeCoin(){
		float height = random.nextFloat() * Gdx.graphics.getHeight();
		coinY.add((int) height);
		coinX.add(Gdx.graphics.getWidth());

	}

	public void makeBomb(){
		float height = random.nextFloat() * Gdx.graphics.getHeight();
		bombY.add((int) height);
		bombX.add(Gdx.graphics.getWidth());
	}

	@Override
	public void render () {
		batch.begin();
		batch.draw(background,0,0, Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
		if(gameState == 1){
			//Main game
			//coin gen
			if(coinCount < 50){
				coinCount ++;
			}
			else{
				makeCoin();
				coinCount = 0;
			}

			//generowanie bomby
			if(bombCount < 60){
				bombCount++;
			}
			else{
				makeBomb();
				bombCount = 0;
			}

			//draw bomb
			bombRect.clear();
			for(int i = 0; i < bombX.size(); ++i){
				batch.draw(bomb,bombX.get(i),bombY.get(i));
				bombX.set(i,bombX.get(i) - bombSpeed);
				bombRect.add(new Rectangle(bombX.get(i),bombY.get(i), bomb.getWidth(),bomb.getHeight()));
			}

			//draw coin
			coinRect.clear();
			for(int i = 0; i < coinY.size(); ++i){
				batch.draw(coin,coinX.get(i),coinY.get(i));
				coinX.set(i,coinX.get(i) - coinSpeed);
				coinRect.add(new Rectangle(coinX.get(i),coinY.get(i),coin.getWidth(),coin.getHeight()));
			}



			//skakanie i opadanie
			if(Gdx.input.justTouched()){
				velocity = -35;
			}

			if(pause < 1){
				pause ++;
			}else {
				if (manState < 3) {
					++manState;
				} else {
					manState = 0;
				}
				pause = 0;
			}
			velocity += gravity;

			manY -= velocity;

			if(manY < 0){
				manY = 0;
			}
			///////////////

			//check how many coins had been collected

			batch.draw(man[manState], Gdx.graphics.getWidth() / 2 - man[manState].getWidth() / 2, manY, Gdx.graphics.getWidth() / 5, Gdx.graphics.getHeight() / 5);
			manRect = new Rectangle(Gdx.graphics.getWidth() / 2 - man[manState].getWidth() / 2,manY,Gdx.graphics.getWidth() / 5, Gdx.graphics.getHeight() / 5);

			for(int i= 0; i < coinRect.size(); ++i){
				if(Intersector.overlaps(manRect,coinRect.get(i))){
					score += 1 * difficultyLvl;
					coinRect.remove(i);
					coinX.remove(i);
					coinY.remove(i);
					break;
					//Gdx.app.log("Coin","collision");
					//Gdx.input.vibrate(100);
				}
			}
			for(int i = 0; i < bombRect.size(); ++i){
				if(Intersector.overlaps(manRect,bombRect.get(i))){
					gameState = -1;
					//Gdx.app.log("bomb","collision");
					//Gdx.app.exit();
				}
			}
			font.draw(batch,String.valueOf(score),10,Gdx.graphics.getHeight() - 10);

		}
		else if(gameState == 0){
			//start of the game
			font.getData().setScale(3);
			font.draw(batch,"Difficulty: " + String.valueOf(difficultyLvl),10,100);
			/*difficulty.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					if(difficultyLvl == 5){
						difficultyLvl = 1;
					}
					else{
						difficultyLvl++;
					}
				}
			});*/
			Gdx.app.log("Gyro", String.valueOf(Gdx.input.getGyroscopeY()));
			if(Gdx.input.getGyroscopeY() != 0){
				gyroY = Gdx.input.getGyroscopeY();
				if(Gdx.input.getGyroscopeY() < -2){
					--difficultyLvl;
				}else
				if(Gdx.input.getGyroscopeY() > 2){
					difficultyLvl++;
				}
				if(difficultyLvl > 5){
					difficultyLvl = 1;
				}
				if(difficultyLvl < 1){
					difficultyLvl = 5;
				}

			}
			switch (difficultyLvl){
				case 1:
					bombSpeed = 10;
					coinSpeed = 5;
					break;
				case 2:
					bombSpeed = 15;
					coinSpeed = 10;
					break;
				case 3:
					bombSpeed = 20;
					coinSpeed = 15;
					break;
				case 4:
					bombSpeed = 25;
					coinSpeed = 17;
					break;
				case 5:
					bombSpeed = 30;
					coinSpeed = 20;
					break;
			}

			font.draw(batch,"Press anywhere " +  " to start!",10,Gdx.graphics.getHeight()/10);
			font.getData().setScale(10);
			if(Gdx.input.justTouched()){
				gameState = 1;
			}
		}
		else if(gameState == -1){
			//gameover
			preferences = Gdx.app.getPreferences("com.k.o.coinman.game");
			Integer topScore = preferences.getInteger("Score", -1);
			font.getData().setScale(2);
			font.draw(batch,"Game Over! Score: " + String.valueOf(score) + ". TOP SCORE:" + topScore + ". Press again to restart",10,Gdx.graphics.getHeight() / 10);
			font.getData().setScale(10);
			preferences.putInteger("Score",score);

			if(score > topScore || topScore == 0){
				preferences.putInteger("Score",score);
				preferences.flush();
			}

			if(Gdx.input.justTouched()){
				score = 0;
				bombRect.clear();
				bombX.clear();
				bombY.clear();
				coinX.clear();
				coinY.clear();
				coinRect.clear();
				gameState = 0;
			}
		}


		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();

	}


}
