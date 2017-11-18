commit 462fdfbd58ba83c2d17dd417311c6d24b226b327
Author: wesandevie <wesandevie@gmail.com>
Date:   Fri Oct 30 10:19:32 2009 +0000

    - in order to implement issue 110
    -- refactored to move Serializable implementation to the base interfaces so future implementation will be Serializable and get warning for missing serial version id.
    -- added annotations to ignore serial version id warnings in test classes

    --HG--
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%401640