commit 731732e936775c20f0d84505162e0d3ab3572148
Author: Justin Ridgewell <somedumbme91@yahoo.com>
Date:   Mon Sep 28 18:34:45 2009 -0400

    Replace all unnecessary double-quotes with single-quotes.

    Although slightly more verbose (having to add ' . $var . ' ), it shows a speed improvement over double-quotes (albeit negligible.) Please see http://wiki.github.com/somedumbme91/stacey/single-quote-vs-double-quote-syntax for justification and testing. A standardized use of concatenation is recommended.