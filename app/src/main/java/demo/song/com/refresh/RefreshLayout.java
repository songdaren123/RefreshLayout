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
    private String TAG=RefreshLayout.class.getSimpleName();
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

    private int distancTop=0;
    private List<LastY> lastYList;

    private int contentState=0;
    private int mContentType=REFRESH_TYPE_DEFAULT;//刷新类型

    private static final int REFRESH_TYPE_DEFAULT = 0;
    private static final int REFRESH_TYPE_HEAD = 1;
    private static final int REFRESH_TYPE_BOTTOM = 2;

    private static final int REFRESH_STATE_DEFAULT = 0;
    private static final int REFRESH_STATE_START = 1;
    private static final int REFRESH_STATE_SCROLLING = 2;
    private static final int REFRESH_STATE_END = 3;


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
            case REFRESH_STATE_DEFAULT:
                break;
            case REFRESH_STATE_START:
                break;
            case REFRESH_STATE_SCROLLING:
                break;
            case REFRESH_STATE_END:
                break;

        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "onTouchEvent: MotionEvent.ACTION_DOWN:"+event.getPointerCount());
                lastYList.add(new LastY(event.getActionIndex(),event.getPointerId(event.getActionIndex())));
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
                Log.i(TAG, "dispatchTouchEvent: " + maxDistance);
                if (recyclerTop() && maxDistance > 0) {
//                    scrollBy(0,-(maxDistance/2));
                    contentState=REFRESH_STATE_START;
//                    return true;
                }
                if(contentState==REFRESH_STATE_START){
                    scrollBy(0,-(maxDistance/2));
                    postInvalidate();
                    return true;
                }
                Log.i(TAG, "onTouchEvent: MotionEvent.ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                removeLastById(event.getPointerId(event.getActionIndex()));
                Log.i(TAG, "onTouchEvent: MotionEvent.ACTION_MOVE");
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.i(TAG, "onTouchEvent: ACTION_POINTER_DOWN");
                lastYList.add(new LastY(event.getActionIndex(),event.getPointerId(event.getActionIndex())));
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.i(TAG, "onTouchEvent: ACTION_POINTER_DOWN");
                removeLastById(event.getPointerId(event.getActionIndex()));
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * 判断RecyclerView是否移动到了顶端
     * @return
     */
    private boolean recyclerTop() {
        LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        int firstIndex = manager.findFirstCompletelyVisibleItemPosition();//获取第一个可见得位置
        int childrenCount = manager.getChildCount();//获取所有item 总数
        Log.i(TAG, "recyclerTop: " + firstIndex);
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
    public interface OnRefreshListener {
        /**
         * Called when a swipe gesture triggers a refresh.
         */
        void onRefresh();
    }

}
