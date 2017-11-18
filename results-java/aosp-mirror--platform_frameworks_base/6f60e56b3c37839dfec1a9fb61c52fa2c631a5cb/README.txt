commit 6f60e56b3c37839dfec1a9fb61c52fa2c631a5cb
Author: Svet Ganov <svetoslavganov@google.com>
Date:   Mon Jul 14 08:21:25 2014 -0700

    Improve print options click to open behavior.

    When print options are closed we show a summary and a handle to open
    the options panel. Often the user instinctively clicks on the summary
    and also the expand handle is a somehow small target. To improve user
    experience clicking on the summary also opens the print options.

    Change-Id: Ia2f3b80f5acf11b40af864729f67fa29e82729fc