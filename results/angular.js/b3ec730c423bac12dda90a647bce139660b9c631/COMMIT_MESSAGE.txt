commit b3ec730c423bac12dda90a647bce139660b9c631
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Tue Aug 26 10:59:39 2014 -0700

    refactor($compile): $$addBindingInfo accepts single expression or an array

    Instead of knowing about `.expressions` property, it just accepts a single expression or an array of expressions.