commit 4925755ffa90aec75c116c6204688314121f71d9
Author: jwilson <jwilson@squareup.com>
Date:   Sun Apr 17 20:15:07 2016 -1000

    Optimize reading one of several expected values with Selection

    This isn't yet public API.

    This relies on an unreleased Okio API.

    This has a significant impact on performance. I measured parsing performance
    improve from 89k ops/sec to 140k ops/sec on one benchmark.