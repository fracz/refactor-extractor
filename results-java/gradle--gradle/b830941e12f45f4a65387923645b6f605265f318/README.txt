commit b830941e12f45f4a65387923645b6f605265f318
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Apr 16 22:46:09 2012 +0200

    Scaled down the dynamic properties deprecation warnings.

     The idea is to balance out usefulness with tidyness, especially for the large multi-project builds that configure a lot properties in allprojects/subprojects {}. The main driver are the outputs from some client's builds were those deprecations take dozens of screens, sometimes even taking 90% of the entire build output. Details:

    -The url and a generic message is printed only once, regardless of the number of properties.
    -The warnings are not repeated for the same property on the same type. If there are more properties with the same name on the same type we print a generic information about it (in majority of cases it means the property is created in subprojects {} scriptblock).
    -The value is abbreviated to the first 25 chars to avoid printing some humongous objects (another use case from the client's).

    Pending: The DeprecationLogger needs refactoring (probably on the master, though).