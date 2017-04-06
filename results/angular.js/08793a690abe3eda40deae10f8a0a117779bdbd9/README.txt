commit 08793a690abe3eda40deae10f8a0a117779bdbd9
Author: Daniel Tabuenca <dtabuenca@solutionstream.com>
Date:   Fri Dec 13 15:20:31 2013 -0800

    refactor(ngTransclude): use transclusion function passed in to link

    Since we now pass in the transclusion function directly to the link function, we no longer need
    the old scheme whereby we saved the transclude function injected into the controller for later
    use in during linking.

    Additionally, this change may aid in correcting a memory leak of detached DOM nodes (see #6181
    for details).

    This commit removes the controller and simplifies ngTransclude.

    Closes #5375
    Closes #6181