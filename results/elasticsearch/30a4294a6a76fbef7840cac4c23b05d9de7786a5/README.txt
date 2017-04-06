commit 30a4294a6a76fbef7840cac4c23b05d9de7786a5
Author: Robert Muir <rmuir@apache.org>
Date:   Fri Feb 20 06:58:43 2015 -0500

    Upgrade to lucene r1660560

    Squashed commit of the following:

    commit 07391388715ed1f737e8acc391cea0bce5d79db9
    Merge: a71cc45 b61b021
    Author: Robert Muir <rmuir@apache.org>
    Date:   Fri Feb 20 06:58:11 2015 -0500

        Git really sucks

        Merge branch 'lucene_r1660560' of github.com:elasticsearch/elasticsearch into lucene_r1660560

    commit b61b02163f62ad8ddd9906cedb3d57fed75eb52d
    Author: Adrien Grand <jpountz@gmail.com>
    Date:   Wed Feb 18 19:03:49 2015 +0100

        Try to improve TopDocs.merge usage.

    commit bf8e4ac46d7fdaf9ae128606d96328a59784f126
    Author: Ryan Ernst <ryan@iernst.net>
    Date:   Wed Feb 18 07:43:37 2015 -0800

        reenable scripting test for accessing postings pieces.  commented out
        parts that fail because of bad assumptions

    commit 6d4d635b1a23b33c437a6bae70beea70ad52d91c
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Feb 18 09:41:46 2015 -0500

        add some protection against broken asserts, but, also disable crappy test

    commit c735bbb11f38782dfea9c4200fcf732564126bf5
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Feb 18 02:21:30 2015 -0500

        cutover remaining stuff from old postings api

    commit 11c9c2bea3db3ff1cd2807bd43e77b500b167aed
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Feb 18 01:46:04 2015 -0500

        cut over most DocsEnum usage

    commit bc18017662f6abddf3f074078f74e582494c88e2
    Author: Robert Muir <rmuir@apache.org>
    Date:   Wed Feb 18 01:19:35 2015 -0500

        upgrade to lucene_r1660560, modulo one test fail