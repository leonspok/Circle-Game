package com.leonspok.circlegame;

/**
 * Created by igorsavelev on 05/03/16.
 */
public abstract class GameTimer extends Thread {
    private volatile boolean cancelled = false;
    private float duration;

    public GameTimer(float duration) {
        this.duration = duration;
    }

    public void cancel() {
        cancelled = true;
    }

    public abstract void timerFired();
    public abstract void timerCompleted();

    public void run() {
        long startMillis = System.currentTimeMillis();
        while (!this.cancelled) {
            long currentMillis = System.currentTimeMillis();
            if (currentMillis - startMillis >= this.duration * 1000) {
                this.timerCompleted();
                return;
            }
            this.timerFired();
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
