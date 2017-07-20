commit 0a0a0d2cdef310e98636ef1b68fc4f9d4108b235
Author: diosmosis <benaka@piwik.pro>
Date:   Tue Dec 9 17:01:25 2014 -0800

    Refs #2624, add INI config options that will force a new visit if campaign/website referrer information changes between actions. Includes new tracker hook shouldForceNewVisit that dimensions can use to force a new visit during tracking. Also includes light refactoring to referrer dimensions. Testless.