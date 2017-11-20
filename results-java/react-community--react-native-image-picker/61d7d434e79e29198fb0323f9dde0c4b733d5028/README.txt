commit 61d7d434e79e29198fb0323f9dde0c4b733d5028
Author: yfuks <yoannfuks@hotmail.fr>
Date:   Sat Feb 27 23:44:02 2016 +0100

    [Android] refactor cropping + lint

    - crop directly from library/camera intent instead of using "com.android.camera.action.CROP" since it's not officially part of the android API