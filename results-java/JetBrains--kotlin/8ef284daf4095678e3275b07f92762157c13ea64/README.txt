commit 8ef284daf4095678e3275b07f92762157c13ea64
Author: Alexey Andreev <Alexey.Andreev@jetbrains.com>
Date:   Wed Dec 21 14:59:41 2016 +0300

    JS: improve temporary variable elimination in some cases. Apply redundant statement elimination when statement in not synthetic, but its expression is