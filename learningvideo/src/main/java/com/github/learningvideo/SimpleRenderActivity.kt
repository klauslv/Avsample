package com.github.learningvideo

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.learningvideo.opengl.drawer.BitmapDrawer
import com.github.learningvideo.opengl.drawer.IDrawer
import com.github.learningvideo.opengl.drawer.TriangleDrawer
import com.github.learningvideo.opengl.egl.SimpleRenderer
import kotlinx.android.synthetic.main.activity_simple_render.*

/**
 * 简单的页面渲染
 *
 *
 */
class SimpleRenderActivity : AppCompatActivity() {
    /**
     * 绘制三角形和纹理贴图，可以总结出Android中OpenGL ES的2D绘制流程
     * 1、通过GLSurfaceView配置OpenGL ES版本，指定Render
     * 2、实现GLSurfaceView.Renderer，复写暴露的方法，并配置OpenGL显示窗口，清屏
     * 3、创建纹理ID
     * 4、配置好顶点坐标和纹理坐标
     * 5、初始化坐标变换矩阵
     * 6、初始化OpenGL程序，并编译、链接顶点着色和片段着色器，获取GLSL中的变量属性
     * 7、激活纹理单元，绑定纹理ID，配置纹理过滤模式和环绕方式
     * 8、绑定纹理（如将bitmap绑定给纹理）
     * 9、启动绘制
     */

    //自定义的OpenGL渲染器，详情请继续往下看
    private lateinit var drawer: IDrawer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_render)

        drawer = if(intent.getIntExtra("type",0)==0){
            TriangleDrawer()
        }else{
            BitmapDrawer(BitmapFactory.decodeResource(CONTXT!!.resources,R.drawable.cover))
        }

        initRender(drawer)
    }

    private fun initRender(drawer: IDrawer) {
        gl_surface.setEGLContextClientVersion(2)
        val render = SimpleRenderer()
        render.addDrawer(drawer)
        gl_surface.setRenderer(render)
    }

    override fun onDestroy() {
        drawer.release()
        super.onDestroy()
    }


}