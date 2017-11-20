commit e7bd929a5a166730a035db3a9927436eef0b7030
Author: Xiao Li <swing1979@gmail.com>
Date:   Wed Jan 13 11:28:02 2016 -0800

    #1098 [performance] Agent improvements:
    1. notify agent to cancel job when running job is cancelled or rescheduled
    2. change to slf4j logger
    3. clear info log when agent connects with websocket or closes connection