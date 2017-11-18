commit a7d49517250531af77a319c769df7d2ec5cbe778
Author: Stefano Mazzocchi <stefano@apache.org>
Date:   Wed Mar 10 07:45:14 2010 +0000

    several improvements for clustering
     - added a unicode ASCII-fiying addition to the fingerprinting functions
     - removed all distance functions for kNN that didn't seem to do anything useful
     - added the ability to indicate what value to use as cluster centroid by simply clicking on it
     (this is useful for those names that have non-ASCII chars that might not even be on your keyboard.. and cut/paste is error prone/cumbersome)
     - added a 10x multiplier to the PPM compression distance which makes it more aligned with the levenshtein ones
     - made sure that we construct a phonetic fingerprint for the whole string and not just the beginning subset
    (performance is still not ideal but it's now reasonable)


    git-svn-id: http://google-refine.googlecode.com/svn/trunk@268 7d457c2a-affb-35e4-300a-418c747d4874