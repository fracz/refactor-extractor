commit b080660dfd69ea6f0a034946b2ff8d94e97a2537
Author: Jeff Brown <jeffbrown@google.com>
Date:   Wed May 16 12:50:41 2012 -0700

    Fix a possible starvation issue related to vsync.

    This makes a noticeable improvement in cases where applications
    post messages that need to be processed between animation frames.

    Bug: 6418353
    Change-Id: If225742e37aeaf3f0ca9710f9bf43dbb03bcde12