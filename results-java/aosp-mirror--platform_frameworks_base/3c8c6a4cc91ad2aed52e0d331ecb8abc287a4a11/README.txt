commit 3c8c6a4cc91ad2aed52e0d331ecb8abc287a4a11
Author: Peng Xu <pengxu@google.com>
Date:   Thu Apr 6 18:37:56 2017 -0700

    Address review suggestions for sensor direct report related APIs

    Changes inline with bug number

    Some improvements about SensorDirectChannel class.
      * Complete the doc about creation of SensorDirectChannel object.
      * Make SensorDirectChannel implements java.nio.channels.Channel.
      * Change isValid() to isOpen().
      * Make close() thread safe.
    Bug: 36550285

      * Throw exception on failure of SensorManager.createDirectChannel.
      * Change to use NullPointerException when unexpected null pointer
        is passed in.
    Bug: 36555061

      * Move SensorManager.configureDirectChannel() to
        SensorDirectChannel.configure().
      * Format SensorDirectChannel.configure() function doc with
        <pre></pre> to maintain the table structure.
      * Reworded Sensor.isDirectChannelTypeSupported java doc.
    Bug: 36555604

    Test: pass updated cts SensorDirectReportTest
    Change-Id: I447121eaf414cbc94292a109a9d93d2e3c89f8f4