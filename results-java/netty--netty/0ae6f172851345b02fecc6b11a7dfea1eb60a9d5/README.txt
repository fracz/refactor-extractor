commit 0ae6f172851345b02fecc6b11a7dfea1eb60a9d5
Author: Xiaoyan Lin <linxiaoyan18@gmail.com>
Date:   Wed Dec 30 21:44:42 2015 -0800

    Fix unnecessary boxing and incorrect Serializable

    Motivation:

    - AbstractHttp2ConnectionHandlerBuilder.encoderEnforceMaxConcurrentStreams can be the primitive boolean
    - SpdySession.StreamComparator should not be Serializable since SpdySession is not Serializable

    Modifications:

    Use boolean instead and remove Serializable

    Result:

    - Minor improvement for AbstractHttp2ConnectionHandlerBuilder
    - StreamComparator is not Serializable any more