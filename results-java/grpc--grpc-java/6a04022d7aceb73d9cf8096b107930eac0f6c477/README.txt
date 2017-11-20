commit 6a04022d7aceb73d9cf8096b107930eac0f6c477
Author: Kun Zhang <zhangkun83@users.noreply.github.com>
Date:   Tue Nov 22 22:32:27 2016 -0800

    core: InternalSubchannel: the new TransportSet. (#2427)

    This is the first step of a major refactor for the LoadBalancer-related part of Channel impl (#1600). It forks TransportSet into InternalSubchannel and makes changes on it.

    What's changed:

    - InternalSubchannel no longer has delayed transport, thus will not buffer
      streams when a READY real transport is absent.
    - When there isn't a READ real transport, obtainActiveTransport() will
      return null.
    - InternalSubchannel is no longer a ManagedChannel
    - Overhauled Callback interface, with onStateChange() replacing the
      adhoc transport event methods.
    - Call out channelExecutor, which is a serializing executor that runs
      the Callback.

    The first commit is an unmodified copy of the files that are being forked. Please review the second commit which changes on the forked files.