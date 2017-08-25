commit 0164592b8c2ef73974303c4ec4bf254f05c270ef
Author: David Mudrak <david@moodle.com>
Date:   Thu May 5 02:21:23 2011 +0200

    MDL-27376 MDL-27377 Backup converters API refactored

    * Several base_converter methods made protected when there was no obvious
    reason why they should be public (subject of eventual change still).
    * The conversion chain now constructed in advance before any converter
    class is instantiated, using Dijkstra's algorithm.