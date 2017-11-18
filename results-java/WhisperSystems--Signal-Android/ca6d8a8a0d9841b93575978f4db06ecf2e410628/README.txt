commit ca6d8a8a0d9841b93575978f4db06ecf2e410628
Author: Jake McGinty <me@jake.su>
Date:   Mon Mar 17 23:25:09 2014 -0700

    refactor and improve contact selection

    * unify single and multi contact selection activities
    * follow android listview design recommendations more closely
    * add contact photos to selection
    * change indicator for push to be more obvious
    * cache circle-cropped bitmaps
    * dedupe numbers when contact has multiple of same phone number

    // FREEBIE