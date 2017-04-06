commit 60743fc52aea9eabee58258a31f4ba465013cb4e
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Sun Feb 19 12:59:10 2012 -0800

    feat(ng:include) Fire $contentLoaded event

    + refactor unload to listen on this event -> we can use unload with ng:view as well

    Closes #743