package com.example.practicaminijuego;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.MediaPlayer;
import android.view.View;

import java.io.IOException;

public class BallView extends View implements SensorEventListener {

    final double deadzone = 0.1;
    int ballDiameter;
    int ballRadius;
    final double slowing = 0.5;
    Bitmap ballBitmap;
    Point ballPos, res, boundsStart, boundsEnd;
    double ballSpeedX = 0, ballSpeedY = 0;
    double senValX, senValY;
    GameLooper runnable;
    int gapStart, gapEnd;
    Paint paintBlack, paintWhite;
    boolean ballsGon = true;
    MediaPlayer mediaPlayer;
    boolean mediaPlayerPaused = false;

    public BallView(Context context, Point resolution) {
        super(context);

        this.res = resolution;

        ballDiameter = (int) (res.x / 3.6);
        ballRadius = ballDiameter / 2;

        // Load the ball
        Resources resources = getResources();
        ballBitmap = BitmapFactory.decodeResource(
                resources,
                R.drawable.ball
        );
        ballBitmap = getResizedBitmap(ballBitmap, ballDiameter, ballDiameter);

        // Set the ball's coordinates
        ballPos = new Point();
        ballPos.x = resolution.x / 2 - (ballBitmap.getWidth() / 2);
        ballPos.y = resolution.y / 2 - (ballBitmap.getHeight() / 2);

        // Stablish the gap
        gapStart = (int) ((res.x / 2) - ballDiameter / 1.9);
        gapEnd = (int) ((res.x / 2) + ballDiameter / 1.9);

        // Initialize paint black
        paintBlack = new Paint();
        paintBlack.setColor(Color.BLACK);
        paintBlack.setStrokeWidth((float) (res.x / 36.0));
        paintBlack.setTextSize((float) (res.x / 10.8));

        // Initialize paint white
        paintWhite = new Paint();
        paintWhite.setColor(Color.WHITE);
        paintWhite.setStrokeWidth((float) (res.x / 36.0));

        // Set the bounds to work with
        boundsStart = new Point();
        boundsStart.x = (int) paintBlack.getStrokeWidth() / 2;
        boundsStart.y = (int) paintBlack.getStrokeWidth() / 2;

        boundsEnd = new Point();
        boundsEnd.x = (int) (res.x - paintBlack.getStrokeWidth() / 2);
        boundsEnd.y = (int) (res.y - paintBlack.getStrokeWidth() / 2);

        // Initialize media player
        mediaPlayer = MediaPlayer.create(context, R.raw.crabrave);

        runnable = new GameLooper(this);
        new Thread(runnable).start();
    }

    public boolean isBallGon() {
        return ballsGon;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // Create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // Resize the bitmap
        matrix.postScale(scaleWidth, scaleHeight);

        // Recreate the new bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        ballSpeedX = ballSpeedX - senValX;
        ballSpeedY = ballSpeedY + senValY;

        // Sets ball in its position based on its speed.
        ballPos.x = (int) (ballPos.x + ballSpeedX);
        ballPos.y = (int) (ballPos.y + ballSpeedY);

        // Check if the ball is out of the screen.
        checkIfBallsGone();

        // Checks if the ball is colliding with a border.
        checkBorderColisions();

        // Slows the ball a bit each frame.
        slowBall();

        canvas.drawColor(Color.WHITE);
        canvas.drawLine(0, 0, res.x, 0, paintBlack);
        canvas.drawLine(0, 0, 0, res.y, paintBlack);
        canvas.drawLine(res.x, 0, res.x, res.y, paintBlack);
        canvas.drawLine(0, res.y, res.x, res.y, paintBlack);
        canvas.drawLine(gapStart - ballRadius, res.y, gapEnd + ballRadius, res.y, paintWhite);
        canvas.drawBitmap(
                ballBitmap,
                ballPos.x,
                ballPos.y,
                null
        );

        if (ballsGon) {
            canvas.drawText("Ball's gone", res.x / 3, res.y / 2, paintBlack);
            runnable.terminate();
        }


    }

    protected void reDraw() {
        invalidate();
    }

    private void slowBall() {
        // X axis
        if (ballSpeedX < -slowing) {
            ballSpeedX = ballSpeedX + slowing;
        } else if (ballSpeedX > slowing) {
            ballSpeedX = ballSpeedX - slowing;
        } else {
            ballSpeedX = 0;
        }

        // Y axis
        if (ballSpeedY < -slowing) {
            ballSpeedY = ballSpeedY + slowing;
        } else if (ballSpeedY > slowing) {
            ballSpeedY = ballSpeedY - slowing;
        } else {
            ballSpeedY = 0;
        }
    }

    private void checkBorderColisions() {

        // Let it go in to the gap
        if (ballPos.y > res.y / 2
                && ballPos.x > gapStart - ballRadius
                && ballPos.x < gapEnd - ballRadius) {

            // If it's in, don't let it go to the sides
            if (ballPos.y > boundsEnd.y) {
                if (ballPos.x < gapStart) {
                    ballPos.x = gapStart;
                } else if (ballPos.x > gapEnd + ballDiameter) {
                    ballPos.x = gapEnd - ballDiameter;
                }
            }
            return;
        }


        // X axis
        if (ballPos.x + ballBitmap.getWidth() > boundsEnd.x) {
            ballPos.x = boundsEnd.x - ballBitmap.getWidth() - 1;
            ballSpeedX = -(ballSpeedX / 2);
        } else if (ballPos.x < boundsStart.x) {
            ballPos.x = boundsStart.x + 1;
            ballSpeedX = -(ballSpeedX / 2);
        }

        // Y axis
        if (ballPos.y + ballBitmap.getHeight() > boundsEnd.y) {
            ballPos.y = boundsEnd.y - ballBitmap.getHeight() + 1;
            ballSpeedY = -(ballSpeedY / 2);
        } else if (ballPos.y < boundsStart.y) {
            ballPos.y = boundsStart.y - 1;
            ballSpeedY = -(ballSpeedY / 2);
        }
    }

    private void checkIfBallsGone() {
        if (ballPos.x > res.x + ballRadius
                || ballPos.x < -ballRadius
                || ballPos.y > res.y + ballRadius
                || ballPos.y < -ballRadius) {
            ballsGon = true;
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        } else {
            ballsGon = false;
            if (mediaPlayer.isPlaying()) {
                resetMediaPlayer();
            }
        }
    }

    public void resetMediaPlayer() {
        mediaPlayer.stop();
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        runnable.run();
    }

    public void reset() {
        resetMediaPlayer();
        ballPos.x = res.x / 2 - (ballDiameter / 2);
        ballPos.y = res.y / 2 - (ballDiameter / 2);
        ballSpeedX = 0;
        ballSpeedY = 0;
        runnable = new GameLooper(this);
        new Thread(runnable).start();
    }

    public void pauseMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayerPaused = true;
        }
    }

    public void resumeMusic() {
        if (mediaPlayerPaused) {
            mediaPlayer.start();
            mediaPlayerPaused = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                senValX = event.values[0];
                senValY = event.values[1];
            }

            if (senValX < deadzone && senValX > -deadzone) {
                senValX = 0;
            }
            if (senValY < deadzone && senValY > -deadzone) {
                senValY = 0;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }


}

