commit 869dd39c5a9c1e55a1d7ab796a05e5e710dff348
Author: Andrei Stingaceanu <stg@google.com>
Date:   Fri May 5 15:43:52 2017 +0100

    Fix broken Backspace/ForwardDelete tests

    * correctly use @Before to intstantiate
    * get rid of useless KeyListenerTestCase class
    * move to KeyUtils, refactor and document util method to generate
      a KeyEvent

    Bug: 37991689
    Test: adb shell am instrument -w -e package android.text.method
    com.android.frameworks.coretests/android.support.test.runner.AndroidJUnitRunner

    Change-Id: Ibbce351f31eb62492cd1c7c920fdef44df89b683