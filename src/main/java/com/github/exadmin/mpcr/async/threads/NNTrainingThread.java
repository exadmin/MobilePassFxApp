package com.github.exadmin.mpcr.async.threads;

import com.github.exadmin.mpcr.async.MyRunnable;
import com.github.exadmin.mpcr.fxui.FxSceneModel;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.KNearest;
import org.opencv.ml.Ml;
import org.opencv.utils.Converters;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NNTrainingThread extends MyRunnable {
    private static final Size SIZE = new Size(20, 20);

    private static final Map<String, Integer> TRN_DIRS = new HashMap<>();
    static {
        TRN_DIRS.put("./data/digits/digit_0", 0);
        TRN_DIRS.put("./data/digits/digit_1", 1);
        TRN_DIRS.put("./data/digits/digit_2", 2);
        TRN_DIRS.put("./data/digits/digit_3", 3);
        TRN_DIRS.put("./data/digits/digit_4", 4);
        TRN_DIRS.put("./data/digits/digit_5", 5);
        TRN_DIRS.put("./data/digits/digit_6", 6);
        TRN_DIRS.put("./data/digits/digit_7", 7);
        TRN_DIRS.put("./data/digits/digit_8", 8);
        TRN_DIRS.put("./data/digits/digit_9", 9);
    }

    public NNTrainingThread(FxSceneModel fxSceneModel, MyRunnable nextRunnable) {
        super(fxSceneModel,nextRunnable);
    }

    @Override
    protected void beforeRunSafe() throws Exception {
        super.beforeRunSafe();
        fxSceneModel.setStatusAsync("Start NN training");
    }

    @Override
    protected void runSafe() throws Exception {
        // prepare train matrices
        Mat trainData = new Mat();
        List<Integer> trainLabs = new ArrayList<>();

        // load pictures & train AN
        int filesCount = 0;
        for (Map.Entry<String, Integer> me : TRN_DIRS.entrySet()) {
            Integer digit = me.getValue();

            List<String> files = listFilesUsingDirectoryStream(me.getKey());
            for (String fileName : files) {
                Mat bigDigit = Imgcodecs.imread(fileName, 0);
                Mat smallDigit = new Mat();

                Imgproc.resize(bigDigit, smallDigit, SIZE);
                smallDigit.convertTo(smallDigit, CvType.CV_32F);

                trainData.push_back(smallDigit.reshape(1, 1));
                trainLabs.add(digit);

                filesCount ++;
                fxSceneModel.setStatusAsync("Start NN training - using " + filesCount + " digit examples");
            }
        }

        KNearest kNearest = KNearest.create();
        kNearest.train(trainData, Ml.ROW_SAMPLE, Converters.vector_int_to_Mat(trainLabs));
        fxSceneModel.setKNearest(kNearest);
    }

    /**
     * Returns list of all png files inside directory
     * @param dir String name of directory to list pictures in
     * @return List of full file names
     * @throws IOException
     */
    public static List<String> listFilesUsingDirectoryStream(String dir) throws IOException {
        List<String> fileList = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir))) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {
                    String fileName = path.getFileName().toString();
                    if (fileName.endsWith(".png")) fileList.add(path.toString());
                }
            }
        }
        return fileList;
    }
}
