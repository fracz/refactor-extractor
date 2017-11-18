commit b984ca7979670a86bb4acc3b83eed0b1125824a6
Author: Norman Maurer <norman_maurer@apple.com>
Date:   Thu Jan 29 06:49:14 2015 +0100

    Allow to change epoll mode

    Motivation:
    Netty uses edge-triggered epoll by default for performance reasons. The downside here is that a messagesPerRead limit can not be enforced correctly, as we need to consume everything from the channel when notified.

    Modification:
    - Allow to switch epoll modes before channel is registered
    - Some refactoring to share more code

    Result:
    It's now possible to switch epoll mode.