package com.shuyu.gsyvideoplayer.render.effect;

import android.opengl.GLSurfaceView;

import com.shuyu.gsyvideoplayer.render.view.GSYVideoGLView.ShaderInterface;

/**
 * Attempts to auto-fix the video based on histogram equalization.
 *
 * @author sheraz.khilji
 */
public class AutoFixEffect implements ShaderInterface {
    private float scale;

    /**
     * Initialize Effect
     *
     * @param scale Float, between 0 and 1. Zero means no adjustment, while 1
     *              indicates the maximum amount of adjustment.
     */
    public AutoFixEffect(float scale) {
        if (scale < 0.0f)
            scale = 0.0f;
        if (scale > 1.0f)
            scale = 1.0f;

        this.scale = scale;
    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {

        String shader = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform samplerExternalOES tex_sampler_0;\n"
                + "varying vec2 vTextureCoord;\n"
                + "void main() {\n"
                + "  float scale = " + scale + ";\n"
                + "  const vec3 weights = vec3(0.33333, 0.33333, 0.33333);\n"
                + "  vec4 color = texture2D(tex_sampler_0, vTextureCoord);\n"
                + "  float energy = dot(color.rgb, weights);\n"
                + "  vec3 balanced = smoothstep(0.0, 1.0, color.rgb);\n"
                + "  vec3 contrast = mix(vec3(energy), balanced, 0.85);\n"
                + "  gl_FragColor = vec4(mix(color.rgb, contrast, scale), color.a);\n"
                + "}\n";

        return shader;

    }
}
