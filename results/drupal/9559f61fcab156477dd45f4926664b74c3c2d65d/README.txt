commit 9559f61fcab156477dd45f4926664b74c3c2d65d
Author: Dries Buytaert <dries@buytaert.net>
Date:   Mon Nov 13 08:17:45 2000 +0000

    Another batch with a lot of internal updates, yet no visual changes to the
    site:

    - watchdog (rewrite):
       + the collected information provides more details and insights
         for post-mortem research
       + input limitation
    - database abstraction layer:
       + mysql errors are now verbose and is no longer displayed in a
         browser - fixes a possible security risk
    - admin.php:
       + updated watchdog page
       + fixed security flaw
    - diary.php:
       + fixed nl2br problem
    - themes:
       + fixed comment bug in all 3 themes.
    - misc:
       + renamed some global variables for sake of consistency:
          $sitename  -->  $site_name
          $siteurl   -->  $site_url
       + added input check where (a) exploitable and (b) possible
       + added input size check
       + various small improvements
       + fixed various typoes

    ... and much, much more in fact.