commit cf6fc611979dd21bb8db06ee7a800715eae1904a
Author: Christopher Manning <manning@cs.stanford.edu>
Date:   Wed Nov 25 10:37:02 2015 -0800

    Minor improvements to StanfordCoreNLP (timing, properties) 1. Restore correct timing of pipeline construction in timing statistics 2. Use String methods on property objects rather than having to cast from Object 3. Change properties file search messages to use logging.