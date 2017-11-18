commit 67a951b655b73d2222b8ddc6b93532688c487b77
Author: Marvin Ramin <marvin.ramin@gmail.com>
Date:   Wed Apr 19 20:46:23 2017 +0200

    Improve doOnDispose JavaDoc (#5296)

    * correct javadoc note in Observable.doOnDispose()

    The note was claiming Observable.doOnDispose() would be called for
    terminal events of the Observable, which is not the case

    * small improvement to .doOnDispose javadocs to refer to Actions

    * add @throws mentions to doOnDisposed javadocs