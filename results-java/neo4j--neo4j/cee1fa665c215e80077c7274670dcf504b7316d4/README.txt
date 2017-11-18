commit cee1fa665c215e80077c7274670dcf504b7316d4
Author: Martin Furmanski <martin.furmanski@neotechnology.com>
Date:   Mon Oct 27 11:57:10 2014 +0100

    [2.2] Add efficient unparking in batched forcing.

    This helps each thread reach its full potential, and in particular the
    single-threaded workload is markedly improved.