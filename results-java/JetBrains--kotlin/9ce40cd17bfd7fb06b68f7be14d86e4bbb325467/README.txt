commit 9ce40cd17bfd7fb06b68f7be14d86e4bbb325467
Author: Tuomas Tynkkynen <dezgeg@gmail.com>
Date:   Wed Feb 5 16:33:34 2014 +0200

    Fix unnecessary parenthesis around 'this' in refactorings

    Operator priority calculation for 'this' expressions was incorrect in
    JetPsiUtil.getPriority(): it would return a very low value for
    a JetThisExpression, causing the IDE to report that parenthesis
    are necessary in an expression like '(this)[1]'.

    Fix this by making JetThisExpression not implement
    JetStatementExpression by removing the implements clause from
    JetLabelQualifiedInstanceExpression and adding it to all other
    subclasses of JetLabelQualifiedInstanceExpression.

    Fixes KT-4513.