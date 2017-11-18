commit 59c93b5ebd13ac4df3b38df7516ae89064be873d
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Thu Mar 28 16:12:31 2013 +0100

    moved language/binary plugins/models into new language-base and language-jvm projects

    - refactored and moved some existing code to prevent cyclic dependencies between the plugins project and the new projects
    - deprecated ProcessResources task at its former location and moved it to language-jvm project; gave it new package name and made it public