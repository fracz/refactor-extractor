commit c132a94d6a429bd8c813c7e6c2d1ab50a7cbae8b
Author: BeezyT <timo@ezdesign.de>
Date:   Mon Nov 12 14:37:08 2012 +0000

    refs #2465

     * when Piwik is used over SSL, load the website in the frame over SSL too
     * when nothing comes back from the iframe, show an error message after a timeout (8 seconds)
     * improved handling of overlayUrl a bit (though this should be obsolete soon when we only have full screen mode)

    git-svn-id: http://dev.piwik.org/svn/trunk@7453 59fd770c-687e-43c8-a1e3-f5a4ff64c105