commit 06364cf6f00feb5ed7494c90b5ac72bdf0703949
Author: Jason Tedor <jason@tedor.me>
Date:   Wed May 3 18:49:08 2017 -0400

    You had one job Netty logging guard

    In pre-release versions of Elasticsearch 5.0.0, users were subject to
    log messages of the form "your platform does not.*reliably.*potential
    system instability". This is because we disable Netty from being unsafe,
    and Netty throws up this scary info-level message when unsafe is
    unavailable, even if it was unavailable because the user requested that
    it be unavailabe. Users were rightly confused, and concerned. So, we
    contributed a guard to Netty to prevent this log message from showing up
    when unsafe was explicitly disabled. This guard shipped with all
    versions of Netty that shipped starting with Elasticsearch
    5.0.0. Unfortunately, this guard was lost in an unrelated refactoring
    and now with the 4.1.10.Final upgrade, users will again see this
    message. This commit is a hack around this until we can get a fix
    upstream again.

    Relates #24469