commit 4687a03f5498f119430e5e9b5dfe23f409ae004b
Author: Philip Jackson <p-jackson@live.com>
Date:   Mon Mar 28 00:29:46 2016 +1300

    Don't wrap drag events in IE/Edge in dev builds

    Dev builds wrap synthetic events inside other events for a better debug
    experience. However IE/Edge won't allow access to the
    DataTransfer.dropEffect property if it's wrapped in this way.
    The first time a drag event is fired, test if we're in an environment
    that behaves this way and disable React's improved development
    experience if we are.