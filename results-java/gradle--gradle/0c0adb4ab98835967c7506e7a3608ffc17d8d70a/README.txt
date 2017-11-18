commit 0c0adb4ab98835967c7506e7a3608ffc17d8d70a
Author: Daz DeBoer <darrell.deboer@gradleware.com>
Date:   Wed Mar 4 12:08:37 2015 +1100

    Minor improvements to JavaFX fix for Groovy

    - Use ClassLoader.systemClassLoader.parent to find the ext class loader (instead of looking for javafx specifically)
    - Added test for compiling java referencing JavaFX on JDK8
    - Removed test that adds specific dependency on JavaFX jar in JDK<8