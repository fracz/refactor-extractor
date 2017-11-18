commit 2b3560b87a5dc052d77a0e903c0d3ef5ae0020c0
Author: johnlenz@google.com <johnlenz@google.com@b0f006be-c8cd-11de-a2e8-8d36a3108c74>
Date:   Mon Dec 3 17:35:31 2012 +0000

    When inferring the template type for a varargs template parameter,
    collect the types of the following parameters as well. This improves
    the handling of Array.prototype.push and similiar functions.

    R=nicksantos
    DELTA=25  (24 added, 0 deleted, 1 changed)


    Revision created by MOE tool push_codebase.
    MOE_MIGRATION=5911


    git-svn-id: https://closure-compiler.googlecode.com/svn/trunk@2362 b0f006be-c8cd-11de-a2e8-8d36a3108c74