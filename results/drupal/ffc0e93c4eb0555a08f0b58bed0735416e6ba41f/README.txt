commit ffc0e93c4eb0555a08f0b58bed0735416e6ba41f
Author: Dries Buytaert <dries@buytaert.net>
Date:   Sun Apr 20 18:34:43 2008 +0000

    - Added a test framework to Drupal along with a first batch of tests for
      Drupal core!  This is an important milestone for the project so enable
      the module and check it out ... :)

      Thanks to Rok Žlender, Károly Négyesi, Jimmy Berry, Kevin Bridges, Charlie
      Gordon, Douglas Hubler, Miglius Alaburda, Andy Kirkham, Dimitri13, Kieran
      Lal, Moshe Weitzman, and the many other people that helped with testing
      over the past years and that drove this home.

      It all works but it is still rough around the edges (i.e. documentation
      is still being written, the coding style is not 100% yet, a number of
      tests still fail) but we spent the entire weekend working on it in Paris
      and made a ton of progress.  The best way to help and to get up to speed,
      is to start writing and contributing some tests ... as well as fixing
      some of the failures.

      For those willing to help with improving the test framework, here are
      some next steps and issues to resolve:

        - How to best approach unit tests and mock functions?
        - How to test drupal_mail() and drupal_http_request()?
        - How to improve the admin UI so we have a nice progress bar?
        - How best to do code coverage?
        - See http://g.d.o/node/10099 for more ...