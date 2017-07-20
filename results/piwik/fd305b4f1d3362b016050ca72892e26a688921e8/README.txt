commit fd305b4f1d3362b016050ca72892e26a688921e8
Author: diosmosis <benaka@piwik.pro>
Date:   Tue Feb 24 18:09:02 2015 -0800

    Refs #7276, initial backwards compatible refactor of updater classes (moved CoreUpdater static functions to core/Updater class, remove use of static methods in Updater, don't use static method setUpdater in ColumnsUpdater).