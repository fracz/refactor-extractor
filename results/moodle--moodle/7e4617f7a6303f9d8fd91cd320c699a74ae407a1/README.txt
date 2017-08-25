commit 7e4617f7a6303f9d8fd91cd320c699a74ae407a1
Author: Sam Hemelryk <sam@moodle.com>
Date:   Mon May 31 03:33:34 2010 +0000

    navigation-dock MDL-22560 Major changes to he way the dock works
    * Themes can now check if a region is completely docked
    * Dock now delegates events to improve performance
    * Dock now completely YUI3
    * No longer uses YUI overlay instead has custom control