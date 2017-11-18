commit bc8fe2fb3b300834040e46dd52ee77067fbac534
Author: Fedor Korotkov <fedor.korotkov@airbnb.com>
Date:   Thu Oct 13 09:50:59 2016 -0700

    Zinc compiler enchasments

    Use direct java compiler and create separate caches for different Zinc versions. Also refactored creating a new compiler so it acquires a lock only if needed and only for writing the final compiler interface to the cache.

    Before a lock was acquired even for checking file existence in the cache and more importantly *compiling* the interface which affects performance of highly parallel builds.