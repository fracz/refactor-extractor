commit 9392c0ca7091dc248dec578b5b7ba601fbda3256
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue Nov 6 16:52:06 2012 +0100

    GRADLE-2495 Rationalized the exception handling in the TcpOutgoingConnector.

    I've looked into this code a while ago when investigating GRADLE-2495 (NetBeans issue). I don't have full confidence it solves the problem (although from the stack trace it looks like it) because this is really hard to reproduce. Nevertheless, the proposed refactoring feels like a right thing anyway as it simplifies the exception handling a lot. Added a little bit of unit test coverage. I don't see a way to integ test this, we've already have integ test coverage for this scenario (deamon is killed, rogue address stays in the registry).

    Thanks a lot Attila for the debugging effort and the solution insight!