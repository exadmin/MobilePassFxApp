package com.github.exadmin.mpcr.async.threads;

import com.github.exadmin.mpcr.async.MyRunnable;
import com.github.exadmin.mpcr.fxui.FxSceneModel;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.Objects;

public class GifResourceLoaderThread extends MyRunnable {

    public GifResourceLoaderThread(FxSceneModel fxSceneModel, MyRunnable nextRunnable) {
        super(fxSceneModel, nextRunnable);
    }

    @Override
    protected void runSafe() throws Exception {
        // loading gif image
        InputStream gifStream = Objects.requireNonNull(getClass().getResourceAsStream("/images/camera/loading.gif"));
        Image image = new Image(gifStream);
        fxSceneModel.setImageAsync(image);
    }
}
