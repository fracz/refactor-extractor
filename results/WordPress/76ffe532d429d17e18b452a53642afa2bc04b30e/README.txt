commit 76ffe532d429d17e18b452a53642afa2bc04b30e
Author: scribu <scribu@1a063a9b-81f0-0310-95a4-ce76da25c4cd>
Date:   Thu Oct 14 07:35:13 2010 +0000

    get_edit_term_link() improvements:
            * add optional $object_type parameter
            * make $term_id and $taxonomy parameters mandatory
            * pass all relevant information to the filter
            * use in WP_Terms_Table
    See #9702


    git-svn-id: http://svn.automattic.com/wordpress/trunk@15800 1a063a9b-81f0-0310-95a4-ce76da25c4cd