commit 2a8746362bc817fa4a2a26134eac827f49c72cca
Author: Lari Hotari <lari.hotari@gradle.com>
Date:   Wed Nov 2 10:11:17 2016 +0200

    Remove DurationMeasurement.fail and improve perf test error handling

    - use the interface just for starting and stopping the measurement
    - improve error handling in performance tests