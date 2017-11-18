commit eee28cbafacc2dcfcd71a00b529c19c507e2877c
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Wed Sep 18 06:48:08 2013 -0400

    Read/write Groovy test graph from a file

    This commit uses Blueprints's GraphMLWriter and -Reader classes to
    dump/load a file containing relations.  The schema is not saved
    anywhere.  The schema is effectively hardcoded as the defaults in
    c.t.t.testutil.gen.Schema.  The Schema settings (and more) used to
    generate the v10k.graphml.gz file in this commit are described in a
    comment near the top of GroovySerialTest.

    This commit also refactors GroovySerialTest and GroovyTestSupport to
    reduce a bit more boilerplate code in GroovySerialTest's domain logic.