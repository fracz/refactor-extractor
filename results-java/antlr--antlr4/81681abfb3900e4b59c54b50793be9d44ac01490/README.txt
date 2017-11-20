commit 81681abfb3900e4b59c54b50793be9d44ac01490
Author: parrt <parrt@cs.usfca.edu>
Date:   Sat Feb 18 11:21:17 2017 -0800

    augment TerminalNode with setParent(). Technically, this is not backward compatible as it changes the interface but no one was able to create custom TerminalNodes anyway so I'm adding as it improves internal code quality. addChild now sets the parent rather than create. MUCH better.