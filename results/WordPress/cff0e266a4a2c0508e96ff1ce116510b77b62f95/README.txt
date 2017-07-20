commit cff0e266a4a2c0508e96ff1ce116510b77b62f95
Author: westi <westi@1a063a9b-81f0-0310-95a4-ce76da25c4cd>
Date:   Sat Jan 28 11:56:50 2012 +0000

    Refactor WPDB::get_caller() into wp_debug_backtrace_summary() and improve the functionality to provide enhanced context and a standardised default pretty format. Fixes #19589


    git-svn-id: http://svn.automattic.com/wordpress/trunk@19773 1a063a9b-81f0-0310-95a4-ce76da25c4cd