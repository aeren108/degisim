// -*- @author aeren_pozitif  -*- //
package dergi.degisim.news;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * This class is for making WebView sliding in a NestedView.
 */

public class SlidableWebView extends WebView {

    public SlidableWebView(Context context) {
        super(context);
    }

    public SlidableWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlidableWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //This line disallows to intercepting of touch events in NestedView
        requestDisallowInterceptTouchEvent(true);
        return super.onTouchEvent(event);
    }
}
