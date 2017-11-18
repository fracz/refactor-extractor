commit f81c0aca8e45bc9a629df4220030aea10f22d38c
Author: Adam Bliss <abliss@gmail.com>
Date:   Fri Mar 22 03:15:34 2013 +0000

    Reimplementation of Concat, improved handling of Observable<Observable<T>>.

    The old version required all of the Observable<T>s to be generated and buffered
    before the concat could begin.  If the outer Observable was asynchronous, items
    could be dropped (test added).  The new version passes the test, and does the
    best job I could (after examining several possible strategies) of achieving
    clear and consistent semantics in accordance with the principle of least
    surprise.