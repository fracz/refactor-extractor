commit 8c7e251f418562a62eb74bbfbb8f499bce0952a5
Author: nmittler <nathanmittler@google.com>
Date:   Mon Nov 2 12:14:39 2015 -0800

    Allow Netty server override for ProtocolNegotiator

    Server implementation had to be refactored to use a ProtocolNegotiator to make this work.