commit 8bca4a68df6df8b8b614fb1a2fc8f313d73577ea
Author: peter <peter@jetbrains.com>
Date:   Wed Dec 18 10:09:07 2013 +0100

    dfa: combine merge by equality and type into a generic relation-based merge

    this improves dfa performance
    and fixes cases when a single global != relation would prevent states from merging