package com.privacyshield.android.Component.Screen.Overview.viewModel

import android.opengl.GLES20
import android.opengl.GLSurfaceView

import javax.microedition.khronos.opengles.GL10

class GpuInfoRenderer(
    private val onGpuInfoReady: (String, String, String, String) -> Unit
) : GLSurfaceView.Renderer {



    override fun onSurfaceCreated(gl: GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        val renderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: "Unknown"
        val vendor = GLES20.glGetString(GLES20.GL_VENDOR) ?: "Unknown"
        val version = GLES20.glGetString(GLES20.GL_VERSION) ?: "Unknown"
        val extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS) ?: "Unknown"

        onGpuInfoReady(renderer, vendor, version, extensions)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {}
    override fun onDrawFrame(gl: GL10?) {}
}
