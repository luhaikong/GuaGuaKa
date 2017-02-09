package com.ynyx.lhk.guaguaka;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by lhk on 2017/2/7.
 */

public class GuaKa extends View {

    /**
     * 绘制线条的Paint,即用户手指绘制Path
     */
    private Paint mFingerPaint = new Paint();
    /**
     * 记录用户绘制的Path
     */
    private Path mFingerPath = new Path();

    /**
     * 是否刮奖完成
     */
    private boolean isComplete;
    /**
     * 覆盖层的Paint
     */
    private Paint mForegroundPaint = new Paint();

    /**
     * canvas绘制内容在其上
     */
    private Bitmap mBitmap;
    private Canvas mCanvas;

    private Paint mTextPaint = new Paint();
    private String mText = "￥500,0000";
    private Rect mTextBound = new Rect();
    /**
     * 手指触摸位置
     */
    private int mLastX;
    private int mLastY;
    private int mWidth,mHeight;

    public GuaKa(Context context) {
        this(context, null);
    }

    public GuaKa(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GuaKa(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mFingerPath = new Path();
        mTextPaint = new Paint();
        mTextPaint = setTextPaint();
        mFingerPaint = setFingerPaint();
        mForegroundPaint = setForegroundPaint();
    }

    private Paint setTextPaint(){
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setTextScaleX(2f);
        paint.setTextSize(32);
        paint.setColor(Color.DKGRAY);
        paint.getTextBounds(mText, 0, mText.length(), mTextBound);
        return paint;
    }

    /**
     * 初始化canvas的绘制用的画笔
     */
    private Paint setForegroundPaint() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#c0c0c0"));
        return paint;
    }

    /**
     * 设置画笔的一些参数
     */
    private Paint setFingerPaint() {
        Paint paint = new Paint();
        // 设置画笔
        // paint.setAlpha(0);
        paint.setColor(Color.parseColor("#c0c0c0"));
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND); // 圆角
        paint.setStrokeCap(Paint.Cap.ROUND); // 圆角
        // 设置画笔宽度
        paint.setStrokeWidth(20);
        return paint;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

        // 绘制缓存的Bitmap
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mFingerPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawRoundRect(new RectF(0, 0, mWidth, mHeight), 30, 30, mFingerPaint);
        mCanvas.drawBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.s_title), null, new RectF(0, 0, mWidth, mHeight), null);
    }

    /**
     * 绘制缓存中的Bitmap
     */
    private void drawCachePath(){
        mFingerPaint.setStyle(Paint.Style.STROKE);
        mFingerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mCanvas.drawPath(mFingerPath,mFingerPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制文字
        canvas.drawText(mText, getWidth() / 2 - mTextBound.width() / 2,
                getHeight() / 2 + mTextBound.height() / 2, mTextPaint);

//        int layer = canvas.saveLayer(0, 0, mWidth, mHeight, null, Canvas.ALL_SAVE_FLAG);

        //是否刮卡完成
        if (!isComplete) {
            drawCachePath();
            //将缓存中的Bitmap绘制到canvas中
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }

//        canvas.restoreToCount(layer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                mFingerPath.moveTo(mLastX, mLastY);
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = Math.abs(x - mLastX);
                int dy = Math.abs(y - mLastY);

                if (dx > 3 || dy > 3)
                    mFingerPath.lineTo(x, y);

                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                new Thread(mRunnable).start();
                break;
        }

        invalidate();
        return true;
    }

    /**
     * 统计擦除区域任务
     */
    private Runnable mRunnable = new Runnable() {
        private int[] mPixels;

        @Override
        public void run() {

            int w = mWidth;
            int h = mHeight;

            float wipeArea = 0;
            float totalArea = w * h;

            Bitmap bitmap = mBitmap;

            mPixels = new int[w * h];

            /**
             * 拿到所有的像素信息
             */
            bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);

            /**
             * 遍历统计擦除的区域
             */
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    int index = i + j * w;
                    if (mPixels[index] == 0) {
                        wipeArea++;
                    }
                }
            }

            /**
             * 根据所占百分比，进行一些操作
             */
            if (wipeArea > 0 && totalArea > 0) {
                int percent = (int) (wipeArea * 100 / totalArea);
                Log.e("TAG", percent + "");

                if (percent > 70) {
                    Log.e("TAG", "清除区域达到70%，下面自动清除");
                    isComplete = true;
                    postInvalidate();
                }
            }
        }

    };

}
