commit 1378aba7aeeb7f6dd6cc2503968ba7b0e58d9333
Author: Ramin Zaghi <ramin.zaghi@arm.com>
Date:   Fri Feb 28 15:03:19 2014 +0000

    Re-implement native library search and copies.

    We now use a two step approach :

    - First we look through the list of shared libraries in an
      APK, and choose an ABI based on the (priority)  list of ABIs
      a given device supports.
    - Then we look through the list of shared libraries and copy
      all shared libraries that match the ABI we've selected.

    This fixes a long-standing bug where we would sometimes copy
    a mixture of different ABIs to the device, and also allows us
    to clearly pick an ABI to run an app with.

    The code in NativeLibraryHelper has been refactored so that all
    file name validation & matching logic is done in a single place
    (NativeLibrariesIterator). This allows us to avoid a lot of
    redundant logic and straightens out a few corner cases (for eg.
    where the abi determination & copying logic do not agree on
    what files to skip).

    bug: https://code.google.com/p/android/issues/detail?id=65053
    bug: 13647418

    Change-Id: I34d08353f24115b0f6b800a7eda3ac427fa25fef
    Co-Authored-By: Zhenghua Wang <zhenghua.wang0923@gmail.com>
    Co-Authored-By: Ramin Zaghi <ramin.zaghi@arm.com>
    Co-Authored-By: Narayan Kamath <narayan@google.com>