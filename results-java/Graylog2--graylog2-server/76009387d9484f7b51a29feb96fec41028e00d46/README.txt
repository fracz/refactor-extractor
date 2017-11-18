commit 76009387d9484f7b51a29feb96fec41028e00d46
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Wed Jun 18 15:54:37 2014 +0200

    update guava to 17.0

     - StopWatch changed interface, trivial
     - ServiceManager and Service changed non trivially
       - refactored the startup sequence slightly, services never start other services now