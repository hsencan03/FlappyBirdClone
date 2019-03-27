package com.ngdroidapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.Random;

import istanbul.gamelab.ngdroid.base.BaseCanvas;
import istanbul.gamelab.ngdroid.util.Log;
import istanbul.gamelab.ngdroid.util.Utils;


/**
 * Created by noyan on 24.06.2016.
 * Nitra Games Ltd.
 */


public class GameCanvas extends BaseCanvas {

    private static final int STATE_START = -1;
    private static final int STATE_PLAY = 0;
    private static final int STATE_GAMEOVER = 1;
    private static int currentstate;

    private int SOUND_WING;
    private int SOUND_POINT;
    private int SOUND_HIT;
    private int SOUND_DIE;

    private static final int GRAVITY = 10;

    private Random rand;

    private Bitmap background;
    private int backgroundcount, bgmovespeed;
    private int[] backgroundx;
    private Rect[] bgdestination;

    private Bitmap floor;
    private int floorcount, floormovespeed;
    private int floorx[], floory;
    private Rect[] floordestination;

    private Bitmap startbg;
    private int startbgx, startbgy, startbgw, startbgh;
    private Rect startbgdestination;

    private Bitmap gameover;
    private int gameoverx, gameovery, gameoverw, gameoverh;
    private Rect godestination;

    private Bitmap[] bird;
    private int birdanimationsize, birdanimno, birdanimationcounter;
    private int birdx, birdy, birdw, birdh;
    private int birddy;
    private Rect birddestination, birdcollision;

    private Bitmap pipe;
    private int pipecount, pipemovespeed, currentpipeid;
    private int pipex[], pipey[][];
    private int pipew, pipeh, pipescalex, pipescaley;
    private int pipegap;
    private Rect[][] pipedestination;

    private int scoreimgcount;
    private Bitmap[] scoreimg;
    private int score;
    private int scorex, scorey;
    private Rect scoredestination;

    public GameCanvas(NgApp ngApp) {
        super(ngApp);
    }

    public void setup() {
        currentstate = STATE_START;

        rand = new Random();

        background = Utils.loadImage(root, "background-day.png");
        backgroundcount = 3;
        bgmovespeed = -10;
        backgroundx = new int[backgroundcount];
        bgdestination = new Rect[backgroundcount];
        for(int i = 0; i < backgroundcount; i++) {
            backgroundx[i] = i * getUnitWidth();
            bgdestination[i] = new Rect(backgroundx[i], 0, backgroundx[i] + getUnitWidth(), getUnitHeight());
        }

        floor = Utils.loadImage(root, "base.png");
        floorcount = backgroundcount;
        floormovespeed = bgmovespeed;
        floorx = new int[floorcount];
        floory = getUnitHeight() - 300;
        floordestination = new Rect[floorcount];
        for(int i = 0; i < floorcount; i++) {
            floorx[i] = i * getUnitWidth();
            floordestination[i] = new Rect(floorx[i], floory, floorx[i] + getUnitWidth(), getUnitHeight());
        }

        startbg = Utils.loadImage(root, "menubg.png");
        startbgw = startbg.getWidth() * 3;
        startbgh = startbg.getHeight() * 3;
        startbgx = (getUnitWidth() - startbgw) / 2;
        startbgy = (getUnitHeight() - startbgh) / 2;
        startbgdestination = new Rect(startbgx, startbgy, startbgx + startbgw, startbgy + startbgh);

        gameover = Utils.loadImage(root, "gameover.png");
        gameoverw = gameover.getWidth() * 3;
        gameoverh = gameover.getHeight() * 3;
        gameoverx = (getUnitWidth() - gameoverw) / 2;
        gameovery = (getUnitHeight() - gameoverh) / 2;
        godestination = new Rect(gameoverx, gameovery, gameoverx + gameoverw, gameovery + gameoverh);

        birdanimationsize = 3;
        birdanimno = 0;
        birdanimationcounter = 2;
        bird = new Bitmap[birdanimationsize];
        for(int i = 0; i < birdanimationsize; i++) {
            bird[i] = Utils.loadImage(root, "bird" + i + ".png");
        }
        birdw = bird[0].getWidth() * 3;
        birdh = bird[0].getHeight() * 3;
        birdx = (getUnitWidth() - birdw) / 4;
        birdy = (getUnitHeight() - birdh) / 2;
        birddestination = new Rect();
        birdcollision = new Rect();

        pipe = Utils.loadImage(root, "pipe-green.png");
        pipecount = 4;
        pipemovespeed = bgmovespeed;
        currentpipeid = 0;
        pipedestination = new Rect[pipecount][2];
        pipex = new int[pipecount];
        pipey = new int[pipecount][2];
        pipew = pipe.getWidth();
        pipeh = pipe.getHeight();
        pipescalex = 2;
        pipescaley = 4;
        pipegap = 400;
        for(int i = 0; i < pipecount; i++) {
            int posy = rand.nextInt(500) - 800;
            pipey[i][0] = posy;
            pipey[i][1] = getUnitHeight() - (pipeh + Math.abs(posy));

            pipex[i] = (pipegap + pipew * pipescalex) * (i + 1) + getUnitWidth();

            pipedestination[i][0] = new Rect();
            pipedestination[i][1] = new Rect();
        }

        scoreimgcount = 10;
        score = 0;
        scoredestination = new Rect();
        scoreimg = new Bitmap[scoreimgcount];
        for(int i = 0; i < scoreimgcount; i++) {
            scoreimg[i] = Utils.loadImage(root, i + ".png");
        }


        try {
            SOUND_WING = root.soundManager.load("sounds/wing.wav");
            SOUND_POINT = root.soundManager.load("sounds/point.wav");
            SOUND_HIT = root.soundManager.load("sounds/hit.wav");
            SOUND_DIE = root.soundManager.load("sounds/die.wav");

        } catch (Exception e){
            Log.e("GameCanvas/Setup", "Sound could not be loaded");
        }
    }

    public void update() {
        switch (currentstate) {
            case STATE_START:
                moveBackground();
                moveFloor();
                break;
            case STATE_PLAY:
                moveBackground();
                moveFloor();
                movePipes();
                moveBird();
                break;
            case STATE_GAMEOVER:
                moveBird();
                break;
        }
    }

    public void draw(Canvas canvas) {
        canvas.save();
        canvas.scale(getWidth() / 1080.0f, getHeight() / 1920.0f);

        switch (currentstate) {
            case STATE_START:
                drawBackground(canvas);
                drawFloor(canvas);
                drawStartScreen(canvas);
                break;
            case STATE_PLAY:
                drawBackground(canvas);
                drawFloor(canvas);
                drawPipes(canvas);
                drawBird(canvas);
                drawGUI(canvas);
                break;
            case STATE_GAMEOVER:
                drawBackground(canvas);
                drawFloor(canvas);
                drawPipes(canvas);
                drawBird(canvas);
                drawGUI(canvas);
                drawGameOverScreen(canvas);
                break;
        }

        canvas.restore();
    }

    private void respawnPipe(int id) {
        pipex[id] = pipex[id] + ((pipegap + pipew * pipescalex) * pipecount);

        int posy = rand.nextInt(500) - 800;

        pipey[id][0] = posy;
        pipey[id][1] = getUnitHeight() - (pipeh + Math.abs(posy));
    }

    private void moveBackground() {
        for(int i = 0; i < backgroundcount; i++) {
            backgroundx[i] += bgmovespeed;
            if(backgroundx[i] < -getUnitWidth()) {
                backgroundx[i] = (backgroundcount - 1) * getUnitWidth();
            }
            bgdestination[i].set(backgroundx[i], 0, backgroundx[i] + getUnitWidth(), getUnitHeight());
        }
    }

    private void moveFloor() {
        for(int i = 0; i < floorcount; i++) {
            floorx[i] += floormovespeed;
            if(floorx[i] < -getUnitWidth()) {
                floorx[i] = (floorcount - 1) * getUnitWidth();
            }
            floordestination[i].set(floorx[i], floory, floorx[i] + getUnitWidth(), getUnitHeight());
        }
    }

    private void movePipes() {
        for(int i = 0; i < pipecount; i++) {
            pipex[i] += pipemovespeed;
            if(pipex[i] + (pipew * pipescalex) < 0) {
                respawnPipe(i);
            }
            pipedestination[i][0].set(pipex[i], pipey[i][0], pipex[i] + pipew * pipescalex, pipey[i][0] + (pipeh * pipescaley));
            pipedestination[i][1].set(pipex[i], pipey[i][1], pipex[i] + pipew * pipescalex, pipey[i][1] + (pipeh * pipescaley));
        }
    }

    private void moveBird() {
        if(--birdanimationcounter <= 0) {
            if (++birdanimno >= birdanimationsize) {
                birdanimno = 0;
            }
            birdanimationcounter = 2;
        }

        birdcollision.set(birdx, birdy + birddy, birdx + birdw, birdy + birddy + birdh);
        if((birdcollision.intersect(pipedestination[currentpipeid][0]) || birdcollision.intersect(pipedestination[currentpipeid][1]) ||
            birdcollision.intersect(pipedestination[currentpipeid == 0 ? pipecount - 1 : currentpipeid - 1][0]) || birdcollision.intersect(pipedestination[currentpipeid == 0 ? pipecount - 1 : currentpipeid - 1][1]) ||
            birdy + birdh >= floory ||
            birdy <= 0) &&
            currentstate == STATE_PLAY) {
                root.soundManager.play(SOUND_HIT);
                root.soundManager.play(SOUND_DIE);
                currentstate = STATE_GAMEOVER;
        } else {
            birdy += birddy;
            birddy += GRAVITY;
        }
        birddestination.set(birdx, birdy, birdx + birdw, birdy + birdh);

        if(birdx > pipedestination[currentpipeid][0].left) {
            score++;
            root.soundManager.play(SOUND_POINT);
            if(++currentpipeid >= pipecount) currentpipeid = 0;
        }
    }

    private void drawBackground(Canvas canvas) {
        for(int i = 0; i < backgroundcount; i++) {
            canvas.drawBitmap(background, null, bgdestination[i], null);
        }
    }

    private void drawPipes(Canvas canvas) {
        for(int i = 0; i < pipecount; i++) {
            canvas.save();
            canvas.rotate(180, pipex[i] + (pipew * pipescalex) / 2, pipey[i][0] + (pipeh * pipescaley) / 2);
            canvas.drawBitmap(pipe, null, pipedestination[i][0], null);
            canvas.restore();
            canvas.drawBitmap(pipe, null, pipedestination[i][1], null);
        }
    }

    private void drawFloor(Canvas canvas) {
        for(int i = 0; i < floorcount; i++) {
            canvas.drawBitmap(floor, null, floordestination[i], null);
        }
    }

    private void drawStartScreen(Canvas canvas) {
        canvas.drawBitmap(startbg, null, startbgdestination, null);
    }

    private void drawBird(Canvas canvas) {
        if(currentstate == STATE_GAMEOVER) {
            canvas.save();
            canvas.rotate(30, birdx + birdw / 2, birdy + birdh / 2);
            canvas.drawBitmap(bird[birdanimno], null, birddestination, null);
            canvas.restore();
        } else {
            canvas.drawBitmap(bird[birdanimno], null, birddestination, null);
        }
    }

    private void drawGUI(Canvas canvas) {
        if(score >= 10) {
            int first = score / 10;
            int second = score % 10;
            scorex = (getUnitWidth() - (scoreimg[first].getWidth() * 3 + scoreimg[second].getWidth() * 3)) / 2;
            scorey = (getUnitHeight() - (scoreimg[first].getHeight() * 3 + scoreimg[second].getHeight() * 3)) / 4;

            scoredestination.set(scorex - 20, scorey, scorex - 20 + scoreimg[first].getWidth() * 3, scorey + scoreimg[first].getHeight() * 3);
            canvas.drawBitmap(scoreimg[first], null, scoredestination, null);
            scoredestination.set(scorex + 20, scorey, scorex + 20 + scoreimg[second].getWidth() * 3, scorey + scoreimg[second].getHeight() * 3);
            canvas.drawBitmap(scoreimg[second], null, scoredestination, null);
        } else {
            scorex = (getUnitWidth() - scoreimg[score].getWidth()) / 2;
            scorey = (getUnitHeight() - scoreimg[score].getHeight()) / 4;
            scoredestination.set(scorex, scorey, scorex + scoreimg[score].getWidth() * 3, scorey + scoreimg[score].getHeight() * 3);
            canvas.drawBitmap(scoreimg[score], null, scoredestination, null);
        }
    }

    private void drawGameOverScreen(Canvas canvas) {
        canvas.drawBitmap(gameover, null, godestination, null);
    }

    public void keyPressed(int key) {

    }

    public void keyReleased(int key) {

    }

    public boolean backPressed() {
        return true;
    }

    public void surfaceChanged(int width, int height) {

    }

    public void surfaceCreated() {

    }

    public void surfaceDestroyed() {

    }

    public void touchDown(int x, int y, int id) {
        switch (currentstate) {
            case STATE_START:
                currentstate = STATE_PLAY;
                break;
            case STATE_PLAY:
                birddy = -40;
                root.soundManager.play(SOUND_WING);
                break;
            case STATE_GAMEOVER:
                root.canvasManager.setCurrentCanvas(root.gc = new GameCanvas(root));
                break;
        }
    }

    public void touchMove(int x, int y, int id) {
    }

    public void touchUp(int x, int y, int id) {
    }


    public void pause() {

    }


    public void resume() {

    }


    public void reloadTextures() {

    }


    public void showNotify() {
    }

    public void hideNotify() {
    }

}
