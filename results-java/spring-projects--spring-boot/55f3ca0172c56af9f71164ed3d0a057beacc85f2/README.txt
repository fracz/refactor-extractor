commit 55f3ca0172c56af9f71164ed3d0a057beacc85f2
Author: Dave Syer <dsyer@gopivotal.com>
Date:   Tue May 27 12:09:08 2014 +0100

    Remove misplaced re-ordering of default property source

    The early re-ordering (and in particular the temporary remove of the
    default properties) seemed to be a relic of an older approach that is
    no longer there since we refactored to support more sane profile ordering.
    Removing it doesn't seem to break anything and it allows you to specify
    the config file locations in SpringApplicationBuilder.properties().

    Fixes gh-953, fixes gh-920