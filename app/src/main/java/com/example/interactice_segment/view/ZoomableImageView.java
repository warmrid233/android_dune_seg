package com.example.interactice_segment.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.example.interactice_segment.R;

public class ZoomableImageView extends androidx.appcompat.widget.AppCompatImageView {

    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector; // 用于处理平移
    private Matrix matrix; // 图像变换矩阵
    private float scaleFactor = 1.f;
    private float[] lastEvent = new float[2]; // 用来记录手指的最后位置

    public ZoomableImageView(Context context) {
        super(context);
        init(context);
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        matrix = new Matrix();
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());
        DrawingView drawingView = findViewById(R.id.drawing_view);  // 获取 DrawingView 控件
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event); // 处理缩放手势
        gestureDetector.onTouchEvent(event); // 处理平移手势
        invalidate(); // 重新绘制视图
        return true;
    }

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
        canvas.save();
        canvas.concat(matrix); // 应用矩阵变换
        super.onDraw(canvas);
        canvas.restore();
    }

    // 处理缩放手势
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor(); // 获取缩放因子
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f)); // 限制缩放因子范围

            matrix.setScale(scaleFactor, scaleFactor); // 应用缩放变换

            return true;
        }
    }

    // 处理平移手势
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            // 记录初始触摸点
            lastEvent[0] = e.getX();
            lastEvent[1] = e.getY();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // 计算手指的拖动距离
            float dx = e2.getX() - lastEvent[0];
            float dy = e2.getY() - lastEvent[1];

            // 更新矩阵的平移部分
            matrix.postTranslate(dx, dy);

            // 更新偏移量

            // 记录当前触摸点
            lastEvent[0] = e2.getX();
            lastEvent[1] = e2.getY();

            // 同步更新 DrawingView 的位置
            return true;
        }
    }

    // 计算点击处在图像原始大小下的位置坐标
    public PointF getOriginalImageCoordinates(float clickX, float clickY) {
        // 获取当前矩阵的反矩阵
        Matrix inverseMatrix = new Matrix();
        matrix.invert(inverseMatrix);  // 获取当前变换的反向矩阵

        // 创建一个 PointF 对象来保存原始图像的坐标
        float[] coords = new float[] {clickX, clickY};

        // 将点击的屏幕坐标通过反矩阵转换为原始图像坐标
        inverseMatrix.mapPoints(coords);

        // 返回转换后的原始坐标
        return new PointF(coords[0], coords[1]);
    }

    public Matrix getCurrentMatrix()
    {
        return new Matrix(matrix);
    }

    public Bitmap getCurrentBitmap()
    {
        Drawable drawable = getDrawable();
        if (drawable == null) return null;

        return ((BitmapDrawable) drawable).getBitmap();
    }


    // 添加一个方法来重置图片到初始状态
    public void reset() {
        // 重置缩放因子和矩阵
        scaleFactor = 1.f;
        matrix.reset(); // 清除所有变换

        // 重新应用缩放变换
        matrix.setScale(scaleFactor, scaleFactor);

        // 重新绘制视图
        invalidate();

    }
}
