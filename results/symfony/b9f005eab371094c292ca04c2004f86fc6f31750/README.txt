commit b9f005eab371094c292ca04c2004f86fc6f31750
Merge: c4a3371 c40a4e5
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Jul 4 07:27:44 2012 +0200

    merged branch Tobion/query (PR #4735)

    Commits
    -------

    c40a4e5 [HttpFoundation] fix query string normalization
    f9ec2ea refactored test method
    0880174 [HttpFoundation] added failing tests for query string normalization

    Discussion
    ----------

    [HttpFoundation] fix query string normalization

    This fixes the query string normalization. There were several problems in it (see test cases that I added).
    The main issue, that first catched my eye, was that the query string was urldecoded before it was exploded by `=`. See old code: `explode('=', rawurldecode($segment), 2);`. This means an encoded `=` (`%3D`) would falsely be considered a separator and thus lead to complete different parameters. The fixed test case is at `pa%3Dram=foo%26bar%3Dbaz&test=test`.

    ---------------------------------------------------------------------------

    by Tobion at 2012-07-04T02:21:25Z

    cc @simensen considering your PR 4711