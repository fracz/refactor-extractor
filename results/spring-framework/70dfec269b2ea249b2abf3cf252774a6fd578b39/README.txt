commit 70dfec269b2ea249b2abf3cf252774a6fd578b39
Author: Rossen Stoyanchev <rstoyanchev@gopivotal.com>
Date:   Mon Oct 14 21:55:13 2013 -0400

    Use alternative UUID strategy in MessageHeaders

    This change adds an alternative UUID generation strategy to use by
    default in MessageHeaders. Instead of using SecureRandom for each
    new UUID, SecureRandom is used only for the initial seed to be
    provided java.util.Random. Thereafter the same Random instance is
    used instead. This provides improved performance while id's are
    still random but less securely so.