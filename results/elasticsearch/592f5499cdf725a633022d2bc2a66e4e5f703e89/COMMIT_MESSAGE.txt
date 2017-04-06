commit 592f5499cdf725a633022d2bc2a66e4e5f703e89
Author: Ali Beyad <ali@elastic.co>
Date:   Tue Jan 19 00:09:22 2016 -0500

    More robust handling of CORS HTTP Access Control

    Uses a refactored version of Netty's CORS implementation to provide more
    robust cross-origin resource request functionality.  The CORS specific
    Elasticsearch parameters remain the same, just the underlying
    implementation has changed.

    It has also been refactored in a way that allows dropping in Netty's
    CORS handler as a replacement once Elasticsearch is upgraded to Netty 4.