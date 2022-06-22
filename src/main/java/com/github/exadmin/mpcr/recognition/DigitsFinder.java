package com.github.exadmin.mpcr.recognition;

import com.github.exadmin.mpcr.fxui.FxSceneModel;
import com.github.exadmin.opencv4j.ColorUtils;
import com.github.exadmin.opencv4j.ImageUtils;
import com.github.exadmin.opencv4j.MatrixUtils;
import com.github.exadmin.opencv4j.enums.ContourApproximationMethod;
import com.github.exadmin.opencv4j.enums.CvType4j;
import com.github.exadmin.opencv4j.enums.RetrievalMode;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.KNearest;

import java.util.ArrayList;
import java.util.List;

public class DigitsFinder {
    private static final Size SIZE = new Size(20, 20);
    private static final int COMPACTED_IMAGE_WIDTH = 640;
    private final FxSceneModel fxSceneModel;

    public DigitsFinder(FxSceneModel fxSceneModel) {
        this.fxSceneModel = fxSceneModel;
    }

    public List<Integer> findDigitsInTheImage(final Mat sourceFrame, KNearest kNearest) {
        // cache settings into local variable for handy aims
        int bilateralFilterDiameter = fxSceneModel.bilateralFilterDiameterProperty.getValue();
        double bilateralFilterSigmaColor = fxSceneModel.bilateralFilterSigmaColor.getValue() / 10d;
        double bilateralFilterSigmaSpace = fxSceneModel.bilateralFilterSigmaSpace.getValue() / 10d;
        double kernelWidth = fxSceneModel.gaussKernelWidth.getValue();
        double kernelHeight = fxSceneModel.gaussKernelHeight.getValue();
        double sigmaX = fxSceneModel.gaussSigmaX.getValue() / 10d;
        double threshold1 = fxSceneModel.cannyThreshold1.getValue() / 10d;
        double threshold2 = fxSceneModel.cannyThreshold2.getValue() / 10d;
        int apertureSize = fxSceneModel.cannyApertureSize.get() * 2 + 1;

        // resize original image to designed width
        final Mat resizedMat = MatrixUtils.resizeImage(sourceFrame, COMPACTED_IMAGE_WIDTH);
        fxSceneModel.setDebugImageAsync(resizedMat, "Compacted frame", 0, 0);
        int compactedImageHeight = resizedMat.height();

        // convert image to gray-scaled
        final Mat grayScaledMat = MatrixUtils.getGrayScaledImage(resizedMat);

        //processing image
        Mat filteredMat = MatrixUtils.getBilateralFilteredImage(grayScaledMat, bilateralFilterDiameter, bilateralFilterSigmaColor, bilateralFilterSigmaSpace);
        Mat gaussBlurMat = MatrixUtils.getGaussianBlurredImage(filteredMat, kernelWidth, kernelHeight, sigmaX);
        Mat edgesMat = MatrixUtils.getEdgesUsingCanny(gaussBlurMat, threshold1, threshold2, apertureSize);
        fxSceneModel.setDebugImageAsync(edgesMat, "Edges", 0, 1);

        // Find contours on the image
        List<MatOfPoint> contourPoints = MatrixUtils.findContours(edgesMat, RetrievalMode.RETR_TREE, ContourApproximationMethod.CHAIN_APPROX_SIMPLE);
        java.util.List<MatOfPoint2f> visibleConts2f = new ArrayList<>();
        for (MatOfPoint contour : contourPoints) {

            // Convert contour to closed polygon
            MatOfPoint2f approxContour2f = MatrixUtils.approximatePolygon(contour, 5.0, true);

            // Select polygon which have only four edges
            if (approxContour2f.toList().size() == 4) {

                // skip contours which are too close to edge of the image
                boolean skip = false;
                for (Point p : approxContour2f.toList()) {
                    if (p.x < 2 || p.y < 2 || p.x > COMPACTED_IMAGE_WIDTH - 2 || p.y > compactedImageHeight - 2) {
                        skip = true;
                        break;
                    }
                }
                if (skip) continue;

                visibleConts2f.add(approxContour2f);
            }
        }

        // Sort selected polygons by their length
        visibleConts2f.sort((curve1, curve2) -> {
            int len1 = (int) Imgproc.arcLength(curve1, true);
            int len2 = (int) Imgproc.arcLength(curve2, true);

            return len2 - len1;
        });

        // select only X largest rectangles
        int LIMIT = 1;
        java.util.List<MatOfPoint> finalList = new ArrayList<>();
        for (int i=0; i<LIMIT; i++) {
            MatOfPoint2f curve2f = visibleConts2f.get(i);
            MatOfPoint curve = MatrixUtils.convert(curve2f, CvType4j.CV_32bit_SignedInt);
            finalList.add(curve);
        }

        Mat tmpMat = MatrixUtils.getCopy(resizedMat);

        for (int vcIndex = 0; vcIndex < finalList.size(); vcIndex++) {
            MatOfPoint visCont = finalList.get(vcIndex);

            if (fxSceneModel.isDebugFlagEnabled()) {
                // highlight interesting region on the debug frame
                Scalar color = ColorUtils.getColor(255, 50, 50);
                Imgproc.drawContours(tmpMat, finalList, vcIndex, color, 1, Imgproc.LINE_8);
                fxSceneModel.setDebugImageAsync(tmpMat, "Region recognition", 0, 2);
            }

            // todo: check that geometric-center is placed inside polygon - otherwise ignore contour

            // transform found rectangle to correct rectangle with strict angels
            List<Point> points = visCont.toList();
            Mat transformedMat = MatrixUtils.getPerspectiveBy4Points(grayScaledMat, COMPACTED_IMAGE_WIDTH, compactedImageHeight, points);
            if (transformedMat != null) {
                fxSceneModel.setDebugImageAsync(transformedMat, "Transformed", 1, 0);

                Mat dstMat = new Mat();
                Imgproc.threshold(transformedMat, dstMat, 150, 255, Imgproc.THRESH_BINARY);

                fxSceneModel.setDebugImageAsync(dstMat, "Thresholded", 1, 1);
                return recognizeDigits(dstMat, kNearest);
            }
        }

        return null;
    }

    /**
     * Recognize digits on the prepared and normalized image
     */
    private List<Integer> recognizeDigits(final Mat imageMat, KNearest kNearest) {
        try {
            fxSceneModel.setDebugImageAsync(imageMat, "fx-image", 1, 2);

            Mat digitsAreaImg = getCommonDigitArea(imageMat);
            fxSceneModel.setDebugImageAsync(digitsAreaImg, "Digits only area", 2, 0);


            if (digitsAreaImg == null) return null;
            Mat[] digitImages = getDigits(digitsAreaImg);
            if (digitImages == null || digitImages.length != 6) return null;

            List<Integer> recognizedDigits = new ArrayList<>();

            for (Mat nextMat : digitImages) {
                int digit = (int) recognizeOneDigit(kNearest, nextMat);
                if (digit >= 0) {
                    recognizedDigits.add(digit);
                }
            }

            if (recognizedDigits.size() == 6) {
                return recognizedDigits;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Returns only region which contains digits - cuts all useless edges.
     * How it works currently:
     *  1. transforms matrix to fx-image
     *  2. looks most top, left, bottom, right coordinates where digits starts or ends
     *  3. crops all useless space around digits
     *  4. return matrix with digits only
     * todo: convert implementation to open-cv native way
     */
    private Mat getCommonDigitArea(final Mat sourceMatrix) {
        Image fxImage = ImageUtils.convertToFxImage(sourceMatrix);
        PixelReader pixelReader = fxImage.getPixelReader();

        // quick assertions
        final int DELTA = 10;

        int imgWidth = (int) fxImage.getWidth();
        int imgHeight = (int) fxImage.getHeight();
        if (imgWidth < 640-DELTA || imgWidth > 640 + DELTA
                || imgHeight < 480 - DELTA || imgHeight > 480 + DELTA) return null;

        // define new sub image with big digits
        final int startRowIndex = 150;
        final int endRowIndex   = 350;
        final int startColIndex = 100;
        final int endColIndex   = 550;
        final Color BLACK = Color.BLACK;

        int leftX  = 1000;
        int rightX = 0;
        int topY   = 1000;
        int bottomY= 0;

        for (int y=startRowIndex; y<endRowIndex; y++) {
            for (int x=startColIndex; x<endColIndex; x++) {
                Color color = pixelReader.getColor(x, y);
                if (!BLACK.equals(color)) {
                    if (x < leftX) leftX = x;
                    if (x > rightX) rightX = x;
                    if (y < topY) topY = y;
                    if (y > bottomY) bottomY = y;
                }
            }
        }

        int newWidth = rightX - leftX;
        int newHeight = bottomY - topY;

        if (newWidth <= 0 || newHeight <= 0) return null;

        return sourceMatrix.submat(topY, bottomY, leftX, rightX);
    }

    private static float recognizeOneDigit(KNearest trainedNN, Mat bigDigit) {
        if (bigDigit == null || bigDigit.empty()) return -1;

        Mat smallDigit = new Mat();
        Imgproc.resize(bigDigit, smallDigit, SIZE);
        smallDigit.convertTo(smallDigit, CvType.CV_32F);

        Mat testDigit = smallDigit.reshape(1, 1);
        Mat res = new Mat();
        return trainedNN.findNearest(testDigit, 1, res);
    }


    /**
     * Returns array of matrices. One matrix per one digit.
     * Current implementation works with fx-image
     * todo: convert implementation to open-cv native way
     */
    public Mat[] getDigits(final Mat sourceMat) {
        Image fxImage = ImageUtils.convertToFxImage(sourceMat);

        final int imgWidth = (int)fxImage.getWidth();
        final int imgHeight = (int) fxImage.getHeight();
        final PixelReader pixelReader = fxImage.getPixelReader();


        List<Integer> xEdges = new ArrayList<>();
        xEdges.add(0);

        int x=1;


        while (x < imgWidth) {
            // find first fully black column and remember its position (actually previous, i.e. -1 by x)
            for (; x < imgWidth; x++) {
                boolean columnIsFullyBlack = true;
                for (int y=0; y<imgHeight; y++) {
                    Color color = (pixelReader.getColor(x, y));
                    if (Color.BLACK.equals(color)) continue;

                    columnIsFullyBlack = false;
                    break;
                }

                if (columnIsFullyBlack) {
                    xEdges.add(x-1);
                    break;
                }
            }

            // find first column with white pixel - and remember its position
            x++;
            for (; x < imgWidth; x++) {
                boolean columnHasWhitePixel = false;
                for (int y=0; y<imgHeight; y++) {
                    Color color = (pixelReader.getColor(x, y));
                    if (Color.BLACK.equals(color)) continue;

                    columnHasWhitePixel = true;
                    break;
                }

                if (columnHasWhitePixel) {
                    xEdges.add(x);
                    break;
                }
            }
        }

        xEdges.add(imgWidth -1);

        // assertion - here we should have 12 coordinates: leftX + rightX for each digit.
        if (xEdges.size() != 12) return null;

        List<Mat> imageList = new ArrayList<>(6);

        for (int i=0; i<6; i++) {
            int leftX = xEdges.get(i * 2);
            int rightX = xEdges.get(i * 2 + 1);

            Mat digit = sourceMat.submat(0, imgHeight-1, leftX, rightX);

            imageList.add(digit);
        }

        return imageList.toArray(new Mat[0]);
    }
}
