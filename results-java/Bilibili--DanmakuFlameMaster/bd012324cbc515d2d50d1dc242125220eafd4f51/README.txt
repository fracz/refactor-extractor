commit bd012324cbc515d2d50d1dc242125220eafd4f51
Author: ctiao <calmer91@gmail.com>
Date:   Sat Aug 13 16:49:58 2016 +0800

    refactor: move settings of AndroidDisplayer into AndroidDisplayer.DisplayerConfig
    move AndroidDisplayer.drawDanmaku into SimpleTextCacheStuffer.drawDanmaku
    add drawCache(...) method to BaseCacheStuffer
    remove drawText(...)|drawStroke(...)|drawBackground(...) methods from BaseCacheStuffer