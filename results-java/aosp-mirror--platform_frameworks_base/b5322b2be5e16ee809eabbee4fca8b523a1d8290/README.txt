commit b5322b2be5e16ee809eabbee4fca8b523a1d8290
Author: songjinshi <songjinshi@xiaomi.com>
Date:   Wed Mar 15 16:16:05 2017 +0800

    [Stability]: fix system_server crash issue caused by fd leak.

    If an app incorrect use of registerListener, it will cause
    system_server socket fd leak, for example:
        protected void onResume() {
            super.onResume();
            mSensorManager.registerListener(new MySensorEventListener(),
                    mSensor, SensorManager.SENSOR_DELAY_UI);
    Each time a new SensorEventQueue, then in the system_server
    will open a new socket fd, as time increases system_server's
    fd will be more than 1024 and crash, so we needed add count limit
    for sensor listener to improve the system stability.

    Test: use the apk attached in the issue
          https://code.google.com/p/android/issues/detail?id=258634
    Bug: 37543280

    Change-Id: I35006966a1638c25bb0f54611e117e16a764e12b
    Signed-off-by: songjinshi <songjinshi@xiaomi.com>