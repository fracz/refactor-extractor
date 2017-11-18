commit b0d7625e3e0f8fe464f76b71f8b3f26f1ebadfc4
Author: Arjen Poutsma <apoutsma@pivotal.io>
Date:   Thu Jul 21 12:18:19 2016 +0200

    Reactor StringEncoder into CharSequenceEncoder

    This commit refactors the StringEncoder to a CharSequenceEncoder, in
    order to support StringBuilders, Groovy GStrings, etc.

    Issue: https://github.com/spring-projects/spring-reactive/issues/120