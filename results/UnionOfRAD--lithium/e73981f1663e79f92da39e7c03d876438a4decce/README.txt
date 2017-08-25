commit e73981f1663e79f92da39e7c03d876438a4decce
Author: gwoo <gwoohoo@gmail.com>
Date:   Tue Jan 12 21:52:27 2010 -0500

    refactoring inflector. removes the need for __init().
    removes `variable`, use camelize(word, false).
    changing default replacement for slug to -