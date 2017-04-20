commit 011ecde5ee031170fd90ef0f63c8af71190b5127
Author: prolic <saschaprolic@googlemail.com>
Date:   Fri Dec 7 21:21:14 2012 +0100

    refactored send response workflow

    - added SendResponseEvent
    - ResponseSender listen for an event
    - first response sender that can send the response, stops propagation
    - send response listener just triggers event, sends no response
    - SendResponseEvent tracks which response headers and content were sent
    - send response listener attaches default listeners (phpenvironmentresponse and consoleresponse, stream response sender is in progress)
    - removed AbstractResponseSender