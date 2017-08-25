commit 8562c2993b142377c814d4a3dcdbdfe35bcd08ea
Author: pmeenan <pmeenan@webpagetest.org>
Date:   Fri Apr 22 19:21:04 2011 +0000

    Made some pretty significant improvements to the video comparison that now lets you specify if you want the comparison to end at Doc Complete, Fully loaded, the last visual change captured or AFT (or an explicit time if you manually tweak the URL).  It also now uploads all of the video captured from the agent so that you can show video past the test end point if needed.  Finally, you can specify all of these options on a per-test bases on the URL itself (per-test UI coming later in Q2)