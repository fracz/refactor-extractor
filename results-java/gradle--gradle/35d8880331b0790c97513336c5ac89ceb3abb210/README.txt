commit 35d8880331b0790c97513336c5ac89ceb3abb210
Author: Rene Groeschke <rene@breskeby.com>
Date:   Mon Sep 17 10:04:48 2012 +0200

    GRADLE-2457: some refactorings after review
    - get() returns null if file is not available in filestore
    - replace tempfile usage in add method by using markerfile instead