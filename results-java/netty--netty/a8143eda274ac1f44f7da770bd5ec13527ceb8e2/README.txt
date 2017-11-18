commit a8143eda274ac1f44f7da770bd5ec13527ceb8e2
Author: Trustin Lee <t@motd.kr>
Date:   Wed Jun 4 16:39:50 2014 +0900

    Overall refactoring of the STOMP codec

    - StompObject -> StompSubframe
    - StompFrame -> StompHeadersSubframe
    - StompContent -> StompContntSubframe
    - FullStompFrame -> StompFrame
    - StompEncoder/Decoder -> StompSubframeEncoder/Decoder
    - StompAggregator -> StompSubframeAggregator
    - Simplify the example
    - Update Javadoc
    - Miscellaneous cleanup