commit 1a4b5a4f02e7d8ff8ff645377d97e6062d36aeaa
Author: Dianne Hackborn <hackbod@google.com>
Date:   Mon Dec 8 17:43:31 2014 -0800

    Work on issue #18640385: Add procstats test mode

    You can now do "adb shell dumpsys procstats --start-testing" to
    enable high frequency pss sampling.

    Also improved the low on RAM mem reporting to separate out RAM
    from memtrack, in case the data we are getting from that is bad.

    And fixed meminfo --oom to work correctly again.

    Change-Id: I7af17eab110a82298bd7b0ce381f8fa5c96c1f6a