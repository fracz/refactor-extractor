commit 85ac8eff90ff1f4356ad4f0f3570e681781220a8
Author: Roman Leventov <leventov@users.noreply.github.com>
Date:   Thu Oct 13 18:28:46 2016 +0300

    Improve performance of IndexMergerV9  (#3440)

    * Improve performance of StringDimensionMergerV9 and StringDimensionMergerLegacy by avoiding primitive int boxing by using IntIterator in IndexedInts instead of Iterator<Integer>; Extract some common logic for V9 and Legacy mergers; Minor improvements to resource handling in StringDimensionMergerV9

    * Don't mask index in MergeIntIterator.makeQueueElement()

    * DRY conversion RoaringBitmap's IntIterator to fastutil's IntIterator

    * Do implement skip(n) in IntIterators extending AbstractIntIterator because original implementation is not reliable

    * Use Test(expected=Exception.class) instead of try { } catch (Exception e) { /* ignore */ }