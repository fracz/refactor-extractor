commit ee1ba7f2f343dd99921162df1c3184e7423102d6
Author: nacin <nacin@1a063a9b-81f0-0310-95a4-ce76da25c4cd>
Date:   Mon Oct 25 20:43:52 2010 +0000

    Importer and exporter overhaul, mega props duck.

    Exporter overhaul:
     * Add author information to export
     * Greater usage of slug identifiers
     * Don't export auto-drafts, spam comments, or edit lock/last meta keys
     * Inline documentation improvements
     * Remove filtering for now (@todo)
     * Bump WXR version to 1.1, but remain back compat in the importer

    Importer overhaul (http://plugins.trac.wordpress.org/changeset/304249):
     * Use an XML parser where available (SimpleXML, XML Parser)
     * Proper import support for navigation menus
     * Many bug fixes, specifically improvements to category and custom taxonomy handling
     * Better author/user mapping

    Fixes #5447 #5460 #7400 #7973 #8471 #9237 #10319 #11118 #11144 #11354 #11574 #12685 #13364 #13394 #13453 #13454 #13627 #14306 #14442 #14524 #14750 #15055 #15091 #15108.

    See #15197.


    git-svn-id: http://svn.automattic.com/wordpress/trunk@15961 1a063a9b-81f0-0310-95a4-ce76da25c4cd