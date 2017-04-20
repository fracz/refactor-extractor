commit ef9de8ffffd729675e40ece7576e3475804aa3b2
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Wed May 1 12:28:45 2013 -0500

    [#4311] CS review

    - Made sure file-level docblocks were consistent throughout
    - Alphabetized constant and property declarations
    - Ensured methods were in public -> protected -> private order
    - s/$self/this/ in all @return annotations
    - Ensured annotations were available and correct for all public methods
    - A few minor formatting changes for readability