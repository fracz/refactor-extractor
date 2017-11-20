commit df34107d7b796c613126cf5891dabca13d9ba8c1
Author: Xiao Li <swing1979@gmail.com>
Date:   Wed Jan 6 16:54:24 2016 -0800

    #1098 [performance] Agent improvements:
    websocket server part 3:
    1. Change message format sent between agent and server, use Java serialization instead of json, because there is no simple way to dump BuildWork object into json, push json format message out of scope.
    2. When agent is not registered, server responses reregister to agent