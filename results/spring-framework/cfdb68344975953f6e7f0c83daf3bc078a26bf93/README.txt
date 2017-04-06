commit cfdb68344975953f6e7f0c83daf3bc078a26bf93
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Mon Apr 4 18:17:11 2016 -0400

    Call resetRequest after writeFrame for polling sessions

    Previous refactoring (fcf6ae and also 43d937) in the SockJsSession
    hierarchy consolidated access to the request and response in the
    base class AbstractHttpSockJsSession in order to keep synchronization
    concerns there. However that also unintentionally removed the call to
    resetRequest() after sending a heartbeat for any of the
    PollingSockJsSession classes. In general a polling session should call
    resetRequest after every frame written.

    This commit brings back the writeFrame override in PollingSockJsSession
    with an extra call to resetRequest().

    Issue: SPR-14107