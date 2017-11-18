commit 995a55925a5e97edc6e7b571e6d7e6d4bbc45e94
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Dec 16 15:39:50 2011 +0100

    Tooling api improvements around compatibility...

    -Added a specific exception that is thrown when tooling api client attempts to use a method that is not supported in a give target gradle version. This way clients can handle this particular exception and implement compatible solutions.
    -Made it clear in the exception message what are the way to resolve the problem.