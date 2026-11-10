package com.example.supercoinrunner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GameView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final ArrayList<Thing> obstacles = new ArrayList<>();
    private final ArrayList<Thing> coins = new ArrayList<>();

    private float screenW, screenH;
    private float playerX, playerY, playerSize;
    private float targetX;
    private float groundY;
    private long lastTime = 0;
    private long spawnTimer = 0;
    private long coinTimer = 0;
    private long elapsed = 0;
    private int score = 0;
    private int best = 0;
    private int lives = 3;
    private int level = 1;
    private boolean started = false;
    private boolean gameOver = false;
    private boolean paused = false;

    private final int bgTop = 0xFF101B3D;
    private final int bgBottom = 0xFF263B68;

    public GameView(Context context) {
        super(context);
        setFocusable(true);
    }

    private static class Thing {
        float x, y, size, speed;
        Thing(float x, float y, float size, float speed) {
            this.x = x; this.y = y; this.size = size; this.speed = speed;
        }
    }

    private void resetGame() {
        obstacles.clear();
        coins.clear();
        score = 0;
        lives = 3;
        level = 1;
        elapsed = 0;
        spawnTimer = 0;
        coinTimer = 0;
        gameOver = false;
        started = true;
        paused = false;
        playerX = screenW / 2f;
        targetX = playerX;
        playerY = groundY - playerSize;
        lastTime = System.currentTimeMillis();
        invalidate();
    }

    public void pauseGame() {
        paused = true;
    }

    public void resumeGame() {
        if (started && !gameOver) {
            paused = false;
            lastTime = System.currentTimeMillis();
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        screenW = getWidth();
        screenH = getHeight();
        if (screenW <= 0 || screenH <= 0) return;

        if (playerSize == 0) {
            playerSize = Math.max(54f, screenW * 0.105f);
            groundY = screenH * 0.84f;
            playerX = screenW / 2f;
            targetX = playerX;
            playerY = groundY - playerSize;
        }

        drawBackground(canvas);

        if (started && !gameOver && !paused) updateGame();
        drawWorld(canvas);
        drawHud(canvas);

        if (!started) drawStartScreen(canvas);
        else if (gameOver) drawGameOver(canvas);
        else if (paused) drawPaused(canvas);

        if (started && !gameOver && !paused) postInvalidateDelayed(16);
    }

    private void drawBackground(Canvas c) {
        paint.setShader(new android.graphics.LinearGradient(0, 0, 0, screenH,
                bgTop, bgBottom, android.graphics.Shader.TileMode.CLAMP));
        c.drawRect(0, 0, screenW, screenH, paint);
        paint.setShader(null);

        paint.setColor(0x22FFFFFF);
        for (int i = 0; i < 28; i++) {
            float x = (i * 97f) % screenW;
            float y = (i * 173f) % (groundY - 50);
            c.drawCircle(x, y, (i % 3) + 1, paint);
        }

        paint.setColor(0xFF1C2D50);
        c.drawRoundRect(new RectF(0, groundY, screenW, screenH + 20), 24, 24, paint);
        paint.setColor(0xFF3D5B8E);
        c.drawRect(0, groundY, screenW, groundY + 7, paint);

        paint.setColor(0xFF314A73);
        for (int i = 0; i < 7; i++) {
            float x = (i * screenW / 6f);
            c.drawRoundRect(x, groundY + 24, x + 4, screenH, 3, 3, paint);
        }
    }

    private void updateGame() {
        long now = System.currentTimeMillis();
        if (lastTime == 0) lastTime = now;
        float dt = Math.min((now - lastTime) / 16.67f, 3.0f);
        lastTime = now;
        elapsed += (long)(dt * 16.67f);

        level = 1 + score / 100;
        float speed = (7.0f + level * 0.75f) * dt;

        playerX += (targetX - playerX) * Math.min(1f, 0.24f * dt);
        playerX = Math.max(playerSize / 2 + 8, Math.min(screenW - playerSize / 2 - 8, playerX));
        playerY = groundY - playerSize;

        spawnTimer += (long)(dt * 16.67f);
        coinTimer += (long)(dt * 16.67f);

        long obstacleDelay = Math.max(420, 1050 - level * 45);
        if (spawnTimer > obstacleDelay) {
            spawnTimer = 0;
            float size = playerSize * (0.72f + random.nextFloat() * 0.28f);
            float x = size / 2 + 8 + random.nextFloat() * (screenW - size - 16);
            obstacles.add(new Thing(x, -size, size, speed));
        }
        if (coinTimer > 720) {
            coinTimer = 0;
            float size = playerSize * 0.38f;
            float x = size + random.nextFloat() * (screenW - size * 2);
            coins.add(new Thing(x, -size, size, speed * 0.82f));
        }

        RectF player = new RectF(playerX - playerSize * .34f,
                playerY + playerSize * .12f,
                playerX + playerSize * .34f,
                playerY + playerSize * .90f);

        Iterator<Thing> oi = obstacles.iterator();
        while (oi.hasNext()) {
            Thing t = oi.next();
            t.y += t.speed * dt;
            RectF r = new RectF(t.x - t.size * .42f, t.y - t.size * .42f,
                    t.x + t.size * .42f, t.y + t.size * .42f);
            if (RectF.intersects(player, r)) {
                oi.remove();
                lives--;
                if (lives <= 0) endGame();
            } else if (t.y > screenH + t.size) {
                oi.remove();
                score += 5;
            }
        }

        Iterator<Thing> ci = coins.iterator();
        while (ci.hasNext()) {
            Thing t = ci.next();
            t.y += t.speed * dt;
            float dx = Math.abs(playerX - t.x);
            float dy = Math.abs((playerY + playerSize / 2) - t.y);
            if (dx < playerSize * .58f && dy < playerSize * .62f) {
                ci.remove();
                score += 10;
            } else if (t.y > screenH + t.size) {
                ci.remove();
            }
        }
    }

    private void drawWorld(Canvas c) {
        for (Thing t : obstacles) {
            paint.setColor(0xFFFF5964);
            c.drawRoundRect(new RectF(t.x - t.size * .46f, t.y - t.size * .46f,
                    t.x + t.size * .46f, t.y + t.size * .46f),
                    t.size * .18f, t.size * .18f, paint);
            paint.setColor(0xFFFFA0A5);
            c.drawRoundRect(new RectF(t.x - t.size * .25f, t.y - t.size * .25f,
                    t.x + t.size * .25f, t.y - t.size * .10f),
                    8, 8, paint);
        }

        for (Thing t : coins) {
            paint.setColor(0xFFFFC928);
            c.drawCircle(t.x, t.y, t.size * .55f, paint);
            paint.setColor(0xFFFFED91);
            c.drawCircle(t.x - t.size * .12f, t.y - t.size * .12f, t.size * .20f, paint);
        }

        // Player: bright blue rounded character with eyes
        paint.setColor(0xFF35D6FF);
        c.drawRoundRect(new RectF(playerX - playerSize * .36f, playerY + playerSize * .08f,
                playerX + playerSize * .36f, playerY + playerSize * .92f),
                playerSize * .22f, playerSize * .22f, paint);
        paint.setColor(0xFF087DAA);
        c.drawRoundRect(new RectF(playerX - playerSize * .25f, playerY + playerSize * .60f,
                playerX + playerSize * .25f, playerY + playerSize * .82f),
                10, 10, paint);
        paint.setColor(0xFF062D4A);
        c.drawCircle(playerX - playerSize * .12f, playerY + playerSize * .36f, playerSize * .045f, paint);
        c.drawCircle(playerX + playerSize * .12f, playerY + playerSize * .36f, playerSize * .045f, paint);
    }

    private void drawHud(Canvas c) {
        paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        paint.setTextSize(Math.max(18, screenW * .045f));
        paint.setColor(0xFFFFFFFF);
        c.drawText("SCORE  " + score, 24, 48, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        c.drawText("LEVEL " + level, screenW - 24, 48, paint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(0xFFFF6677);
        String hearts = "";
        for (int i = 0; i < lives; i++) hearts += "♥ ";
        paint.setTextSize(25);
        c.drawText(hearts, 24, 82, paint);
        paint.setColor(0xFFFFD34E);
        paint.setTextSize(15);
        c.drawText("BEST " + Math.max(best, score), 24, 108, paint);
    }

    private void drawStartScreen(Canvas c) {
        drawOverlay(c);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        paint.setTextSize(screenW * .105f);
        paint.setColor(0xFFFFFFFF);
        c.drawText("SUPER", screenW / 2, screenH * .34f, paint);
        paint.setColor(0xFFFFD34E);
        c.drawText("COIN RUNNER", screenW / 2, screenH * .43f, paint);
        paint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        paint.setTextSize(18);
        paint.setColor(0xFFE4ECFF);
        c.drawText("DODGE • COLLECT • SURVIVE", screenW / 2, screenH * .50f, paint);
        drawButton(c, "TAP TO PLAY", screenH * .61f);
        paint.setTextSize(15);
        paint.setColor(0xFFCCD8F2);
        c.drawText("Move your finger left and right", screenW / 2, screenH * .70f, paint);
        c.drawText("Avoid red blocks • Collect gold coins", screenW / 2, screenH * .74f, paint);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawGameOver(Canvas c) {
        drawOverlay(c);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        paint.setTextSize(42);
        paint.setColor(0xFFFF6875);
        c.drawText("GAME OVER", screenW / 2, screenH * .39f, paint);
        paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        paint.setTextSize(24);
        paint.setColor(Color.WHITE);
        c.drawText("Score: " + score, screenW / 2, screenH * .48f, paint);
        c.drawText("Best: " + Math.max(best, score), screenW / 2, screenH * .54f, paint);
        drawButton(c, "PLAY AGAIN", screenH * .65f);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawPaused(Canvas c) {
        drawOverlay(c);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(36);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setColor(Color.WHITE);
        c.drawText("PAUSED", screenW / 2, screenH * .48f, paint);
        c.drawText("Tap to continue", screenW / 2, screenH * .56f, paint);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawOverlay(Canvas c) {
        paint.setColor(0xCC081329);
        c.drawRect(0, 0, screenW, screenH, paint);
    }

    private void drawButton(Canvas c, String label, float centerY) {
        float w = screenW * .64f;
        RectF r = new RectF((screenW - w) / 2, centerY - 30, (screenW + w) / 2, centerY + 30);
        paint.setColor(0xFF13BCEB);
        c.drawRoundRect(r, 30, 30, paint);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        paint.setTextSize(19);
        c.drawText(label, screenW / 2, centerY + 7, paint);
    }

    private void endGame() {
        gameOver = true;
        best = Math.max(best, score);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        if (event.getAction() == MotionEvent.ACTION_DOWN ||
                event.getAction() == MotionEvent.ACTION_MOVE) {
            if (!started || gameOver) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) resetGame();
                return true;
            }
            if (paused) {
                paused = false;
                lastTime = System.currentTimeMillis();
                invalidate();
                return true;
            }
            targetX = x;
            invalidate();
            return true;
        }
        return true;
    }
}
