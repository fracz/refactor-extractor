commit fb2ac1e46eb8e21413deb64d822b19c6bbd275e1
Author: Xiao Li <swing1979@gmail.com>
Date:   Fri Jan 8 12:09:33 2016 -0800

    #1098 [performance] Agent improvements: 1. mark agent lost contact when websocket connection with the agent is closed. 2. send async message to agent to avoid server side blocking IO, overall design should be robust enough for doing this (ping to update agent status, mark lost contact when lost connection, reschudule hanging jobs, e.g.)