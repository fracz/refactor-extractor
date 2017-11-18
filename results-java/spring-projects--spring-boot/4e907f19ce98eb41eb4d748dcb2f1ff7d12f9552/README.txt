commit 4e907f19ce98eb41eb4d748dcb2f1ff7d12f9552
Author: Dave Syer <dsyer@pivotal.io>
Date:   Tue Jan 20 17:23:25 2015 +0000

    Carefully add nested archives from JAR in PropertiesLauncher

    If user runs an executable archive then it and its lib directory will be
    on the classpath. Entries from loader.path take precedence in a way that
    should make sense to users (earlier wins like in CLASSPATH env var).

    Also added new integration tests to verify the behaviour (big improvement
    on the old ones, which probably aought to be beefed up to the same
    standard).

    Fixes gh-2314