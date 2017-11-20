commit cd74e3ba3ccd8bd4782e45f145e7b931d5174d28
Author: David Rodriguez Gonzalez <davidrg131092@gmail.com>
Date:   Fri Nov 18 14:01:58 2016 +0100

    Unique id generator improvements (#2129)

    * Add test for null suffix

    * Don't work with null suffixes

    * Concat StringBuilder calls

    * Calculate StringBuilder capacity on object creation

    * Make test more readable