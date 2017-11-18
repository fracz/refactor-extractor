commit b3609de8e9be3ef73454e7abbe11bdbbb98028cb
Author: Christopher Manning <manning@cs.stanford.edu>
Date:   Sun Jul 24 20:30:29 2016 -0700

    No functional diffs but mid-dot, non-BMP, testing improvements. 1. Remove repeated code by adding a method to check whether mid-dot 2. Rewrite codepoint processing for non-BMP characters to be more efficient 3. Add a unit test class.