package com.github.exadmin.mpcr.examples;

import com.github.exadmin.mpcr.async.threads.EstablishConnectionThread;
import com.github.exadmin.mpcr.fxui.FxSceneModel;

public class TestCLI {
    public static void main(String[] args) {
        FxSceneModel fxSceneModel = new FxSceneModel(null);
        fxSceneModel.pinCode.setValue("616883");
        EstablishConnectionThread fakeTread = new EstablishConnectionThread(fxSceneModel, null, true);
        fakeTread.run();
    }
}
