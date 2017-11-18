commit 47cf5af868ca3bca7031142a378c33eec49f4ba7
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Tue Jan 27 17:31:08 2015 +0100

    improve stream engine fingerprinting of streams, stream rules and outputs

    uses hashcode and sha1 to detect changes to rebuild stream engine router on updates

    fixes #922