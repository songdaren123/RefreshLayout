package demo.song.com.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class HeadView extends View {
    public HeadView(Context context) {
        this(context,null);
    }

    public HeadView(Context context,  AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HeadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
    }
}
