commit ab9622ffc6694dd33600832b68cd655ead6d6d89
Author: Ekaterina Tuzova <Ekaterina.Tuzova@jetbrains.com>
Date:   Fri Mar 29 16:01:27 2013 +0400

    fixed PY-9274 Broken raw bytes literal parsing for Python 3.3

    refactored lexer, added compatibility warnings, quickfix to remove not only U prefix