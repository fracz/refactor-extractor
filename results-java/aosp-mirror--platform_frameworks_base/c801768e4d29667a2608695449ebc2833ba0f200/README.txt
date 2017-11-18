commit c801768e4d29667a2608695449ebc2833ba0f200
Author: Dianne Hackborn <hackbod@google.com>
Date:   Tue Jul 6 13:34:38 2010 -0700

    Integrate Loader support in to Activity/Fragment.

    Introduces a new LoaderManager class that takes care of
    most of what LoaderManagingFragment does.  Every Fragment
    and Activity can have one instance of this class.  In the
    future, the instance will be retained across config changes.

    Also various other cleanups and improvement.

    Change-Id: I3dfb406dca46bda7f5acb3c722efcbfb8d0aa9ba