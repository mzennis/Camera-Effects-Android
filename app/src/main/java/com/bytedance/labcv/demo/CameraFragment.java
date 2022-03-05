package com.bytedance.labcv.demo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bytedance.labcv.demo.databinding.FragmentCameraBinding;

public class CameraFragment extends Fragment
        implements TextureView.SurfaceTextureListener {

    private FragmentCameraBinding binding;
    private TextureView cameraPreview;
    private Context context;
    private CameraUtil cameraUtil;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // init
        context = this.getContext();
        // initialize a camera util
        cameraUtil = new CameraUtil();

        binding = FragmentCameraBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set up camera preview (textureView) listener
        cameraPreview = binding.cameraPreview;
        cameraPreview.setSurfaceTextureListener(this);

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraUtil.setCameraContext(context);
                cameraUtil.setTextureView(cameraPreview);
                // set up front camera
                cameraUtil.setupFrontCamera();
                // turn on camera immediately
                cameraUtil.initCameraStateCallback();
                cameraUtil.openCamera();
            }
        });
    }

    @Override
    public void onResume() {

        super.onResume();
        cameraUtil.startBackgroundThread();
    }


    @Override
    public void onPause() {
        try {
            cameraUtil.stopBackgroundThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onPause();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ------------------------------- Listeners ------------------------------------------


    /**
     * Listeners for cameraPreview TextureView
     * */
    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
        System.out.println("Surface Texture Available");
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

    }

}