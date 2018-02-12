package me.iologic.apps.dtn;

import com.wang.avi.AVLoadingIndicatorView;

/**
 * Created by vinee on 12-02-2018.
 */

public class Indicators {

    void startAnim(AVLoadingIndicatorView avi) {
        avi.show();
        // or avi.smoothToShow();
    }

    void stopAnim(AVLoadingIndicatorView avi) {
        avi.hide();
        // or avi.smoothToHide();
    }

}
