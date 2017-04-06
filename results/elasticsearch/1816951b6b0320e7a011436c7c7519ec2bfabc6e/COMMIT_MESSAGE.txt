commit 1816951b6b0320e7a011436c7c7519ec2bfabc6e
Author: Alexander Reelsen <alexander@reelsen.net>
Date:   Fri Jul 18 15:38:57 2014 +0200

    Netty: Refactoring to make MessageChannelHandler extensible

    Small refactorings to make the MessageChannelHandler more extensible.
    Also allowed access to the different netty pipelines

    This is the fix after the first version had problems with the HTTP
    transport due to wrong reusing channel handlers, which is the reason
    why tests failed.

    Relates #6889
    Closes #6915