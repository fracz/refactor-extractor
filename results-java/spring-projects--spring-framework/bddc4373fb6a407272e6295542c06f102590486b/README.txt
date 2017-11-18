commit bddc4373fb6a407272e6295542c06f102590486b
Author: Sam Brannen <sam@sambrannen.com>
Date:   Mon Mar 30 17:32:59 2015 +0200

    Refactor ObjectToObjectConverter & improve exception msg

    - The exception message now mentions lacking to-Object method as well.

    - Documented explicit lacking support for toString() for conversions.

    - Introduced dedicated has*() methods for greater clarity and to reduce
      code duplication.

    - Static factory methods (i.e., of, from, valueOf) are now supported for
      conversion to a String.