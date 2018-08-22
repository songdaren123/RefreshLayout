package demo.song.com.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class FootView extends View {
    public FootView(Context context) {
        this(context,null);
    }

    public FootView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
    }
}
