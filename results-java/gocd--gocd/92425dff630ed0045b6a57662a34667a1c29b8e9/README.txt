commit 92425dff630ed0045b6a57662a34667a1c29b8e9
Author: Xiao Li <swing1979@gmail.com>
Date:   Fri Jan 29 09:59:24 2016 -0800

    Websocket communication (#1098) #1793: improve sending message limit, change to use partial send to make sure we can send messages that are larger than current websocket max message size. This is due to current message is a dump of model objects. Removed old reset websocket max message size code, we should not worry about that anymore