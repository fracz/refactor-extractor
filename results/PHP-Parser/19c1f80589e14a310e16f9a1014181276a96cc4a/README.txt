commit 19c1f80589e14a310e16f9a1014181276a96cc4a
Author: nikic <nikita.ppv@googlemail.com>
Date:   Tue Apr 3 22:47:41 2012 +0200

    Add simple templating support.

    Templates use __name__ placeholders. A variant of the placeholder with a
    capitalized first latter can be accessed using __Name__ (this is useful
    for camel case identifiers, e.g. get__Name__).

    Currently the implemention is not particularly clean, because the Template
    instantiates a Lexer itself. Fixing this requires a major refactoring of
    the lexer/parser interface.