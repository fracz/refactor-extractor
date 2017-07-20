commit 96c9174ee21a866584de2ed4fb0dc243929bed55
Author: Renan Gonçalves <renan.saddam@gmail.com>
Date:   Fri Mar 29 15:24:08 2013 +0100

    Fixing tag generation for Html::script() and Html::css() when using 'fullBase' => true.

    Changing values on array_diff_key() from empty ('') to null as they have no consequences and offer better readability.