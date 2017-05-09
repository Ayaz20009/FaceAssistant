package com.example.ayazshah.faceassistantglassapp;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder surfaceHolder = null;
    private Camera camera = null;

    @SuppressWarnings("deprecation")
    public CameraView(Context context) {
        super(context);

        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);

        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();

        this.setCameraParameters(camera);

        try {
            camera.setPreviewDisplay(holder);

        } catch (Exception e) {
            this.releaseCamera();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Start the preview for surfaceChanged
        if (camera != null) {
            camera.startPreview();
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        this.releaseCamera();
    }

    public void setCameraParameters(Camera camera) {
        if (camera != null) {
            Parameters parameters = camera.getParameters();
            parameters.setPreviewFpsRange(30000, 30000);
            camera.setParameters(parameters);
        }
    }

    public void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}