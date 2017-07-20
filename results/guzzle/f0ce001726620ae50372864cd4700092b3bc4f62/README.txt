commit f0ce001726620ae50372864cd4700092b3bc4f62
Author: Michael Dowling <mtdowling@gmail.com>
Date:   Tue Sep 18 00:17:33 2012 -0700

    Horrible optimizations to the JSON schema validation to limit the number of calls made when validating. Resulted in about a 35% speed improvement of a core feature that could potentially be called hundreds of times when sending complex requests.