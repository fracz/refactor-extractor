commit ed369dc9996287d1515581fb23d0a9571fe9a404
Author: Ilya.Kazakevich <Ilya.Kazakevich@jetbrains.com>
Date:   Wed Feb 1 19:39:38 2017 +0300

    New test runners: tests moved to new configuration:
    * Some tests fail. Will fix.
    * py files renamed to prevent them from unexpected discovering
    * "prefixes" added to tests as a way to provide different targets
    * verbosity improved
    * suite changed to "non leaf" since there are no suites in new runners