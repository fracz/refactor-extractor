commit 8e17d636ef441a9be80977d34acfaabc12982eb7
Author: Simon Willnauer <simonw@apache.org>
Date:   Thu Nov 14 10:17:12 2013 +0100

    Upgrade to Lucene 4.6

    This commit upgrades to Lucene 4.6 and contains the following improvements:

     * Remove XIndexWriter in favor of the fixed IndexWriter
     * Removes patched XLuceneConstantScoreQuery
     * Now uses Lucene passage formatters contributed from Elasticsearch in PostingsHighlighter
     * Upgrades to Lucene46 Codec from Lucene45 Codec
     * Fixes problem in CommonTermsQueryParser where close was never called.

    Closes #4241