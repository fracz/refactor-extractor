commit df06f2600dbc1efa0573723fd73b7bb8cdaa2347
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Mon Dec 15 12:09:28 2014 +0300

    PY-8989 Several improvements in PyConvertTripleQuotedStringIntention

    * Do not replace Python-specific escape sequences with
    StringUtil#escapeStringCharacter, take care of quotes only with
    StringUtil#escapeChar
    * Do not show intention for multiline raw strings, because we can't
    insert '\n' inside them.
    * Do not include empty line before closing triple quote in result, but
    insert '\n' in the line before
    * Do not place closing brace on its own line