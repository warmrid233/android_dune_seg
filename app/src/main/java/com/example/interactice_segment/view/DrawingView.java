package com.example.interactice_segment.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.interactice_segment.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class DrawingView extends View
{
    private Matrix matrix = new Matrix(); // 图像变换矩阵
    private Paint paint;
    private List<float[]> points;
    private int points_num = 0; // 记录连接线交互中绘制的点的数量，便于撤回操作
    private Path path;
    private Bitmap bitmap;
    private Canvas canvas;
    private int method; // 记录当前交互模式: 0 - 图片缩放、移动， 1 - 点击， 2 - 连线， 3 - 画笔


    // 使用栈来记录每次绘制的路径（撤销）
    private Stack<Bitmap> undoStack = new Stack<>();

    public DrawingView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }


    public DrawingView(Context context)
    {
        super(context);
    }

    private void init()
    {
        method = 1;
        ZoomableImageView imageView = findViewById(R.id.img_show);

        paint = new Paint();
        paint.setColor(Color.GREEN); // 画笔颜色
        paint.setAntiAlias(true);
        paint.setStrokeWidth(3); // 设置画笔宽度
        paint.setStyle(Paint.Style.STROKE); // 设置为描边模式

        paint.setAntiAlias(true);

        path = new Path();
        setDrawingCacheEnabled(true);  // 启用绘制缓存
        points = new ArrayList<>();  // 初始化点的列表
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        // 创建一个Bitmap和Canvas，用于绘制
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas)
    {
        super.onDraw(canvas);
        // 绘制背景图
//        canvas.drawBitmap(bitmap, 0, 0, null);
        if(bitmap != null)
        {
            canvas.drawBitmap(bitmap, matrix, null);
        }
        if (method == 2) {
            drawAllLines();
        }
    }

    public void setMethod(int method)
    {
        this.method = method;
    }

    public void setColor(int color)
    {
        if(color == 1)
        {
            paint.setColor(Color.GREEN);
        }
        else if (color == 2)
        {
            paint.setColor((Color.RED));
        }
    }

    boolean newLine = false; // 用于记录画笔交互是否绘制了新的线条
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float x = event.getX();
        float y = event.getY();

        // 点击以外的交互重新计算坐标
        if(method != 0)
        {
            Matrix inverseMatrix = new Matrix();
            matrix.invert(inverseMatrix);  // 获取当前变换的反向矩阵
            // 创建一个 PointF 对象来保存原始图像的坐标
            float[] coords = new float[] {x, y};
            // 将点击的屏幕坐标通过反矩阵转换为原始图像坐标
            inverseMatrix.mapPoints(coords);
            x = coords[0];
            y = coords[1];
            if (x < 0 || x > bitmap.getWidth() || y < 0 || y > bitmap.getHeight())
            {
                return true;
            }
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (method == 1)
                {
                    canvas.drawCircle(x, y, 5, paint);
                    saveStateForUndo();
                }
                else if (method == 2)
                {
                    canvas.drawCircle(x, y, 5, paint);
                    // 将点击的坐标添加到列表中
                    points.add(new float[]{x, y});
                } else if (method == 3)
                {
                    path.moveTo(x, y); // 移动到起始点
                    return true;
                }
            case MotionEvent.ACTION_MOVE:
                if (method == 3)
                {
                    path.lineTo(x, y); // 画线
                    canvas.drawPath(path,paint);
                    newLine = true;
                    break;
                }

            case MotionEvent.ACTION_UP:
                // 结束触摸事件时，可以做一些清理操作或保存路径
                if(newLine)
                {
                    saveStateForUndo();
                    newLine = false;
                }
                break;
        }

        invalidate();  // 重新绘制视图
        return true;
    }

    // 重新绘制所有的点和线
    private void drawAllLines()
    {
        // 如果有两个以上的点，绘制路径
        if (points.size() > 1)
        {
            path.reset();
            float[] firstPoint = points.get(0);
            path.moveTo(firstPoint[0], firstPoint[1]);
            for (int i = 1; i < points.size(); i++)
            {
                float[] point = points.get(i);
                path.lineTo(point[0], point[1]);
            }
            canvas.drawPath(path, paint);
        }
        if(points.size() > points_num)
        {
            points_num = points.size();
            saveStateForUndo();
        }
    }

    private void saveStateForUndo()
    {
        Bitmap currentBitmap = bitmap.copy(bitmap.getConfig(), true);
        undoStack.push(currentBitmap);
    }

    // 撤销操作
    public void undo()
    {
        // 删除最后一个点
        if (!points.isEmpty() && method == 2)
        {
            points.remove(points.size() - 1);  // 移除最后一个点
            points_num--;
        }

        // 从撤回栈中恢复Bitmap
        if (!undoStack.isEmpty())
        {
            undoStack.pop();  // 弹出栈顶的元素
            path.rewind();
            if (!undoStack.isEmpty())
            {
                // 恢复栈顶的Bitmap
                bitmap = undoStack.peek().copy(bitmap.getConfig(), true);
            }
            else
            {
                // 如果栈为空，则清空画布
                bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            }

            // 创建新的Canvas，使用恢复后的bitmap
            canvas = new Canvas(bitmap);

            // 刷新视图，更新界面
            invalidate();
        }
    }

    public Bitmap getBitmap()
    {
        return this.bitmap;
    }

    public void setBitmap(Bitmap bitmap, Matrix matrix, float scale)
    {
//        this.bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scale),
//                (int) (bitmap.getHeight() * scale), true);
//        this.bitmap.setWidth((int) (bitmap.getWidth() * scale));
//        this.bitmap.setHeight((int) (bitmap.getHeight() * scale));
        this.bitmap = Bitmap.createBitmap((int) (bitmap.getWidth() * scale),
                (int) (bitmap.getHeight() * scale), Bitmap.Config.ARGB_8888);
        this.matrix = new Matrix(matrix);
        this.canvas = new Canvas(this.bitmap);
        saveStateForUndo();
        invalidate();
    }

    public void resetMatrix()
    {
        this.matrix.reset();
    }


    public void clear()
    {
        path.reset();
        points.clear();
        points_num = 0;
        undoStack.clear();
        bitmap.eraseColor(Color.TRANSPARENT);
        invalidate();
    }
}

