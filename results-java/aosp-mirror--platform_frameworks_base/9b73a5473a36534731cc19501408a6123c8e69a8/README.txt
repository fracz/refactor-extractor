commit 9b73a5473a36534731cc19501408a6123c8e69a8
Author: Keun-young Park <keunyoung@google.com>
Date:   Wed Feb 1 12:09:58 2017 -0800

    wait for sensor service before starting WMS

    - Starting sensor service in separate thread led into occasional 1 sec
      blocking of WMS inside PhoneWindowManager to get sensor service.
    - Change startSensorService into blocking call and call it from
      separate thread using SystemServerInitThreadPool.
    - This does not improve best case boot-up time but fixes occasional 1 sec delay
      which is happening in 10 to 20% rate. So this is potential 100 to 200ms saving for many runs.

    bug: 34846045
    Test: multiple reboots.
    Change-Id: Ia08fa3284aed5e576acaac6fbfd74b9db9f7d63c