commit 1661b51272e5cfd7f820022222128d9ab05a72a3
Author: ZHANG Dapeng <zdapeng@google.com>
Date:   Fri Jul 14 11:05:09 2017 -0700

    core: refactor SubchannelImpl

    * refactor SubchannelImpl -> AbstractSubchannel

    The old `SubchannelImpl` is actually not implementing anything, and the old `SubchannelImplImpl` has a weird name.