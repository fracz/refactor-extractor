commit b20d698ebd630a5d8030c42d3b88f6ef54224e36
Author: Lukas Reschke <lukas@owncloud.com>
Date:   Thu Nov 27 00:01:55 2014 +0100

    Cache results of available languages

    This function is about 8 times calles for every single page call, when caching this variable I was able to gain a small performance improvement from 20,512 µs to 630 µs profiled with xhprof

    Surely, this is no gigantic gain but if we would do that for every function out there...