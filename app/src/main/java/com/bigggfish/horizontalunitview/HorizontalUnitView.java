package com.bigggfish.horizontalunitview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

public class HorizontalUnitView extends View {

    //默认值
    private static final int DEFAULT_CHECKED_COLOR = 0xff3773f5;
    private static final int DEFAULT_NORMAL_COLOR = 0xffd2dffd;
    private static final int DEFAULT_BG_COLOR = 0xffffffff;
    private static final int DEFAULT_INNER_RADIUS = 5;
    private static final int DEFAULT_OUT_RADIUS = 10;
    private static final int DEFAULT_ITEM_COUNT = 4;
    private static final int DEFAULT_RING_STROKE_WIDTH = 1;
    private static final int DEFAULT_LINE_STROKE_WIDTH = 2;
    private static final int DEFAULT_TEXT_PADDING = 8;

    private Context mContext;

    //绘制处理
    private int mScreenWidth2;//屏幕宽度
    private int mLineLength2;//连线长度
    private int mPaddingLeft2;//paddingleft
    private int mPaddingRight2;//paddingright
    private int mPaddingTop2;//paddingtop
    private int mPaddingBottom2;//paddingbottom
    private int mTextPadding2 = DEFAULT_TEXT_PADDING;//text padding
    private int mOutRadius2 = DEFAULT_OUT_RADIUS;//外圆半径
    private int mInnerRadius2 = DEFAULT_INNER_RADIUS;//内圆半径
    private int mDefInnerRadius2 = DEFAULT_INNER_RADIUS;
    private int mOutDiam2 = DEFAULT_OUT_RADIUS * 2;//外圆直径
    //private int mInnerDiam2 = DEFAULT_INNER_RADIUS * 2;//内圆直径
    private int mWidth2;//控件自己宽度
    private int mHeight2;//控件自己高度
    private int mRingStrokeWidth2 = DEFAULT_RING_STROKE_WIDTH;//绘制圆环线宽度
    private int mLineStrokeWidth2 = DEFAULT_LINE_STROKE_WIDTH;//绘制连线宽度

    private int mItemCount2 = DEFAULT_ITEM_COUNT;//总item个数
    private int mCheckedNum2;//选中item个数
    private int mVisibleNum2;
    private float mTextSize2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
            14, getResources().getDisplayMetrics());//默认字体大小
    private int mCheckedColor = DEFAULT_CHECKED_COLOR;//选中item颜色
    private int mNormalColor = DEFAULT_NORMAL_COLOR;//默认item颜色
    private int mBgColor = DEFAULT_BG_COLOR;//背景颜色
    //绘制动画
    private ValueAnimator lineAnim;//绘制线动画
    private ValueAnimator pointAnim;//绘制线动画
    private float mRate = 0f;//绘制多少比率 0~1
    private int mCurrentDrawPosition = 0;//当前绘制第几个item
    private boolean isInitDraw = true;//是否是初始化绘制

    //点击处理
    private int mTouchSlop2;//最小点击距离
    private float downOldX;//点击位置X坐标
    private float downOldY;//点击位置Y坐标
    private OnItemClickListener mListener;
    //数据处理
    private Adapter mAdapter2;
    private List<String> mTextList2 = new ArrayList<>();

    ///////////////////////OLD///////////////////////////
    private Paint mPaintLine;//绘制线画笔
    private Paint mPaintGreyCircle;//绘制灰色圆
    private Paint mPaintBlueCircle;//绘制蓝色圆
    private Paint mPaintBlueRing;//绘制蓝色圆环
    private Paint mTextPaint;//绘制文字

    public HorizontalUnitView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
        initPaint();

    }

    public HorizontalUnitView(Context context) {
        this(context, null);
    }

    /**
     * 初始化参数
     */
    private void init() {
        //初始化屏幕宽度
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mScreenWidth2 = dm.widthPixels;
        //点击最小距离
        mTouchSlop2 = ViewConfiguration.get(mContext).getScaledTouchSlop();
        setFocusable(true);
        setFocusableInTouchMode(true);
        //measureParams();
        initAnimator();
        mDefInnerRadius2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_INNER_RADIUS, getResources().getDisplayMetrics());
        mOutRadius2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_OUT_RADIUS, getResources().getDisplayMetrics());
        mInnerRadius2 = mDefInnerRadius2;
        mOutDiam2 = mOutRadius2 * 2;
        mRingStrokeWidth2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_RING_STROKE_WIDTH, getResources().getDisplayMetrics());
        mLineStrokeWidth2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_LINE_STROKE_WIDTH, getResources().getDisplayMetrics());
        mTextPadding2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_TEXT_PADDING, getResources().getDisplayMetrics());
    }

    /**
     * 初始化Paint
     */
    private void initPaint() {
        mPaintLine = new Paint();
        mPaintLine.setStrokeWidth(mLineStrokeWidth2);

        mPaintBlueRing = new Paint();
        mPaintBlueRing.setStyle(Paint.Style.STROKE);
        mPaintBlueRing.setAntiAlias(true);
        mPaintBlueRing.setColor(mCheckedColor);
        mPaintBlueRing.setStrokeWidth(mRingStrokeWidth2);

        mPaintBlueCircle = new Paint();
        mPaintBlueCircle.setStyle(Paint.Style.FILL);
        mPaintBlueCircle.setAntiAlias(true);
        mPaintBlueCircle.setColor(mCheckedColor);

        mPaintGreyCircle = new Paint();
        mPaintGreyCircle.setAntiAlias(true);
        mPaintGreyCircle.setColor(mNormalColor);

        mTextPaint = new Paint();
        mTextPaint.setTextSize(mTextSize2);
    }

    /**
     * 计算相关参数
     */
    private void measureParams() {
        //计算连线长度
        if (mItemCount2 != 0) {
            mLineLength2 = (mScreenWidth2 - mPaddingLeft2 - mPaddingRight2 - mOutDiam2 * mItemCount2) / (mItemCount2 - 1);
        }

    }

    private void initAnimator() {
        lineAnim = ValueAnimator.ofFloat(0, 1).setDuration(500);
        pointAnim = ValueAnimator.ofFloat(0, 1).setDuration(500);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        mPaddingLeft2 = getPaddingLeft();
        mPaddingRight2 = getPaddingRight();
        mPaddingTop2 = getPaddingTop();
        mPaddingBottom2 = getPaddingBottom();
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        if (widthSpecMode == MeasureSpec.AT_MOST) {
            widthSpecSize = mScreenWidth2 - lp.leftMargin - lp.rightMargin;
        }
        if (heightSpecMode == MeasureSpec.AT_MOST) {
            heightSpecSize = mPaddingTop2 + mPaddingBottom2 + mOutDiam2 + mTextPadding2 + (int) mTextSize2 + 1;
            //heightSpecSize = DisplayUtils.dp2px(getContext(), mHeight);
        }
        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        measureParams();
        drawBg(canvas);
        if(isInitDraw){
            initVisibleNum(canvas);
        }else{
            drawCheckPos(canvas);
        }
    }

    private void initVisibleNum(Canvas canvas) {
        for (int i = 0; i < mItemCount2; i++) {
            if (i < mVisibleNum2) {
                if (i == mCheckedNum2 - 1) {
                    if (i < mCurrentDrawPosition) {
                        mInnerRadius2 = mDefInnerRadius2;
                        drawInnerBlueCircle(canvas, mPaddingLeft2 + mOutRadius2 + i * (mLineLength2 + mOutDiam2), mPaddingTop2 + mOutRadius2);
                    } else if (i == mCurrentDrawPosition) {
                        mInnerRadius2 = (int) (mDefInnerRadius2 * mRate);
                        drawInnerBlueCircle(canvas, mPaddingLeft2 + mOutRadius2 + i * (mLineLength2 + mOutDiam2), mPaddingTop2 + mOutRadius2);
                    } else {
                        mInnerRadius2 = mDefInnerRadius2;
                        drawInnerBlueCircle(canvas, mPaddingLeft2 + mOutRadius2 + i * (mLineLength2 + mOutDiam2), mPaddingTop2 + mOutRadius2);
                    }
                }

                if (i < mCurrentDrawPosition) {
                    drawBlueCircle(canvas, mPaddingLeft2 + mOutRadius2 + i * (mLineLength2 + mOutDiam2), mPaddingTop2 + mOutRadius2);
                    if (mTextList2 != null && mTextList2.size() > i) {
                        drawBlueText(canvas, mTextList2.get(i), mPaddingLeft2 + mOutRadius2 + i * (mLineLength2 + mOutDiam2)
                                , mPaddingTop2 + mOutDiam2 + mTextPadding2 + (int) mTextSize2);
                    }
                    if (i < mItemCount2 - 1) {
                        int startX = mPaddingLeft2 + mOutDiam2 + i * (mLineLength2 + mOutDiam2);
                        int endXBlue = startX + mLineLength2;
                        drawBlueLine(canvas, startX, mPaddingTop2 + mOutRadius2, endXBlue, mPaddingTop2 + mOutRadius2);
                    }
                } else if (i == mCurrentDrawPosition) {
                    drawBlueCircle(canvas, mPaddingLeft2 + mOutRadius2 + i * (mLineLength2 + mOutDiam2), mPaddingTop2 + mOutRadius2);
                    if (mTextList2 != null && mTextList2.size() > i) {
                        drawBlueText(canvas, mTextList2.get(i), mPaddingLeft2 + mOutRadius2 + i * (mLineLength2 + mOutDiam2)
                                , mPaddingTop2 + mOutDiam2 + mTextPadding2 + (int) mTextSize2);
                    }
                    if (i < mItemCount2 - 1) {
                        int startX = mPaddingLeft2 + mOutDiam2 + i * (mLineLength2 + mOutDiam2);
                        int endXBlue = startX + (int) (mLineLength2 * mRate);
                        int endXGrey = endXBlue + (int) (mLineLength2 * (1 - mRate));
                        drawBlueLine(canvas, startX, mPaddingTop2 + mOutRadius2, endXBlue, mPaddingTop2 + mOutRadius2);
                        drawGreyLine(canvas, endXBlue, mPaddingTop2 + mOutRadius2, endXGrey, mPaddingTop2 + mOutRadius2);
                    }
                } else {
                    drawGreyCircle(canvas, mPaddingLeft2 + mOutRadius2 + i * (mLineLength2 + mOutDiam2), mPaddingTop2 + mOutRadius2);
                    if (mTextList2 != null && mTextList2.size() > i) {
                        drawGreyText(canvas, mTextList2.get(i), mPaddingLeft2 + mOutRadius2 + i * (mLineLength2 + mOutDiam2)
                                , mPaddingTop2 + mOutDiam2 + mTextPadding2 + (int) mTextSize2);
                    }
                    if (i < mItemCount2 - 1) {
                        int startX = mPaddingLeft2 + mOutDiam2 + i * (mLineLength2 + mOutDiam2);
                        int endX = startX + mLineLength2;
                        drawGreyLine(canvas, startX, mPaddingTop2 + mOutRadius2, endX, mPaddingTop2 + mOutRadius2);
                    }
                }

            } else {
                drawGreyCircle(canvas, mPaddingLeft2 + mOutRadius2 + i * (mLineLength2 + mOutDiam2), mPaddingTop2 + mOutRadius2);
                if (mTextList2 != null && mTextList2.size() > i) {
                    drawGreyText(canvas, mTextList2.get(i), mPaddingLeft2 + mOutRadius2 + i * (mLineLength2 + mOutDiam2)
                            , mPaddingTop2 + mOutDiam2 + mTextPadding2 + (int) mTextSize2);
                }
                if (i < mItemCount2 - 1) {
                    int startX = mPaddingLeft2 + mOutDiam2 + i * (mLineLength2 + mOutDiam2);
                    int endX = startX + mLineLength2;
                    drawGreyLine(canvas, startX, mPaddingTop2 + mOutRadius2, endX, mPaddingTop2 + mOutRadius2);
                }
            }
        }
    }

    private void drawCheckPos(Canvas canvas) {

        for (int i = 0; i < mItemCount2; i++) {
            if (i < mVisibleNum2) {
                if(i == mCheckedNum2 - 1){
                    mInnerRadius2 = (int) (mDefInnerRadius2 * mRate);
                    drawInnerBlueCircle(canvas, mPaddingLeft2 + mOutRadius2 + i * (mLineLength2 + mOutDiam2), mPaddingTop2 + mOutRadius2);
                }

                drawBlueCircle(canvas, mPaddingLeft2 + mOutRadius2 + i * (mLineLength2 + mOutDiam2), mPaddingTop2 + mOutRadius2);
                if (mTextList2 != null && mTextList2.size() > i) {
                    drawBlueText(canvas, mTextList2.get(i), mPaddingLeft2 + mOutRadius2 + i * (mLineLength2 + mOutDiam2)
                            , mPaddingTop2 + mOutDiam2 + mTextPadding2 + (int) mTextSize2);
                }
                if (i < mItemCount2 - 1) {
                    int startX = mPaddingLeft2 + mOutDiam2 + i * (mLineLength2 + mOutDiam2);
                    int endXBlue = startX + mLineLength2;
                    drawBlueLine(canvas, startX, mPaddingTop2 + mOutRadius2, endXBlue, mPaddingTop2 + mOutRadius2);
                }

            } else {
                drawGreyCircle(canvas, mPaddingLeft2 + mOutRadius2 + i * (mLineLength2 + mOutDiam2), mPaddingTop2 + mOutRadius2);
                if (mTextList2 != null && mTextList2.size() > i) {
                    drawGreyText(canvas, mTextList2.get(i), mPaddingLeft2 + mOutRadius2 + i * (mLineLength2 + mOutDiam2)
                            , mPaddingTop2 + mOutDiam2 + mTextPadding2 + (int) mTextSize2);
                }
                if (i < mItemCount2 - 1) {
                    int startX = mPaddingLeft2 + mOutDiam2 + i * (mLineLength2 + mOutDiam2);
                    int endX = startX + mLineLength2;
                    drawGreyLine(canvas, startX, mPaddingTop2 + mOutRadius2, endX, mPaddingTop2 + mOutRadius2);
                }
            }
        }
    }

    /**
     * 绘制背景
     */
    private void drawBg(Canvas canvas) {
        canvas.drawColor(mBgColor);
    }

    private void drawBlueCircle(Canvas canvas, int cx, int cy) {
        canvas.drawCircle(cx, cy, mOutRadius2, mPaintBlueRing);
        //canvas.drawCircle(cx, cy, mInnerRadius2, mPaintBlueCircle);
    }

    private void drawInnerBlueCircle(Canvas canvas, int cx, int cy) {
        //canvas.drawCircle(cx, cy, mOutRadius2, mPaintBlueRing);
        canvas.drawCircle(cx, cy, mInnerRadius2, mPaintBlueCircle);
    }

    private void drawGreyCircle(Canvas canvas, int cx, int cy) {
        canvas.drawCircle(cx, cy, mOutRadius2, mPaintGreyCircle);
    }

    private void drawGreyLine(Canvas canvas, int startX, int startY, int endX, int endY) {
        mPaintLine.setColor(mNormalColor);
        canvas.drawLine(startX, startY, endX, endY, mPaintLine);
    }

    private void drawBlueLine(Canvas canvas, int startX, int startY, int endX, int endY) {
        mPaintLine.setColor(mCheckedColor);
        canvas.drawLine(startX, startY, endX, endY, mPaintLine);
    }

    private void drawBlueText(Canvas canvas, String text, int x, int y) {
        mTextPaint.setColor(mCheckedColor);
        float textSize = mTextPaint.measureText(text);
        canvas.drawText(text, x - textSize / 2, y, mTextPaint);
    }

    private void drawGreyText(Canvas canvas, String text, int x, int y) {
        mTextPaint.setColor(mNormalColor);
        float textSize = mTextPaint.measureText(text);
        canvas.drawText(text, x - textSize / 2, y, mTextPaint);
    }

    //计算当前点击的位置
    private int measureClickPosition(float x, float y) {
        int width = getMeasuredWidth() - mPaddingLeft2 - mPaddingRight2;
        int num = (int) (x - mPaddingLeft2) / ((width - mOutDiam2) / (mItemCount2 - 1));
        int rem = (int) (x - mPaddingLeft2) % ((width - mOutDiam2) / (mItemCount2 - 1));
        if (rem < mOutDiam2) {
            return num;
        }
        return -1;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    /**
     * 提示重新绘制
     */
    public void notifyDataChange() {
        mItemCount2 = mAdapter2.getCount();
        mTextList2.clear();
        for (int i = 0; i < mItemCount2; i++) {
            mTextList2.add(mAdapter2.getData(i));
        }
     /*   if (mTextList2 != null && mTextList2.size() > 0) {
            if ((int) mTextPaint.measureText(mTextList2.get(0)) > mOutDiam2) {
                mPaddingLeft2 = (int) mTextPaint.measureText(mTextList2.get(0)) / 2 - mOutRadius2 + mPaddingLeft2;
            }

            if ((int) mTextPaint.measureText(mTextList2.get(mItemCount2 - 1)) > mOutDiam2) {
                mPaddingRight2 = (int) mTextPaint.measureText(mTextList2.get(mItemCount2 - 1)) / 2 - mOutRadius2 + mPaddingRight2;
            }
        }*/
    }

    private void startLineAnimDraw() {
        lineAnim.removeAllListeners();
        lineAnim.removeAllUpdateListeners();
        lineAnim.setRepeatCount(mItemCount2 - 1);
        lineAnim.setInterpolator(new DecelerateInterpolator(1f));
        lineAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRate = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mCurrentDrawPosition = 0;
        lineAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                mCurrentDrawPosition += 1;
              /*  if (mCurrentDrawPosition < mVisibleNum2) {
                    mCurrentDrawPosition += 1;
                }*/
            }
        });
        lineAnim.start();
    }

    private void startPointAnimDraw() {
        pointAnim.removeAllListeners();
        pointAnim.removeAllUpdateListeners();
        pointAnim.setInterpolator(new DecelerateInterpolator(1f));
        pointAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRate = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mCurrentDrawPosition = 0;
        pointAnim.start();
    }

    ///////////////GET_SET/////////////////////////////

    public OnItemClickListener getOnItemClickListener() {
        return mListener;
    }

    public void setOnItemClickListener(OnItemClickListener mListener) {
        this.mListener = mListener;
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downOldX = event.getX();
                        downOldY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (event.getX() - downOldX < mTouchSlop2 && event.getY() - downOldY < mTouchSlop2) {
                            if (HorizontalUnitView.this.mListener != null) {
                                int position = measureClickPosition(event.getX(), event.getY());
                                if (position != -1) {
                                    HorizontalUnitView.this.mListener.onItemClick(position);
                                }
                            }
                        }
                        downOldX = 0;
                        downOldY = 0;
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        downOldX = 0;
                        downOldY = 0;
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 设置选中下标
     *
     * @param position
     */
    public void setCheckedPos(int position) {
        if (position >= mVisibleNum2 || position < -1) {
            throw new IllegalArgumentException("输入position不正确 position:" + position);
        }
        mCheckedNum2 = position + 1;
        isInitDraw = false;
        startPointAnimDraw();

    }

    /**
     * 当前可以点击是多少个
     *
     * @param visibleNum
     */
    public void setVisibleNum(int visibleNum) {
        if (visibleNum > mTextList2.size() || visibleNum < -1) {
            throw new IllegalArgumentException("输入visibleNum不正确 visibleNum:" + visibleNum);
        }
        if (mVisibleNum2 != visibleNum) {
            mCheckedNum2 = visibleNum;
            mVisibleNum2 = visibleNum;
        }
        isInitDraw = true;
        startLineAnimDraw();
    }

    /**
     * 设置适配器
     *
     * @param mAdapter
     */
    public void setAdapter(Adapter mAdapter) {
        this.mAdapter2 = mAdapter;
        notifyDataChange();
    }

    /**
     * 适配器
     */
    public static abstract class Adapter<T> {

        private List<T> mData;

        public Adapter(List<T> data) {
            mData = data;
        }

        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        public Adapter setData(List<T> data) {
            mData = data;
            return this;
        }

        private String getData(int position) {
            return getText(mData.get(position));
        }

        public abstract String getText(T t);
    }
}
