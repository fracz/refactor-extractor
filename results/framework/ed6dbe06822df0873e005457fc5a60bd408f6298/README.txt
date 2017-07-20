commit ed6dbe06822df0873e005457fc5a60bd408f6298
Author: Sébastien Nikolaou <info@sebdesign.eu>
Date:   Thu Feb 4 21:11:12 2016 +0200

    Refactor and prevent some potential regressions

    Move some methods around, improve naming, and remove repeated method calls,
    because elegant code is good code.

    Also, determine which rules need their parameters fields normalized.
    For example, we don't want to replace asterisks with numbers on regex rules.