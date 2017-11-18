commit 10f70fe96b7b93074f8366718c8330a78fc21923
Author: NathanSweet <nathan.sweet@gmail.com>
Date:   Sun Aug 4 21:19:58 2013 +0200

    Improved native library extraction and loading.

    I've found a small percentage of users can't write to the temp directory. Eg:
    https://code.google.com/p/libgdx/issues/detail?id=1504
    These changes improve the chances of being able to extract and load a library. For load(), multiple locations are tried to extract and load the library. For extractFile(), multiple locations are tried to extract the file.