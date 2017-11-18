commit c2d55acab52941cb883c891e6973b0aa4457ec5b
Author: Oliver Woodman <olly@google.com>
Date:   Wed Dec 3 18:10:30 2014 +0000

    Get Exo+HLS memory usage more under control.

    - Split sample pools for video/audio/misc, since the typical
      required sample sizes are very different (and so it becomes
      inefficient to use a sample sized for video to hold audio).
    - Add TODO for further improvements.

    Issue: #174