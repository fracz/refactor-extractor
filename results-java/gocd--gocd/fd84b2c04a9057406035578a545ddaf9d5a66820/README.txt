commit fd84b2c04a9057406035578a545ddaf9d5a66820
Author: Xiao Li <swing1979@gmail.com>
Date:   Mon Jan 4 17:00:24 2016 -0800

    #1098 [performance] Agent improvements:
    websocket server part 1:
    1. Process ping action from agent
    2. Assign new cookie to agent when ping info has no cookie.
    3. Send cancel job message to agent if server side runtime status is canceled