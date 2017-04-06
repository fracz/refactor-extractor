commit c00c0fa020a2ab3391a38312aba898db517f9f47
Author: Christoph Büscher <christoph@elastic.co>
Date:   Tue Jan 26 09:59:11 2016 +0100

    Initial refactoring for phrase suggester

    Adding initial serialization methods (readFrom, writeTo) to the
    PhraseSuggestionBuilder, also adding the base test framework for
    serialiazation testing, equals and hashCode. Moving SuggestionBuilder
    out of the global SuggestBuilder for better readability.