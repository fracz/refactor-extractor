commit f54d3e8ac72a3cc60c31c4081f931495172d6766
Author: Matthieu Napoli <matthieu@mnapoli.fr>
Date:   Thu Apr 9 23:39:59 2015 +1200

    #214 Extend definitions

    - refactored definition sources to a simpler design
    - class definitions do not extend previous definition anymore
    - `DI\extend()` allows to create a new "class definition extension" that do extend the previous (or any other) definition
    - reworked how the cache is used