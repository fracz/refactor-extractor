commit c4174a004279281861ae1d4fc5fa4edbb552f58f
Author: Mike Penz <mikepenz@gmail.com>
Date:   Mon Jul 11 19:08:59 2016 +0200

    * add back `setBounds` into the size methods to improve behavior in `setCompoundDrawable` use cases and others.
     * This goes against the thing suggested here: https://code.google.com/p/android/issues/detail?id=210834&thanks=210834&ts=1463954825 but the issue was caused by the missing `onBoundsChange` method
     * FIX #https://github.com/gitskarios/Gitskarios/issues/494