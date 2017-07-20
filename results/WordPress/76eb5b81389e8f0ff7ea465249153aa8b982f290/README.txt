commit 76eb5b81389e8f0ff7ea465249153aa8b982f290
Author: westi <westi@1a063a9b-81f0-0310-95a4-ce76da25c4cd>
Date:   Wed Aug 24 10:39:52 2011 +0000

    Optimise get_term to not query for term_id = 0. Also improve the prepared query to use %d for the term_id.
    Fixes #18076 props mdawaffe.

    git-svn-id: http://svn.automattic.com/wordpress/trunk@18591 1a063a9b-81f0-0310-95a4-ce76da25c4cd