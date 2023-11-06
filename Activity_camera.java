package com.example.betaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import com.example.betaapp.ml.BestFp16;

public class Activity_camera extends AppCompatActivity implements ImageReader.OnImageAvailableListener {
    private static final int INPUT_SIZE = 640;
    private static final int NUM_CLASSES = 5;
    private static final String MODEL_FILE = "ml/best-fp16.tflite";
    private Interpreter tflite;
    private ByteBuffer imgData;
    private int[] intValues;
    private int yRowStride;
    private int sensorOrientation;
    private Handler handler;
    private static final String[] LABELS = {"hello", "this", "mine", "project", "thanks"};
    private BestFp16 model;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        handler = new Handler();

        // Initialize TensorFlow Lite model
        try {
            tflite = new Interpreter(loadModelFile(this, MODEL_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize input and output buffers for inference
        imgData = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3); // 3 channels, each channel has 4 bytes (float size)
        intValues = new int[INPUT_SIZE * INPUT_SIZE];

        //TODO ask for camera permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.CAMERA}, 121);
            } else {
                //TODO show live camera footage
                setFragment();
            }
        } else {
            //TODO show live camera footage
            setFragment();
        }
    }

    // Method to load the TFLite model file from assets folder
    private MappedByteBuffer loadModelFile(Activity activity, String modelFile) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelFile);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Method to preprocess the camera frame and convert it to the required input format for TFLite
    private void preprocessImage(Bitmap bitmap) {
        // Resize the image to the input size of the model
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);

        // Initialize input and output buffers for inference
        int numBytesPerChannel = 4; // Each float value is 4 bytes
        imgData = ByteBuffer.allocateDirect(INPUT_SIZE * INPUT_SIZE * 3 * 4); // 3 channels, each channel has 4 bytes (float size)
        imgData.order(ByteOrder.nativeOrder());
        intValues = new int[INPUT_SIZE * INPUT_SIZE];

        // Normalize the pixel values to the range of [0, 1]
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.getWidth(), 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());
        for (int i = 0; i < intValues.length; i++) {
            final int val = intValues[i];
            imgData.putFloat(((val >> 16) & 0xFF) / 255.0f);
            imgData.putFloat(((val >> 8) & 0xFF) / 255.0f);
            imgData.putFloat((val & 0xFF) / 255.0f);
        }
    }

    // Method to perform inference on the camera frame using the loaded TFLite model
    private void detectObjects(Bitmap bitmap) {
        if (model == null) {
            Log.e("TFLite", "TFLite model is not loaded.");
            return;
        }


        // Preprocess the camera frame
        preprocessImage(bitmap);

        // Run inference
        long startTime = SystemClock.uptimeMillis();
        // No need for try-catch here since there's no IOException possibility
        // Prepare inputs
        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, INPUT_SIZE, INPUT_SIZE, 3}, DataType.FLOAT32);
        inputFeature0.loadBuffer(imgData);

        // Run model inference
        BestFp16.Outputs outputs = model.process(inputFeature0);
        TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
        TensorBuffer outputFeature1 = outputs.getOutputFeature1AsTensorBuffer();
        TensorBuffer outputFeature2 = outputs.getOutputFeature2AsTensorBuffer();
        TensorBuffer outputFeature3 = outputs.getOutputFeature3AsTensorBuffer();

        // Process the output to get detected objects and their bounding boxes
        // Update this part based on the specific output format of your model
        // For example, you can use argmax to find the class with the highest confidence score
        int detectedClassIndex = 0;
        float maxConfidence = outputFeature0.getFloatValue(0); // Change index based on your output feature
        for (int i = 1; i < NUM_CLASSES; i++) {
            float confidence = outputFeature0.getFloatValue(i); // Change index based on your output feature
            if (confidence > maxConfidence) {
                maxConfidence = confidence;
                detectedClassIndex = i;
            }
        }

        // Draw bounding boxes on the original bitmap to visualize the detected objects
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        // You can use the 'detectedClassIndex' to get the class label from the 'LABELS' array
        String label = LABELS[detectedClassIndex];
        canvas.drawText(label, 50, 50, paint);

        // Display the processed frame on the screen

        // You can update your UI with the mutableBitmap here.
        // For example, if you have an ImageView with id 'imageView', you can set the bitmap like this:
        // ImageView imageView = findViewById(R.id.imageView);
        // imageView.setImageBitmap(mutableBitmap);

        long endTime = SystemClock.uptimeMillis();
        Log.d("InferenceTime", "Inference time: " + (endTime - startTime) + "ms");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //TODO show live camera footage
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setFragment();
        } else {

        }
    }

    //TODO fragment which show live footage from camera
    int previewHeight = 0, previewWidth = 0;

    protected void setFragment() {
        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String cameraId = null;
        try {
            String[] cameraIds = manager.getCameraIdList();
            if (cameraIds.length > 0) {
                cameraId = cameraIds[0];
            } else {
                // Handle the case when no camera is available
                // For example, display an error message or disable camera-related features
                // Or you may choose to do nothing and simply return from this method
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        Fragment fragment;
        CameraConnectionFragment camera2Fragment =
                CameraConnectionFragment.newInstance(
                        new CameraConnectionFragment.ConnectionCallback() {
                            @Override
                            public void onPreviewSizeChosen(final Size size, final int rotation) {
                                previewHeight = size.getHeight();
                                previewWidth = size.getWidth();
                                sensorOrientation = rotation - getScreenOrientation();
                            }
                        },
                        this,
                        R.layout.camera_fragment,
                        new Size(640, 640));

        camera2Fragment.setCamera(cameraId);
        fragment = camera2Fragment;

        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

    //TODO getting frames of live camera footage and passing them to model
    private boolean isProcessingFrame = false;
    private byte[][] yuvBytes = new byte[3][];
    private int[] rgbBytes = null;
    private Runnable postInferenceCallback;
    private Runnable imageConverter;
    private Bitmap rgbFrameBitmap;




    private Bitmap getBitmapFromImage(Image image) {
        // Get the image format
        int imageFormat = image.getFormat();
        if (imageFormat == ImageFormat.YUV_420_888) {
            // For YUV_420_888 format, you can directly convert it to a Bitmap
            Image.Plane[] planes = image.getPlanes();
            int width = image.getWidth();
            int height = image.getHeight();
            int pixelStride = planes[1].getPixelStride();
            int rowStride = planes[1].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            byte[] newData = new byte[width * height * 3 / 2];
            byte[] rowData = new byte[rowStride];
            int channelOffset = 0;
            int outputStride = 1;
            for (int row = 0; row < height; row++) {
                int outputOffset = row * width * 3 / 2;
                for (int col = 0; col < width; col++) {
                    int pixelOffset = col * pixelStride + channelOffset;
                    newData[outputOffset] = planes[1].getBuffer().get(pixelOffset);
                    outputOffset += outputStride;
                }
                channelOffset += rowPadding;
            }
            channelOffset = width * height;
            for (int row = 0; row < height / 2; row++) {
                int outputOffset = channelOffset;
                for (int col = 0; col < width / 2; col++) {
                    int pixelOffset = col * pixelStride + channelOffset;
                    newData[outputOffset] = planes[2].getBuffer().get(pixelOffset);
                    outputOffset += outputStride;
                }
                channelOffset += rowPadding;
            }
            YuvImage yuvImage = new YuvImage(newData, ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, width, height), 50, out);
            byte[] imageBytes = out.toByteArray();
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } else {
            // Handle other image formats here (if needed)
            return null;
        }
    }


    @Override
    public void onImageAvailable(ImageReader reader) {
// We need wait until we have some size from onPreviewSizeChosen
        if (previewWidth == 0 || previewHeight == 0) {
            return;
        }
        if (rgbBytes == null) {
            rgbBytes = new int[previewWidth * previewHeight];
        }
        try {
            final Image image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }

            if (isProcessingFrame) {
                image.close();
                return;
            }
            isProcessingFrame = true;
            final Image.Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);
            yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();

            imageConverter =
                    new Runnable() {
                        @Override
                        public void run() {
                            ImageUtils.convertYUV420ToARGB8888(
                                    yuvBytes[0],
                                    yuvBytes[1],
                                    yuvBytes[2],
                                    previewWidth,
                                    previewHeight,
                                    yRowStride,
                                    uvRowStride,
                                    uvPixelStride,
                                    rgbBytes);
                        }
                    };

            postInferenceCallback =
                    new Runnable() {
                        @Override
                        public void run() {
                            image.close();
                            isProcessingFrame = false;
                        }
                    };

            // Convert the captured frame to a Bitmap
            Bitmap bitmap = getBitmapFromImage(image);

            // Perform object detection on the Bitmap
            detectObjects(bitmap);

            // Process the image for inference
            processImage();
        } catch (final Exception e) {
            Log.d("tryError", e.getMessage());
            return;
        }
    }

    private void processImage() {
        imageConverter.run();
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        rgbFrameBitmap.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight);
        //Do your work here
        postInferenceCallback.run();
    }

    protected void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }
}
