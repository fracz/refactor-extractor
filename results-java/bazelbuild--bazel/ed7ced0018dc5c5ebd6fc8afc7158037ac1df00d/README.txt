commit ed7ced0018dc5c5ebd6fc8afc7158037ac1df00d
Author: Justine Tunney <jart@google.com>
Date:   Tue Nov 29 18:29:37 2016 +0000

    Support multiple mirror URLs for external repos

    This change improves upon 4c67807964e37cfd55bbcda4c6374fcc480bcecc.

    - A urls attribute has been added to the native workspace rules, with
      the exception of maven_jar and git_repository. The Skylark repository
      API also supports multiple URLs now.

    - The earlier mirrors in the list are preferred. Failover will happen
      automatically in parallel.

    - The first 32kB of data is checked before choosing a mirror in order
      to evade captive portals.

    - If one's Internet goes down or a download times out, then the
      download will resume automatically where it left off, provided the
      server supports RFC7233 for that particular file. Please note that
      GitHub does not support this for archive snapshots. Files should
      always be mirrored to a CDN, e.g. GCS, because they support this.

    - A semaphore is now used on downloads so only 8 can happen at once.

    Fixes #1814
    Fixes #2131
    Fixes #2008
    Fixes #1968
    Fixes #1717
    Fixes #943
    Wont fix #1194
    Fixes tensorflow/tensorflow#5933
    Fixes tensorflow/tensorflow#5924
    Fixes tensorflow/tensorflow#5924
    Fixes tensorflow/tensorflow#5432
    See #1607
    See #821
    See tensorflow/tensorflow#5080
    See tensorflow/tensorflow#5029
    See tensorflow/tensorflow#4583
    See tensorflow/tensorflow#4058

    RELNOTES: A urls attribute has been added to repository rules to support multiple mirror URLs for reliably downloading files.

    --
    MOS_MIGRATED_REVID=140495736