commit ee305ae144d074692f2737eed8e38ec32f20e98e
Author: Dries Buytaert <dries@buytaert.net>
Date:   Sun Jan 11 15:05:21 2004 +0000

    Round 3 of aggregator improvements:

     - Added support for new tags:
       + Optinal feed image: <image> tag.
       + Dublin core dates: <dc:date> <dcterms:created>, <dcterms:issued>,
         <dcterms:modified>.
     - Usability improvements:
       + On the administration page, made the feed/bundle titles link
         to the feeds/bundles' pages.  On the feed/bundle's page, made
         the 'Last updated' field link to the administration page.
       + Moved the 'syndication' menu one level down.
     - Updated some content sensitive help.
     - Further improved themeability.
     - Fixed some invalid HTML.