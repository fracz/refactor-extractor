commit cc0420b36b3b03de7dd75f44ef263adaf49b0e06
Author: terrafrost <terrafrost@php.net>
Date:   Sat May 23 11:55:03 2015 -0500

    ANSI: improve vt100 terminal emulation

    store each coordinate's attributes independently and add support
    for a few more escape codes