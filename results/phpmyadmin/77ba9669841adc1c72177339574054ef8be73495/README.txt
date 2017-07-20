commit 77ba9669841adc1c72177339574054ef8be73495
Author: Sebastian Mendel <cybot_tm@users.sourceforge.net>
Date:   Thu Mar 22 14:47:58 2007 +0000

    documentation;
    make use of session for SQL history;
    PMA_getRelationsParam caches result, to speed up multiple calls;
    smaller improvements;
    all functions are self responsible for getting $cfgRelation and validate it - no need to check before function call;