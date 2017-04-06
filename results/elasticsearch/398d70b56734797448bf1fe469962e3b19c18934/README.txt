commit 398d70b56734797448bf1fe469962e3b19c18934
Author: Adrien Grand <jpountz@gmail.com>
Date:   Tue Jul 5 11:08:45 2016 +0200

    Add `scaled_float`. #19264

    This is a tentative to revive #15939 motivated by elastic/beats#1941.
    Half-floats are a pretty bad option for storing percentages. They would likely
    require 2 bytes all the time while they don't need more than one byte.

    So this PR exposes a new `scaled_float` type that requires a `scaling_factor`
    and internally indexes `value*scaling_factor` in a long field. Compared to the
    original PR it exposes a lower-level API so that the trade-offs are clearer and
    avoids any reference to fixed precision that might imply that this type is more
    accurate (actually it is *less* accurate).

    In addition to being more space-efficient for some use-cases that beats is
    interested in, this is also faster that `half_float` unless we can improve the
    efficiency of decoding half-float bits (which is currently done using software)
    or until Java gets first-class support for half-floats.