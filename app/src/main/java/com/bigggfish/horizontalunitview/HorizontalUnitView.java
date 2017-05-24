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
    private static final int DEFAULT_CHECKED_COLOR = 0xff2196F3;
    private static final int DEFAULT_NORMAL_COLOR = Color.LTGRAY;
    private static final int DEFAULT_BG_COLOR = 0xffffffff;
    private static final int DEFAULT_INNER_RADIUS = 6;
    private static final int DEFAULT_OUT_RADIUS = 12;
    private static final int DEFAULT_ITEM_COUNT = 4;
    private static final int DEFAULT_RING_STROKE_WIDTH = 1;
    private static final int DEFAULT_LINE_STROKE_WIDTH = 3;
    private static final int DEFAULT_TEXT_PADDING = 8;

    private Context mContext;

    //绘制处理
    private int mScreenWidth;//屏幕宽度
    private int mLineLength;//连线长度
    private int mPaddingLeft;//paddingleft
    private int mPaddingRight;//paddingright
    private int mPaddingTop;//paddingtop
    private int mPaddingBottom;//paddingbottom
    private int mTextPadding = DEFAULT_TEXT_PADDING;//text padding
    private int mOutRadius = DEFAULT_OUT_RADIUS;//外圆半径
    private int mInnerRadius = DEFAULT_INNER_RADIUS;//内圆半径
    private int mDefInnerRadius = DEFAULT_INNER_RADIUS;
    private int mOutDiam = DEFAULT_OUT_RADIUS * 2;//外圆直径
    //private int mInnerDiam = DEFAULT_INNER_RADIUS * 2;//内圆直径
    private int mWidth;//控件自己宽度
    private int mHeight;//控件自己高度
    private int mRingStrokeWidth = DEFAULT_RING_STROKE_WIDTH;//绘制圆环线宽度
    private int mLineStrokeWidth = DEFAULT_LINE_STROKE_WIDTH;//绘制连线宽度

    private int mItemCount = DEFAULT_ITEM_COUNT;//总item个数
    private int mCheckedNum;//选中item个数
    private int mVisibleNum;
    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
            14, getResources().getDisplayMetrics());//默认字体大小
    private int mCheckedColor = DEFAULT_CHECKED_COLOR;//选中item颜色
    private int mNormalColor = DEFAULT_NORMAL_COLOR;//默认item颜色
    private int mBgColor = DEFAULT_BG_COLOR;//背景颜色
    //绘制动画
    private ValueAnimator lineAnim;//绘制线动画
    private ValueAnimator pointAnim;//绘制线动画
    private float mRate = 0f;//绘制多少比率 0~1
    private int mCurrentDrawPosition = 0;//当前绘制第几个item
    private boolean isInitDraw = true;//是否是初始化动画绘制还是点击动画绘制

    //点击处理
    private int mTouchSlop;//最小点击距离
    private float downOldX;//点击位置X坐标
    private float downOldY;//点击位置Y坐标
    private OnItemClickListener mListener;
    //数据处理
    private Adapter mAdapter;
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
        mScreenWidth = dm.widthPixels;
        //点击最小距离
        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        setFocusable(true);
        setFocusableInTouchMode(true);
        //measureParams();
        initAnimator();
        mDefInnerRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_INNER_RADIUS, getResources().getDisplayMetrics());
        mOutRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_OUT_RADIUS, getResources().getDisplayMetrics());
        mInnerRadius = mDefInnerRadius;
        mOutDiam = mOutRadius * 2;
        mRingStrokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_RING_STROKE_WIDTH, getResources().getDisplayMetrics());
        mLineStrokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_LINE_STROKE_WIDTH, getResources().getDisplayMetrics());
    }

    /**
     * 初始化Paint
     */
    private void initPaint() {
        mPaintLine = new Paint();
        mPaintLine.setStrokeWidth(mLineStrokeWidth);

        mPaintBlueRing = new Paint();
        mPaintBlueRing.setStyle(Paint.Style.STROKE);
        mPaintBlueRing.setAntiAlias(true);
        mPaintBlueRing.setColor(mCheckedColor);
        mPaintBlueRing.setStrokeWidth(mRingStrokeWidth);

        mPaintBlueCircle = new Paint();
        mPaintBlueCircle.setStyle(Paint.Style.FILL);
        mPaintBlueCircle.setAntiAlias(true);
        mPaintBlueCircle.setColor(mCheckedColor);

        mPaintGreyCircle = new Paint();
        mPaintGreyCircle.setAntiAlias(true);
        mPaintGreyCircle.setColor(Color.GRAY);

        mTextPaint = new Paint();
        mTextPaint.setTextSize(mTextSize);
    }

    /**
     * 计算相关参数
     */
    private void measureParams() {
        //计算连线长度
        if (mItemCount != 0) {
            mLineLength = (mScreenWidth - mPaddingLeft - mPaddingRight - mOutDiam * mItemCount) / (mItemCount - 1);
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
        mPaddingLeft = getPaddingLeft();
        mPaddingRight = getPaddingRight();
        mPaddingTop = getPaddingTop();
        mPaddingBottom = getPaddingBottom();
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        if (widthSpecMode == MeasureSpec.AT_MOST) {
            widthSpecSize = mScreenWidth - lp.leftMargin - lp.rightMargin;
        }
        if (heightSpecMode == MeasureSpec.AT_MOST) {
            heightSpecSize = mPaddingTop + mPaddingBottom + mOutDiam + mTextPadding + (int) mTextSize + 1;
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

    /**
     * 初始化绘制item
     * @param canvas
     */
    private void initVisibleNum(Canvas canvas) {
        for (int i = 0; i < mItemCount; i++) {
            if (i < mVisibleNum) {//需要动画绘制个数
                if (i == mCheckedNum - 1) {//初始化绘制当前选择的点。
                    if (i < mCurrentDrawPosition) {
                        mInnerRadius = mDefInnerRadius;
                        drawInnerBlueCircle(canvas, mPaddingLeft + mOutRadius + i * (mLineLength + mOutDiam), mPaddingTop + mOutRadius);
                    } else if (i == mCurrentDrawPosition) {
                        mInnerRadius = (int) (mDefInnerRadius * mRate);
                        drawInnerBlueCircle(canvas, mPaddingLeft + mOutRadius + i * (mLineLength + mOutDiam), mPaddingTop + mOutRadius);
                    } else {
                        mInnerRadius = mDefInnerRadius;
                        drawInnerBlueCircle(canvas, mPaddingLeft + mOutRadius + i * (mLineLength + mOutDiam), mPaddingTop + mOutRadius);
                    }
                }

                if (i < mCurrentDrawPosition) {//动画完成item
                    drawBlueCircle(canvas, mPaddingLeft + mOutRadius + i * (mLineLength + mOutDiam), mPaddingTop + mOutRadius);
                    if (mTextList2 != null && mTextList2.size() > i) {
                        drawBlueText(canvas, mTextList2.get(i), mPaddingLeft + mOutRadius + i * (mLineLength + mOutDiam)
                                , mPaddingTop + mOutDiam + mTextPadding + (int) mTextSize);
                    }
                    if (i < mItemCount - 1) {
                        int startX = mPaddingLeft + mOutDiam + i * (mLineLength + mOutDiam);
                        int endXBlue = startX + mLineLength;
                        drawBlueLine(canvas, startX, mPaddingTop + mOutRadius, endXBlue, mPaddingTop + mOutRadius);
                    }
                } else if (i == mCurrentDrawPosition) {//绘制选中item，动画进行中
                    drawBlueCircle(canvas, mPaddingLeft + mOutRadius + i * (mLineLength + mOutDiam), mPaddingTop + mOutRadius);
                    if (mTextList2 != null && mTextList2.size() > i) {
                        drawBlueText(canvas, mTextList2.get(i), mPaddingLeft + mOutRadius + i * (mLineLength + mOutDiam)
                                , mPaddingTop + mOutDiam + mTextPadding + (int) mTextSize);
                    }
                    if (i < mItemCount - 1) {
                        int startX = mPaddingLeft + mOutDiam + i * (mLineLength + mOutDiam);
                        int endXBlue = startX + (int) (mLineLength * mRate);
                        int endXGrey = endXBlue + (int) (mLineLength * (1 - mRate));
                        drawBlueLine(canvas, startX, mPaddingTop + mOutRadius, endXBlue, mPaddingTop + mOutRadius);
                        drawGreyLine(canvas, endXBlue, mPaddingTop + mOutRadius, endXGrey, mPaddingTop + mOutRadius);
                    }
                } else {//动画未开始item
                    drawGreyCircle(canvas, mPaddingLeft + mOutRadius + i * (mLineLength + mOutDiam), mPaddingTop + mOutRadius);
                    if (mTextList2 != null && mTextList2.size() > i) {
                        drawGreyText(canvas, mTextList2.get(i), mPaddingLeft + mOutRadius + i * (mLineLength + mOutDiam)
                                , mPaddingTop + mOutDiam + mTextPadding + (int) mTextSize);
                    }
                    if (i < mItemCount - 1) {
                        int startX = mPaddingLeft + mOutDiam + i * (mLineLength + mOutDiam);
                        int endX = startX + mLineLength;
                        drawGreyLine(canvas, startX, mPaddingTop + mOutRadius, endX, mPaddingTop + mOutRadius);
                    }
                }

            } else {//灰色item
                drawGreyCircle(canvas, mPaddingLeft + mOutRadius + i * (mLineLength + mOutDiam), mPaddingTop + mOutRadius);
                if (mTextList2 != null && mTextList2.size() > i) {
                    drawGreyText(canvas, mTextList2.get(i), mPaddingLeft + mOutRadius + i * (mLineLength + mOutDiam)
                            , mPaddingTop + mOutDiam + mTextPadding + (int) mTextSize);
                }
                if (i < mItemCount - 1) {
                    int startX = mPaddingLeft + mOutDiam + i * (mLineLength + mOutDiam);
                    int endX = startX + mLineLength;
                    drawGreyLine(canvas, startX, mPaddingTop + mOutRadius, endX, mPaddingTop + mOutRadius);
                }
            }
        }
    }

    /**
     * 点击事件绘制item
     * @param canvas
     */
    private void drawCheckPos(Canvas canvas) {

        for (int i = 0; i < mItemCount; i++) {
            if (i < mVisibleNum) {
                if(i == mCheckedNum - 1){
                    mInnerRadius = (int) (mDefInnerRadius * mRate);
                    drawInnerBlueCircle(canvas, mPaddingLeft + mOutRadius + i * (mLineLength + mOutDiam), mPaddingTop + mOutRadius);
                }

                drawBlueCircle(canvas, mPaddingLeft + mOutRadius + i * (mLineLength + mOutDiam), mPaddingTop + mOutRadius);
                if (mTextList2 != null && mTextList2.size() > i) {
                    drawBlueText(canvas, mTextList2.get(i), mPaddingLeft + mOutRadius + i * (mLineLength + mOutDiam)
                            , mPaddingTop + mOutDiam + mTextPadding + (int) mTextSize);
                }
                if (i < mItemCount - 1) {
                    int startX = mPaddingLeft + mOutDiam + i * (mLineLength + mOutDiam);
                    int endXBlue = startX + mLineLength;
                    drawBlueLine(canvas, startX, mPaddingTop + mOutRadius, endXBlue, mPaddingTop + mOutRadius);
                }

            } else {
                drawGreyCircle(canvas, mPaddingLeft + mOutRadius + i * (mLineLength + mOutDiam), mPaddingTop + mOutRadius);
                if (mTextList2 != null && mTextList2.size() > i) {
                    drawGreyText(canvas, mTextList2.get(i), mPaddingLeft + mOutRadius + i * (mLineLength + mOutDiam)
                            , mPaddingTop + mOutDiam + mTextPadding + (int) mTextSize);
                }
                if (i < mItemCount - 1) {
                    int startX = mPaddingLeft + mOutDiam + i * (mLineLength + mOutDiam);
                    int endX = startX + mLineLength;
                    drawGreyLine(canvas, startX, mPaddingTop + mOutRadius, endX, mPaddingTop + mOutRadius);
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
        canvas.drawCircle(cx, cy, mOutRadius, mPaintBlueRing);
        //canvas.drawCircle(cx, cy, mInnerRadius2, mPaintBlueCircle);
    }

    private void drawInnerBlueCircle(Canvas canvas, int cx, int cy) {
        //canvas.drawCircle(cx, cy, mOutRadius2, mPaintBlueRing);
        canvas.drawCircle(cx, cy, mInnerRadius, mPaintBlueCircle);
    }

    private void drawGreyCircle(Canvas canvas, int cx, int cy) {
        canvas.drawCircle(cx, cy, mOutRadius, mPaintGreyCircle);
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
        int width = getMeasuredWidth() - mPaddingLeft - mPaddingRight;
        int num = (int) (x - mPaddingLeft) / ((width - mOutDiam) / (mItemCount - 1));
        int rem = (int) (x - mPaddingLeft) % ((width - mOutDiam) / (mItemCount - 1));
        if (rem < mOutDiam) {
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
        mItemCount = mAdapter.getCount();
        mTextList2.clear();
        for (int i = 0; i < mItemCount; i++) {
            mTextList2.add(mAdapter.getData(i));
        }
    }

    private void startLineAnimDraw() {
        lineAnim.removeAllListeners();
        lineAnim.removeAllUpdateListeners();
        lineAnim.setRepeatCount(mItemCount - 1);
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

    @Override
    public boolean performClick() {
        return super.performClick();
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
                        if (event.getX() - downOldX < mTouchSlop && event.getY() - downOldY < mTouchSlop) {
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
        if (position >= mVisibleNum || position < -1) {
            return;
            //throw new IllegalArgumentException("输入position不正确 position:" + position);
        }
        mCheckedNum = position + 1;
        isInitDraw = false;
        startPointAnimDraw();

    }

    /**
     * 当前可以点击是多少个
     *
     * @param visibleNum
     */
    public void setVisibleNum(int visibleNum) {
        if (visibleNum >= mTextList2.size() || visibleNum < -1) {
            return;
            //throw new IllegalArgumentException("输入visibleNum不正确 visibleNum:" + visibleNum);
        }
        if (mVisibleNum != visibleNum) {
            mCheckedNum = visibleNum;
            mVisibleNum = visibleNum;
        }
        isInitDraw = true;
        startLineAnimDraw();
    }

    /**
     * 设置适配器
     */
    public void setAdapter(Adapter mAdapter) {
        this.mAdapter = mAdapter;
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
