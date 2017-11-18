commit abe4420006d244140d9d4efd0c31756b13951e76
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Wed Nov 1 16:22:49 2017 -0400

    Improve ReactorNettyTcpClient shutdown logic

    This commit takes care of the TODOs in ReactorNettyTcpClient by taking
    advantage of improvements in Reactor Netty.

    Issue: SPR-16145