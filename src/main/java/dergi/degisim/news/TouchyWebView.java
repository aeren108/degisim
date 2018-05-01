package dergi.degisim.news;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * Created by Eren Colak on 1.05.2018.
 */

public class TouchyWebView extends WebView {

    public TouchyWebView(Context context) {
        super(context);
    }

    public TouchyWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchyWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        requestDisallowInterceptTouchEvent(true);
        return super.onTouchEvent(event);
    }
}
