package demo.song.com.refresh;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

public class RefreshLayout extends ViewGroup{
    private String TAG = RefreshLayout.class.getSimpleName();
    private HeadView mHeadView;
    private FootView mFootView;
    private RecyclerView mRecyclerView;
    private Scroller mScroller;

    private int headViewTop = 0;
    private int headviewBottom = 0;

    private int footViewTop = 0;
    private int footViewBottom = 0;

    private int contentViewTop = 0;
    private int contentViewBottom = 0;

    private int distancTop = 0;
    private List<LastY> lastYList;

    private int contentState = 0;
    private int mContentType = REFRESH_TYPE_DEFAULT;//刷新类型

    private static final int REFRESH_TYPE_DEFAULT = 0;

    private static final int REFRESH_STATE_IDLE = 0;
    private static final int REFRESH_STATE_DRAGGING = 1;
    private static final int REFRESH_STATE_SCROLLING_TO_HOLD_POSITION = 2;
    private static final int REFRESH_STATE_SCROLLING_TO_IDLE = 3;
    private static final int STATE_HOLDING_POSITION = 4;

    private OnRefreshListener mListener;

    public RefreshLayout(Context context) {
        this(context, null);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        distancTop = 42;
        mScroller = new Scroller(context);
        lastYList = new ArrayList<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int count = getChildCount();
        int height = 0;
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            if (view instanceof RecyclerView) {
                measureChild(view, widthMeasureSpec, MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) + distancTop, MeasureSpec.getMode(heightMeasureSpec)));
            } else {
                measureChild(view, widthMeasureSpec, heightMeasureSpec);
            }
            height = height + view.getMeasuredHeight();
        }
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.getMode(heightMeasureSpec)));

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            if (view instanceof HeadView) {
                headviewBottom = view.getMeasuredHeight();
                view.layout(l, headViewTop, r, headviewBottom);
                contentViewTop = headviewBottom;
                mHeadView = (HeadView) view;
            } else if (view instanceof RecyclerView) {
                contentViewBottom = view.getMeasuredHeight() + contentViewTop;
                view.layout(l, contentViewTop, r, contentViewBottom);
                footViewTop = contentViewBottom;
                mRecyclerView = (RecyclerView) view;
            } else if (view instanceof FootView) {
                footViewBottom = view.getMeasuredHeight() + footViewTop;
                view.layout(l, footViewTop, r, footViewBottom);
                mFootView = (FootView) view;
            }
        }
        scrollTo(0, contentViewTop);
    }

    @Override
    public void computeScroll() {
        switch (contentState) {
            case REFRESH_STATE_IDLE:
                break;
            case REFRESH_STATE_DRAGGING:
                Log.i(TAG, "computeScroll: REFRESH_STATE_DRAGGING");

                break;
            case REFRESH_STATE_SCROLLING_TO_HOLD_POSITION:
                scrollerToIdle();
                break;
            case STATE_HOLDING_POSITION:
                 break;
            case REFRESH_STATE_SCROLLING_TO_IDLE:
                contentState=REFRESH_STATE_IDLE;
                break;
        }
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastYList.add(new LastY((int) event.getY(event.getActionIndex()), event.getPointerId(event.getActionIndex())));
                break;
            case MotionEvent.ACTION_MOVE:
                int maxDistance = 0;
                for (int i = 0; i < event.getPointerCount(); i++) {//获取移动的最大距离
                    int pointId = event.getPointerId(i);
                    LastY lastY = lastYById(pointId);
                    int y = (int) event.getY(i);//获取每个点的滑动位置
                    int indexDistance = y - lastY.getIndexY();
                    lastY.setIndexY(y);//更新新值
                    if (Math.abs(indexDistance) > Math.abs(maxDistance)) {//进入可滑动状态
                        maxDistance = indexDistance;
                    }
                }
                if (recyclerTop() && maxDistance > 0 && contentState != REFRESH_STATE_DRAGGING) {
                    contentState = REFRESH_STATE_DRAGGING;
                    if (mListener != null) {
                        mListener.onRefresh();
                    }
                }
                if (contentState == REFRESH_STATE_DRAGGING) {
                    scrollBy(0, -(maxDistance / 2));
                    if (recyclerTop() && getScrollY() > contentViewTop) {//解决上拉造成的影响
                        scrollTo(0, contentViewTop);
                        contentState = REFRESH_STATE_IDLE;
                        return super.dispatchTouchEvent(event);
                    }
                    postInvalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                removeLastById(event.getPointerId(event.getActionIndex()));
                if (contentState == REFRESH_STATE_DRAGGING) {
                    if (mListener != null) {
                        mListener.onRefreshComplete();
                    }
                    scorllToHoldPosition();
                    return true;
                }

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                lastYList.add(new LastY(event.getActionIndex(), event.getPointerId(event.getActionIndex())));
                break;
            case MotionEvent.ACTION_POINTER_UP:
                removeLastById(event.getPointerId(event.getActionIndex()));
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private void scorllToHoldPosition() {
        contentState = REFRESH_STATE_SCROLLING_TO_HOLD_POSITION;
        if (getScaleY() <= contentViewTop) {
            mScroller.startScroll(0, getScrollY(), 0, contentViewTop - getScrollY());
        }
        postInvalidate();
    }

    private void scrollerToIdle() {
        contentState = REFRESH_STATE_SCROLLING_TO_IDLE;
        mScroller.startScroll(0, getScrollY(), 0, contentViewTop - getScrollY());
        postInvalidate();
    }

    /**
     * 判断RecyclerView是否移动到了顶端
     *
     * @return
     */
    private boolean recyclerTop() {
        LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        int firstIndex = manager.findFirstCompletelyVisibleItemPosition();//获取第一个可见得位置
        int childrenCount = manager.getChildCount();//获取所有item 总数
        return (childrenCount > 0 && firstIndex == 0);
    }

    private LastY lastYById(int id) {
        int pointId = 0;
        for (LastY lastY : lastYList) {
            if (lastY.getPointId() == id) {
                return lastY;
            }

        }
        return lastYList.get(pointId);
    }

    private void removeLastById(int id) {
        int index = 0;
        for (LastY lastY : lastYList) {
            if (lastY.getPointId() == id) {
                index = lastYList.indexOf(lastY);
            }
        }
        lastYList.remove(index);
    }

    private class LastY {
        private int indexY;
        private int pointId;

        public LastY(int indexY, int pointId) {
            this.indexY = indexY;
            this.pointId = pointId;
        }

        public int getIndexY() {
            return indexY;
        }

        public void setIndexY(int indexY) {
            this.indexY = indexY;
        }

        public int getPointId() {
            return pointId;
        }

        public void setPointId(int pointId) {
            this.pointId = pointId;
        }
    }

    public void setOnRefreshListener(OnRefreshListener mListener) {
        this.mListener = mListener;
    }

    public interface OnRefreshListener {
        /**
         * 下拉开始
         */
        void onRefresh();

        /**
         * 下拉完成
         */
        void onRefreshComplete();
    }

}
