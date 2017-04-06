commit 48ac9747a8c9a3206aaff165add1c12e75c79604
Author: Luca Cavanna <cavannaluca@gmail.com>
Date:   Thu Aug 8 17:10:42 2013 +0200

    Added third highlighter type based on lucene postings highlighter

    Requires field index_options set to "offsets" in order to store positions and offsets in the postings list.
    Considerably faster than the plain highlighter since it doesn't require to reanalyze the text to be highlighted: the larger the documents the better the performance gain should be.
    Requires less disk space than term_vectors, needed for the fast_vector_highlighter.
    Breaks the text into sentences and highlights them. Uses a BreakIterator to find sentences in the text. Plays really well with natural text, not quite the same if the text contains html markup for instance.
    Treats the document as the whole corpus, and scores individual sentences as if they were documents in this corpus, using the BM25 algorithm.

    Uses forked version of lucene postings highlighter to support:
    - per value discrete highlighting for fields that have multiple values, needed when number_of_fragments=0 since we want to return a snippet per value
    - manually passing in query terms to avoid calling extract terms multiple times, since we use a different highlighter instance per doc/field, but the query is always the same

    The lucene postings highlighter api is  quite different compared to the existing highlighters api, the main difference being that it allows to highlight multiple fields in multiple docs with a single call, ensuring sequential IO.
    The way it is introduced in elasticsearch in this first round is a compromise trying not to change the current highlight api, which works per document, per field. The main disadvantage is that we lose the sequential IO, but we can always refactor the highlight api to work with multiple documents.

    Supports pre_tag, post_tag, number_of_fragments (0 highlights the whole field), require_field_match, no_match_size, order by score and html encoding.

    Closes #3704