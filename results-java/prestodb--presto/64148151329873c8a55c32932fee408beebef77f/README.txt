commit 64148151329873c8a55c32932fee408beebef77f
Author: Dain Sundstrom <dain@iq80.com>
Date:   Fri Nov 14 14:52:20 2014 -0800

    Fix state machine in MockExchangeRequestProcessor

    Always create mock buffers for requested resources to avoid null pointer exceptions
    Consolidate mock buffer code to improve readability