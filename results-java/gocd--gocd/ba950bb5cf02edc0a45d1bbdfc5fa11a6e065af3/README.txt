commit ba950bb5cf02edc0a45d1bbdfc5fa11a6e065af3
Author: Xiao Li <swing1979@gmail.com>
Date:   Wed Jan 6 17:02:49 2016 -0800

    #1098 [performance] Agent improvements:
    websocket agent part 1:
    1. Add a system environment toggle for enabling websocket.
    2. Add a toggle to disable auto register local agent for testing purpose.
    3. New websocket ping with upgrade check and agent ssl registration. Notice we're doing websocket ping in original loop method, because it is configured as fixRate=false, original ping loop has fixRate=true, fixRate=true may cause client side sending out too much calls when there is one call is blocked for any reason, and we don't need very frequent ping to keep agent status updated on server side. And we will need time to test out what's right interval for websocket client to ping server (Server marks agent that does not ping in 30 sec as lost contact).
    4. Process set cookie, cancel job, reregister agent and build work messages