commit 91db99208e197a73584a88a8d835eeb55c466335
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Fri Apr 27 13:14:46 2012 +0200

    refactor(scope.$emit): rename event.cancel() to event.stopPropagation()

    Breaks event.cancel() is event.stopPropagation()