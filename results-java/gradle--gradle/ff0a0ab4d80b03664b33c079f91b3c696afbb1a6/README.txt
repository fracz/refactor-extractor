commit ff0a0ab4d80b03664b33c079f91b3c696afbb1a6
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Fri Apr 27 12:53:22 2012 +0200

    improvements to TestLogger

    - renamed to TestCountLogger
    - show skipped tests
    - slightly rephrased output
    - make logger remember whether root suite reported failure (shift responsibility from TestSummaryListener)