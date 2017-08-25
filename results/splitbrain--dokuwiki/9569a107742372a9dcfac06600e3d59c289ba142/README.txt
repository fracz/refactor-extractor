commit 9569a107742372a9dcfac06600e3d59c289ba142
Author: Danny Lin <danny0838@pchome.com.tw>
Date:   Wed Jan 26 11:06:41 2011 +0800

    Major rework of rewrite block in handler.php. (FS#2145)

    -Simplify the algorithm. May improve performance.
    -Treat footnote as pure block and section as pure stack.
    -Remove post-p-open and pre-p-close linefeeds. Affects the effect of xbr plugin.