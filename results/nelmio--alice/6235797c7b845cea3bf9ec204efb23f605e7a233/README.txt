commit 6235797c7b845cea3bf9ec204efb23f605e7a233
Author: Théo FIDRY <theo.fidry@gmail.com>
Date:   Tue Jul 12 18:28:08 2016 +0100

    Fix composite parameters (#423)

    In b80fdfa (https://github.com/nelmio/alice/pull/355), the Processor method Parameterized has been refactored to be more readable and robust. In the process, a non covered case involving composite keys has been broken. This commit fix the BC break introduced and add a test case for it.