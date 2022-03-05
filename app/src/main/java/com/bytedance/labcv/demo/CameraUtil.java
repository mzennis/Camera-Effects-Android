package com.bytedance.labcv.demo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;


import java.util.Arrays;

public class CameraUtil implements ImageReader.OnImageAvailableListener{

    private TextureView cameraPreview;
    private String cameraId;
    private CameraManager camManager;
    private Context context;
    private HandlerThread camBackgroundHandlerThread;
    private Handler camBackgroundHandler;
    private CameraDevice.StateCallback cameraStateCallback;
    private Size previewSize;
    private ImageReader imageReader;

    /**
     * Camera capture session's callback to handle raw frame data
     */
    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback(){
        private  void process(CaptureResult result){
            // process camera preview stream raw frame data
            
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }
    };

    /**
     * open the Camera
     */
    public void openCamera(){

        // permission checking required
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        try {
            camManager.openCamera(cameraId, cameraStateCallback, camBackgroundHandler);
        } catch (CameraAccessException e) {
            System.out.println("Failed when trying to open the camera!");
            e.printStackTrace();
        }
    }

    /**
     * setupFrontCamera
     */
    public void setupFrontCamera() {
        // create camera manager
        camManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] camIds = camManager.getCameraIdList();
            for (Integer index = 0; index < camIds.length; index++) {
                CameraCharacteristics camCharacter = camManager.getCameraCharacteristics(camIds[index]);
                // select front camera
                if (camCharacter.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    cameraId = camIds[index];
                    // get a list of camera preview sizes
                    Size[] previewSizes = camCharacter.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
                    // match the preview size to the UI TextureView size for display, with highest quality
                    for (index = 0; index < previewSizes.length; index++) {
                        Size currentPreviewSize = previewSizes[index];
                        // e.g. 4:3
                        // TODO: check camera orientation to fit the aspect ratio automatically below
                        Float previewAspectRatio = ((float) currentPreviewSize.getWidth()) / currentPreviewSize.getHeight();
                        Float textureViewAspectRatio = ((float) cameraPreview.getHeight()) / cameraPreview.getWidth();
                        Float epsilon = (float) 0.001;
                        // when camera preview and textureView has the same aspect ratio
                        if (Math.abs(previewAspectRatio - textureViewAspectRatio) < epsilon){
                            // take this previewSize for display
                            previewSize = currentPreviewSize;
                            imageReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.JPEG, 1);
                            imageReader.setOnImageAvailableListener(this, camBackgroundHandler);
                            return;
                        }
                    }
                }

            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * setup TextureView for displaying camera preview
     */
    public void setTextureView(TextureView textureView){
        if(textureView == null){
            System.out.println("texture view must NOT be empty");
        }
        cameraPreview = textureView;
    }


    /**
     * setup app context
     */
    public void setCameraContext(Context context){
        this.context = context;
    }

    /**
     * Initialize Camera Util before using it
     */
    public void initCameraStateCallback(){

        // create camera device state callback
        cameraStateCallback = new CameraDevice.StateCallback() {

            @Override
            public void onOpened(@NonNull CameraDevice cameraDevice) {

                System.out.println("Camera Open successful");
                cameraPreview.getSurfaceTexture().setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
                Surface previewSurface = new Surface(cameraPreview.getSurfaceTexture());
                try {
                    CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    captureRequestBuilder.addTarget(previewSurface);

                    cameraDevice.createCaptureSession(Arrays.asList(previewSurface, imageReader.getSurface()),
                            new CameraCaptureSession.StateCallback() {
                                @Override
                                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                                    try {
                                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(),
                                                null, camBackgroundHandler);
                                    } catch (CameraAccessException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                                }
                            }, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                System.out.println("Camera disconnected");
            }

            @Override
            public void onError(@NonNull CameraDevice cameraDevice, int i) {
                System.out.println("Camera state callback error");
            }
        };
    }

    /**
     * Background Camera Thread Handler
     */
    public void startBackgroundThread(){
        camBackgroundHandlerThread = new HandlerThread("Camera Video Thread");
        camBackgroundHandlerThread.start();
        camBackgroundHandler = new Handler(camBackgroundHandlerThread.getLooper());
    }

    public void stopBackgroundThread() throws InterruptedException {
        camBackgroundHandlerThread.quitSafely();
        camBackgroundHandlerThread.join();
    }


    /**
     * Image Available Listener
     * @param imageReader
     */
    @Override
    public void onImageAvailable(ImageReader imageReader) {
        Image image = imageReader.acquireLatestImage();
    }
}
