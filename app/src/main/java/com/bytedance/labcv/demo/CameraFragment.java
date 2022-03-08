package com.bytedance.labcv.demo;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bef.effectsdk.OpenGLUtils;
import com.bytedance.labcv.core.util.LogUtils;
import com.bytedance.labcv.demo.databinding.FragmentCameraBinding;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraFragment extends Fragment
        implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private FragmentCameraBinding binding;
    private GLSurfaceView mSurfaceView;
    private int mTextureId;
    private SurfaceTexture mSurfaceTexture;
    private CameraDrawer mDrawer;
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
        cameraUtil.setCameraContext(context);
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set up GL Surface View for rendering camera preview
        initGLSurfaceView();

        // toggle effects upon button click
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

        cameraUtil.stopBackgroundThread();

        super.onPause();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ------------------------------- Util functions ------------------------------------------
    private void initGLSurfaceView(){
        mSurfaceView = binding.glview;
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        ConfigurationInfo ci = am.getDeviceConfigurationInfo();
        if(ci.reqGlEsVersion >= 0x30000)
        {
            mSurfaceView.setEGLContextClientVersion(3);
        }
        else
        {
            mSurfaceView.setEGLContextClientVersion(2);
        }

        // set GL Surface View listener
        mSurfaceView.setRenderer(this);
        mSurfaceView.setRenderMode(RENDERMODE_WHEN_DIRTY);

    }


    // ------------------------------- Listeners ------------------------------------------


    /**
     * Listeners for GLSurfaceView
     * */

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        System.out.println("GLSurfaceView is created");

        // get surface texture to render
        mTextureId = OpenGLUtils.getExternalOESTextureID();
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        // set up camera preview as rendering source
        cameraUtil.setSurfaceTexture(mSurfaceTexture);
        // create a camera drawer instance to render
        mDrawer = new CameraDrawer();

        // set up front camera
        cameraUtil.setupFrontCamera(mSurfaceView);
        // turn on camera immediately
        cameraUtil.initCameraStateCallback();
        // open camera
        cameraUtil.openCamera();

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        // {zh} 清空缓冲区颜色 {en} Clear buffer color
        //Clear buffer color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Convert the input texture to a 2D texture with a positive face

        // Update the texture image to the most recent frame from the image stream
        mSurfaceTexture.updateTexImage();
        // render
        mDrawer.draw(mTextureId, true);
    }


    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mSurfaceView.requestRender();
    }
}