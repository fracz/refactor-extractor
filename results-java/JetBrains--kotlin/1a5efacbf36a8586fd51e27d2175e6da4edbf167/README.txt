commit 1a5efacbf36a8586fd51e27d2175e6da4edbf167
Author: Pavel V. Talanov <talanov.pavel@gmail.com>
Date:   Tue Apr 14 20:39:37 2015 +0300

    Refactor frontend components

    Make dependencies more explicit
    Move components out of ExpressionTypingServices
    Make ExpressionTypingUtils a true utility class, refactor stuff out
    Extract new components: FakeCallResolver, MultiDeclarationResolver, ValueParameterResolver