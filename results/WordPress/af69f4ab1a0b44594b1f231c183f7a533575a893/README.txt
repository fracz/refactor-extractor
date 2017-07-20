commit af69f4ab1a0b44594b1f231c183f7a533575a893
Author: Gary Pendergast <gary@pento.net>
Date:   Mon Oct 10 06:38:31 2016 +0000

    General: Restore usage of `$wpdb`, instead of `$this->db`.

    Hiding the `$wpdb` global behind a property decreases the readability of the code, as well as causing irrelevant output when dumping an object.

    Reverts [38275], [38278], [38279], [38280], [38387].
    See #37699.


    Built from https://develop.svn.wordpress.org/trunk@38768


    git-svn-id: http://core.svn.wordpress.org/trunk@38711 1a063a9b-81f0-0310-95a4-ce76da25c4cd