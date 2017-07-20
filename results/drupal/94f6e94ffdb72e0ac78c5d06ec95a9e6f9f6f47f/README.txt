commit 94f6e94ffdb72e0ac78c5d06ec95a9e6f9f6f47f
Author: Dries Buytaert <dries@buytaert.net>
Date:   Wed Jan 7 19:52:10 2004 +0000

    - Many excellent news aggregator improvements by Kjartan:
        + Added drupal_http_request().
        + Replaced rssfeeds with OPML feed subscription list.
        + Added support for pubDate.
        + Added support for conditional gets using ETag and Last-Modified.