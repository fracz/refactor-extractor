commit 28b2f098beb2dcf1e3b6745c65c1194d45da0691
Author: Dries Buytaert <dries@buytaert.net>
Date:   Mon Jun 8 05:00:12 2009 +0000

    - Patch #334283 by Damien Tournoud, andypost, Freso et al: add context to t() to help deal with ambigious strings (and improved the locale APIs a bit). Example: May is both a short month name as a long month name in English, but not necessarily in other languages.