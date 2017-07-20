commit 5468377d3e092c4b12d2bacd338db68ed0b7fca2
Author: Andrew Nacin <wp@andrewnacin.com>
Date:   Tue Mar 4 07:11:13 2014 +0000

    Bail early from shortcode functions if no delimiter is present.

    This is a significant performance improvement for processing content without shortcodes, and only the slightest hit when content contains shortcodes (which must then undergo processing anyway). Performance results on the ticket.

    props TobiasBg.
    fixes #23855.

    Built from https://develop.svn.wordpress.org/trunk@27394


    git-svn-id: http://core.svn.wordpress.org/trunk@27242 1a063a9b-81f0-0310-95a4-ce76da25c4cd