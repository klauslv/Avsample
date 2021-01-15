package com.github.learningvideo.opengl.egl

import android.content.Context
import android.graphics.PointF
import android.opengl.GLSurfaceView
import android.util.AttributeSet

/**
 * Created by lvming on 1/12/21 11:05 AM.
 * Email: lvming@guazi.com
 * Description: 自定义GLSUrfaceview
 */
class DefGLSurfaceView : GLSurfaceView {

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    private var mPrePoint = PointF()

//    private var mDrawer:
}