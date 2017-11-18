commit e3ad7e52a69c6e8c5b06905f63839bd39d0e89a8
Author: Josh Guilfoyle <devjasta@fb.com>
Date:   Fri Jan 30 14:04:29 2015 -0800

    Add stetho-urlconnection helper

    This adds a helper module, stetho-urlconnection, to reduce the
    boilerplate code needed to hook HttpURLConnection.  Ultimately this is
    about simplified integration but it has the nice side effect of
    centralizing usages of NetworkEventReporter so that we could more easily
    revision this API if we needed to.

    Some semantic changes were applied to the sample as a result of this
    refactor (in particular, requestWillBeSent is now delivered after
    URL#openConnection) though these were done explicitly to simplify
    integration and should not have any functional impact.  See
    http://www.tbray.org/ongoing/When/201x/2012/01/17/HttpURLConnection for
    context on exactly how messed up the HttpURLConnection API really is.

    Also fixed a bug with calling disconnect after the HttpURLConnection
    instance was disposed.

    A companion stetho-okhttp module should be added as well.