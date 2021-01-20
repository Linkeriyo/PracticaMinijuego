package com.example.practicaminijuego;

public class GameLooper implements Runnable {
    private volatile boolean running = true;
    final int frameRate = 60;
    final long timeout = (long) ((1.0 / frameRate) * 100);
    BallView v;

    public GameLooper(BallView v) {
        this.v = v;
    }

    public void terminate() {
        running = false;
    }

    public void run() {
        while (running) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e){
                e.printStackTrace();
                running = false;
            }
            v.reDraw();
        }
    }
}
