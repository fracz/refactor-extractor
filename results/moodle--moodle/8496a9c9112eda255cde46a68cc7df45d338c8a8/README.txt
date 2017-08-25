commit 8496a9c9112eda255cde46a68cc7df45d338c8a8
Author: skodak <skodak>
Date:   Sat Aug 25 12:22:15 2007 +0000

    MDL-10995 improved capability_search():
    * eliminated fetching of course - $COURSE used if id matches
    * parent category structure is cached in static array