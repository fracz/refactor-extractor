commit 680685bec4f260fe635c66297fc3fb38a5116e07
Author: Dan Abramov <dan.abramov@gmail.com>
Date:   Wed Aug 17 10:04:56 2016 +0100

    Fix slow performance of PropTypes.oneOfType() on misses (#7510)

    It used to be slow whenever a type miss occurred because expensive `Error` objects were being created. For example, with `oneOfType([number, data])`, passing a date would create an `Error` object in `number` typechecker for every item.

    The savings depend on how much commonly you used `oneOfType()`, and how often it had “misses”. If you used it heavily, you might see 1.5x to 2x performance improvements in `__DEV__` after this fix.