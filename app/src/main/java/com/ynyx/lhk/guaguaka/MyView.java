package com.ynyx.lhk.guaguaka;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 孙群
 * http://blog.csdn.net/iispring
 */
public class MyView extends View {

    Paint paint;
    float cellSize = 0;
    float cellHorizontalOffset = 0;
    float cellVerticalOffset = 0;
    float circleRadius = 0;
    float rectSize = 0;
    static int circleColor = 0xffffcc44;//黄色
    static int rectColor = 0xff66aaff;//蓝色
    float textSize = getResources().getDimensionPixelSize(R.dimen.textSize);

    private static final Xfermode[] sModes = {
            new PorterDuffXfermode(PorterDuff.Mode.CLEAR),
            new PorterDuffXfermode(PorterDuff.Mode.SRC),
            new PorterDuffXfermode(PorterDuff.Mode.DST),
            new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER),
            new PorterDuffXfermode(PorterDuff.Mode.DST_OVER),
            new PorterDuffXfermode(PorterDuff.Mode.SRC_IN),
            new PorterDuffXfermode(PorterDuff.Mode.DST_IN),
            new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT),
            new PorterDuffXfermode(PorterDuff.Mode.DST_OUT),
            new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP),
            new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP),
            new PorterDuffXfermode(PorterDuff.Mode.XOR),
            new PorterDuffXfermode(PorterDuff.Mode.DARKEN),
            new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN),
            new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY),
            new PorterDuffXfermode(PorterDuff.Mode.SCREEN)
    };

    private static final String[] sLabels = {
            "Clear", "Src", "Dst", "SrcOver",
            "DstOver", "SrcIn", "DstIn", "SrcOut",
            "DstOut", "SrcATop", "DstATop", "Xor",
            "Darken", "Lighten", "Multiply", "Screen"
    };

    public MyView(Context context) {
        super(context);
        init(null, 0);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        if(Build.VERSION.SDK_INT >= 11){
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    // create a bitmap with a circle, used for the "dst" image
    static Bitmap makeDst(int w, int h) {
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        p.setColor(circleColor);
        c.drawOval(new RectF(0, 0, w*3/4, h*3/4), p);
        return bm;
    }

    // create a bitmap with a rect, used for the "src" image
    static Bitmap makeSrc(int w, int h) {
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        p.setColor(rectColor);
        c.drawRect(w/3, h/3, w*19/20, h*19/20, p);
        return bm;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //设置背景色
        canvas.drawARGB(255, 139, 197, 186);

        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        for(int row = 0; row < 4; row++){
            for(int column = 0; column < 4; column++){
                //canvas.save();
                int index = row * 4 + column;
                float translateX = (cellSize + cellHorizontalOffset) * column;
                float translateY = (cellSize + cellVerticalOffset) * row;

                int layer = canvas.saveLayer(0, 0, canvasWidth, canvasHeight, null, Canvas.ALL_SAVE_FLAG);
                paint.reset();
                paint.setTextSize(textSize);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setStrokeWidth(2);
                paint.setFilterBitmap(false);
                paint.setXfermode(null);

                //画文字
                canvas.translate(translateX, translateY);
                String text = sLabels[index];
                paint.setColor(Color.BLACK);
                float textXOffset = cellSize / 2;
                float textYOffset = textSize + (cellVerticalOffset - textSize) / 2;
                canvas.drawText(text, textXOffset, textYOffset, paint);

                //画边框
                canvas.translate(0, cellVerticalOffset);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(0xff000000);
                canvas.drawRect(2, 2, cellSize - 2, cellSize - 2, paint);

//                //画圆
//                paint.setStyle(Paint.Style.FILL);
//                paint.setColor(circleColor);
//                float left = circleRadius + 3;
//                float top = circleRadius + 3;
//                canvas.drawCircle(left, top, circleRadius, paint);
//
//                //画矩形
//                paint.setXfermode(sModes[index]);
//                paint.setColor(rectColor);
//                float rectRight = circleRadius + rectSize;
//                float rectBottom = circleRadius + rectSize;
//                canvas.drawRect(left, top, rectRight, rectBottom, paint);

                canvas.translate(5, 5);
                canvas.drawBitmap(makeDst((int) circleRadius*2,(int) circleRadius*2), 0, 0, paint);

                paint.setXfermode(sModes[index]);
                canvas.drawBitmap(makeSrc((int) circleRadius*2,(int) circleRadius*2), 0, 0, paint);

                paint.setXfermode(null);
//                canvas.restore();
                canvas.restoreToCount(layer);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cellSize = w / 4.5f;
        cellHorizontalOffset = cellSize / 6;
        cellVerticalOffset = cellSize * 0.426f;
        circleRadius = cellSize / 3;
        rectSize = cellSize * 0.6f;
    }
}