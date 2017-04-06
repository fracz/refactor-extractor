commit 4d19239ec4966c82b2cc9793902686f0cbab0fcf
Author: Alexander Reelsen <alexander@reelsen.net>
Date:   Tue Oct 15 10:56:57 2013 +0200

    Add support for Lucene SuggestStopFilter

    The suggest stop filter is an improved version of the stop filter, which
    takes stopwords only into account if the last char of a query is a
    whitespace. This allows you to keep stopwords, but to allow suggesting for
    "a".

    Example: Index document content "a word". You are now able to suggest for
    "a" and get back results in the completion suggester, if the suggest stop
    filter is used on the query side, but will not get back any results for
    "a " as this is identified as a stopword.

    The implementation allows to set the `remove_trailing` parameter for a
    custom stop filter and thus use the suggest stop filter instead of the
    standard stop filter.