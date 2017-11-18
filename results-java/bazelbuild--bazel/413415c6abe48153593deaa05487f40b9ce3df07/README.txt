commit 413415c6abe48153593deaa05487f40b9ce3df07
Author: Kush Chakraborty <kush@google.com>
Date:   Fri Mar 10 16:15:51 2017 +0000

    A no-op refactoring of BazelJavaSemantics to create a separate internal class which handles the Classpath substitution, and slightly simplifying the logic to obtain the main class.

    This is a partial rollforward of commit 786cfa2ed980e278c42ee474408844f7e3720385 (without the scary changes!)

    --
    PiperOrigin-RevId: 149759252
    MOS_MIGRATED_REVID=149759252