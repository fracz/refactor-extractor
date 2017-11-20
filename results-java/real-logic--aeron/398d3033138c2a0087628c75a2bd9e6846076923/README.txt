commit 398d3033138c2a0087628c75a2bd9e6846076923
Author: Todd L. Montgomery <todd.montgomery@kaazing.com>
Date:   Tue Aug 19 13:42:46 2014 -0700

    [Java]: made logging exceptions use printStackTrace if logging not enabled. Made logging exceptions use while loop to make sure they are not dropped. Logging of frames in refactored and added logging of frames in dropped for debugging with loss generators.