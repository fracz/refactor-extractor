commit 5ea51f42fb97bcf51320467d2f9ea0490b4bfced
Author: Chris Beams <cbeams@vmware.com>
Date:   Sat Jan 21 13:27:14 2012 +0100

    Fix and refactor spring-aspects build

     - Fix compileTestJava issue in which test classes were not being
       compiled or run

     - Use built-in eclipse.project DSL instead of withXml closure
       to add AspectJ nature and builder

     - Rename {aspectJ=>aspects}.gradle and format source