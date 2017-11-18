commit 9eda531eea499a6bedc45f9da154e676cd087804
Author: Svetlana Isakova <svetlana.isakova@jetbrains.com>
Date:   Thu Jul 25 18:28:36 2013 +0400

    refactoring: extracted method 'reportTypeInferenceExpectedTypeMismatch'

    to reuse it later from 'tracing for if'
    changed required/found types order in message to synchronize it with 'TYPE_MISMATCH' error