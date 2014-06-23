package com.xxworkshop.ui.imagecapture;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import com.xxworkshop.common.F;
import com.xxworkshop.common.formatter.Anchor;
import com.xxworkshop.common.formatter.Rect;

/**
 * Created by zhuyj on 2014/6/3.
 * Modified by broche on 2014/6/16
 */
public class FocusView extends View {
    private final int LineColor = Color.argb(0xFF, 0x80, 0x80, 0x80);
    private final int MaskColor = Color.argb(0xAF, 0x00, 0x00, 0x00);
    private final float StrokWidth = 3.0f;

    //画笔
    private Paint paint = new Paint();

    //截图框各边位置
    private int focusLeft = 0;
    private int focusTop = 0;
    private int focusRight = 0;
    private int focusBottom = 0;

    public FocusView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(LineColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(StrokWidth);
        canvas.drawRect(focusLeft, focusTop, focusRight, focusBottom, paint);    //绘制焦点框
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(MaskColor);
        canvas.drawRect(getLeft(), getTop(), getRight(), focusTop, paint);    //绘制焦点框上边阴影
        canvas.drawRect(getLeft(), focusTop, focusLeft, focusBottom + StrokWidth / 2, paint);    //绘制焦点框左边阴影
        canvas.drawRect(focusRight + StrokWidth / 2, focusTop, getRight(), focusBottom + StrokWidth / 2, paint);    //绘制焦点框右边边阴影
        canvas.drawRect(getLeft(), focusBottom + StrokWidth / 2, getRight(), getBottom(), paint);    //绘制焦点框下边阴影
    }

    public void init(Rect rect, Anchor anchor) {
        Rect trect = F.convertRect(rect, anchor);
        focusLeft = trect.x;
        focusTop = trect.y;
        focusRight = trect.x + trect.w;
        focusBottom = trect.y + trect.h;
    }

//    /**
//     * 返回焦点框左边位置
//     *
//     * @return
//     */
//    public int getFocusLeft() {
//        return focusLeft;
//    }
//
//    /**
//     * 返回焦点框上边位置
//     *
//     * @return
//     */
//    public int getFocusTop() {
//        return focusTop;
//    }
//
//    /**
//     * 返回焦点框右边位置
//     *
//     * @return
//     */
//    public int getFocusRight() {
//        return focusRight;
//    }
//
//    /**
//     * 返回焦点框下边位置
//     *
//     * @return
//     */
//    public int getFocusBottom() {
//        return focusBottom;
//    }
//
//    /**
//     * 返回焦点框中间点坐标
//     *
//     * @return
//     */
//    public PointF getFocusMidPoint() {
//        return mFocusMidPoint;
//    }
//
//    /**
//     * 返回焦点框宽度
//     *
//     * @return
//     */
//    public int getFocusWidth() {
//        return mFocusWidth;
//    }
//
//    /**
//     * 设置焦点框的宽度
//     *
//     * @param width
//     */
//    public void setFocusWidth(int width) {
//        this.mFocusWidth = width;
//        postInvalidate();
//    }
//
//    /**
//     * 返回阴影颜色
//     *
//     * @return
//     */
//    public int getHideColor() {
//        return MaskColor;
//    }
//
//    /**
//     * 设置阴影颜色
//     *
//     * @param color
//     */
//    public void setHidColor(int color) {
//        this.MaskColor = color;
//        postInvalidate();
//    }
//
//    /**
//     * 返回焦点框边框颜色
//     *
//     * @return
//     */
//    public int getFocusColor() {
//        return LineColor;
//    }
//
//    /**
//     * 设置焦点框边框颜色
//     *
//     * @param color
//     */
//    public void setFocusColor(int color) {
//        this.LineColor = color;
//        postInvalidate();
//    }
//
//    /**
//     * 返回焦点框边框绘制宽度
//     *
//     * @return
//     */
//    public float getStrokWidth() {
//        return StrokWidth;
//    }
//
//    /**
//     * 设置焦点边框宽度
//     *
//     * @param width
//     */
//    public void setStrokWidth(float width) {
//        this.StrokWidth = width;
//        postInvalidate();
//    }
}
