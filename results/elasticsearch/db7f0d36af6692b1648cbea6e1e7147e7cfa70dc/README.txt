commit db7f0d36af6692b1648cbea6e1e7147e7cfa70dc
Author: Alexander Reelsen <alexander@reelsen.net>
Date:   Thu Jul 17 08:28:55 2014 +0200

    Netty: Refactoring to make MessageChannelHandler extensible

    Small refactorings to make the MessageChannelHandler more extensible.
    Also allowed access to the different netty pipelines

    Closes #6889