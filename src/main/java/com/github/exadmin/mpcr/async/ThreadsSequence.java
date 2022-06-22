package com.github.exadmin.mpcr.async;

import com.github.exadmin.mpcr.fxui.FxSceneModel;

import java.lang.reflect.Constructor;
import java.util.Stack;

public class ThreadsSequence {

    public ThreadsSequenceHelper startFrom(Class<? extends MyRunnable> myRunnableClass) {
        ThreadsSequenceHelper helper = new ThreadsSequenceHelper();
        helper.thenRun(myRunnableClass);
        return helper;
    }

    public static class ThreadsSequenceHelper {
        final Stack<Class< ? extends MyRunnable>> threadClasses = new Stack<>();

        protected ThreadsSequenceHelper getThis() {
            return this;
        }

        public ThreadsSequenceHelper thenRun(Class<? extends MyRunnable> myRunnableClass) {
            threadClasses.push(myRunnableClass);
            return getThis();
        }

        public void start(FxSceneModel fxSceneModel) {
            MyRunnable prevRunnable = null;

            while (!threadClasses.empty()) {
                try {
                    Class<? extends MyRunnable> myRunnableClass = threadClasses.pop();
                    Constructor<? extends MyRunnable> constructor = myRunnableClass.getConstructor(FxSceneModel.class, MyRunnable.class);
                    prevRunnable = constructor.newInstance(fxSceneModel, prevRunnable);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.exit(-1);
                }
            }

            if (prevRunnable != null) {
                Thread thread = new Thread(prevRunnable);
                thread.setName(prevRunnable.getClass().getName());
                thread.setDaemon(true);
                thread.start();
            }
        }
    }
}
