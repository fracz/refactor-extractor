commit d1dc387b758bb247f63f8ca078236b2b13b7d894
Author: Xiao Li <swing1979@gmail.com>
Date:   Thu Jan 7 13:09:08 2016 -0800

    #1098 [performance] Agent improvements:
    1. send report messages to server when building job.
    2. make JobRunner#handled and JobRunner#isJobCancelled thread-safe.
    3. setup websocket config by system environment variables
    4. do not check registered status when processing ping message on server side. do it when looking for job for agent.