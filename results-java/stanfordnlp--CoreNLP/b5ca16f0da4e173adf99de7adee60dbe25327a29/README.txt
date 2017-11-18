commit b5ca16f0da4e173adf99de7adee60dbe25327a29
Author: Christopher Manning <manning@cs.stanford.edu>
Date:   Sun Jan 10 01:44:39 2016 -0800

    Minor improvements to StanfordCoreNLP (timing, properties) 1. Restore correct timing of pipeline construction in timing statistics 2. Use String methods on property objects rather than having to cast from Object 3. Change properties file search messages to use logging.