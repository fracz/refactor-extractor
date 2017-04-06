commit 76ab02e002758d71503eaf55269f80dbc8ec9b04
Merge: 07d1a72 c2eddaf
Author: Jason Tedor <jason@tedor.me>
Date:   Wed Aug 31 16:16:30 2016 -0400

    Merge branch 'master' into log4j2

    * master:
      Avoid NPE in LoggingListener
      Randomly use Netty 3 plugin in some tests
      Skip smoke test client on JDK 9
      Revert "Don't allow XContentBuilder#writeValue(TimeValue)"
      [docs] Remove coming in 2.0.0
      Don't allow XContentBuilder#writeValue(TimeValue)
      [doc] Remove leftover from CONSOLE conversion
      Parameter improvements to Cluster Health API wait for shards (#20223)
      Add 2.4.0 to packaging tests list
      Docs: clarify scale is applied at origin+offest (#20242)