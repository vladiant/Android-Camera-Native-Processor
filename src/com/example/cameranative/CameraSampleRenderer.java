package com.example.cameranative;

import android.content.Context;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.EGLContextFactory;

public class CameraSampleRenderer implements GLSurfaceView.Renderer,
		EGLContextFactory {

	private GLSurfaceView mSurface;

	private static final String TAG = "CameraSampleRenderer";

	public CameraSampleRenderer(Context context) {
		mSurface = new GLSurfaceView(context);
		mSurface.setEGLContextFactory(this);
		mSurface.setEGLConfigChooser(new ConfigChooser(5, 6, 5, 0, 16, 0));
		mSurface.setRenderer(this);
	}

	public GLSurfaceView getGLSurfaceView() {
		return mSurface;
	}
	
	/// Native functions start
	public native void surfaceChanged(int w, int h);

	public native void render();

	public native void cleanup();

	public native float getGPUFPS();
	/// Native functions end

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	}

	public void onSurfaceChanged(GL10 gl, int w, int h) {
		surfaceChanged(w, h);
	}

	public void onDrawFrame(GL10 gl) {
		render();
	}

	public EGLContext createContext(EGL10 egl, EGLDisplay display,
			EGLConfig eglConfig) {

		final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
		int[] attr = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };

		return egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT,
				attr);
	}

	public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
		cleanup();
		egl.eglDestroyContext(display, context);
	}

	private static class ConfigChooser implements
			GLSurfaceView.EGLConfigChooser {

		public ConfigChooser(int r, int g, int b, int a, int depth, int stencil) {
			mRedSize = r;
			mGreenSize = g;
			mBlueSize = b;
			mAlphaSize = a;
			mDepthSize = depth;
			mStencilSize = stencil;
		}

		private EGLConfig getMatchingConfig(EGL10 egl, EGLDisplay display,
				int[] configAttribs) {
			int[] num_config = new int[1];
			egl.eglChooseConfig(display, configAttribs, null, 0, num_config);

			int numConfigs = num_config[0];
			if (numConfigs <= 0)
				throw new IllegalArgumentException("No matching EGL configs");

			EGLConfig[] configs = new EGLConfig[numConfigs];
			egl.eglChooseConfig(display, configAttribs, configs, numConfigs,
					num_config);

			// printConfigs(egl, display, configs);

			return chooseConfig(egl, display, configs);
		}

		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
			final int EGL_OPENGL_ES2_BIT = 0x0004;
			final int[] s_configAttribs_gl20 = { EGL10.EGL_RED_SIZE, 4,
					EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4,
					EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
					EGL10.EGL_NONE };

			return getMatchingConfig(egl, display, s_configAttribs_gl20);
		}

		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
				EGLConfig[] configs) {
			for (EGLConfig config : configs) {
				int d = findConfigAttrib(egl, display, config,
						EGL10.EGL_DEPTH_SIZE, 0);
				int s = findConfigAttrib(egl, display, config,
						EGL10.EGL_STENCIL_SIZE, 0);

				if (d < mDepthSize || s < mStencilSize)
					continue;

				int r = findConfigAttrib(egl, display, config,
						EGL10.EGL_RED_SIZE, 0);
				int g = findConfigAttrib(egl, display, config,
						EGL10.EGL_GREEN_SIZE, 0);
				int b = findConfigAttrib(egl, display, config,
						EGL10.EGL_BLUE_SIZE, 0);
				int a = findConfigAttrib(egl, display, config,
						EGL10.EGL_ALPHA_SIZE, 0);

				if (r == mRedSize && g == mGreenSize && b == mBlueSize
						&& a == mAlphaSize)
					return config;
			}

			return null;
		}

		private int findConfigAttrib(EGL10 egl, EGLDisplay display,
				EGLConfig config, int attribute, int defaultValue) {
			if (egl.eglGetConfigAttrib(display, config, attribute, mValue))
				return mValue[0];

			return defaultValue;
		}

		protected int mRedSize;
		protected int mGreenSize;
		protected int mBlueSize;
		protected int mAlphaSize;
		protected int mDepthSize;
		protected int mStencilSize;
		private int[] mValue = new int[1];
	}
}
