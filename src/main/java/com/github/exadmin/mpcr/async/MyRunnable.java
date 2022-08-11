package com.github.exadmin.mpcr.async;

import com.github.exadmin.mpcr.fxui.FxSceneModel;

public abstract class MyRunnable implements Runnable {
    protected MyRunnable nextThreadToStart;
    protected final FxSceneModel fxSceneModel;

    public MyRunnable(FxSceneModel fxSceneModel, MyRunnable nextThreadToStart) {
        this.nextThreadToStart = nextThreadToStart;
        this.fxSceneModel = fxSceneModel;
    }

    protected void beforeRunSafe() throws Exception {
        // do nothing
        // System.out.println("Starting thread " + Thread.currentThread());
    }

    protected abstract void runSafe();

    protected void afterRunSafe() throws Exception {
        // do nothing
    }

    protected void onException(Exception ex) {
        ex.printStackTrace();
    }

    protected void afterRunSafeFinally() {
        // do nothing
    }

    @Override
    public final void run() {
        try {
            beforeRunSafe();
            runSafe();
            afterRunSafe();
        } catch (Exception ex) {
            onException(ex);
        }

        if (nextThreadToStart != null) {
            Thread thread = new Thread(nextThreadToStart);
            thread.setName(nextThreadToStart.getClass().getName());
            thread.setDaemon(true);
            thread.start();
        }
    }
}
