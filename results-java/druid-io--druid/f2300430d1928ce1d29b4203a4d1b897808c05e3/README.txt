commit f2300430d1928ce1d29b4203a4d1b897808c05e3
Author: Charles Allen <charles@allen-net.com>
Date:   Wed Apr 22 12:51:32 2015 -0700

    Cleanup some code in index creation.
    * Add some unit tests
    * Add io.druid.segment.IndexMerger.reprocess for quick re-indexing of data
    * Add dim-value validation to validation checker (instead of ONLY index #)
    * General code refactoring to make things a little easier to read