commit 31aa0d9a6c421eff190488c7d843e972d16731b7
Author: Sergio <sergio@Ystad.(none)>
Date:   Sun Jul 26 02:21:32 2009 +0200

    Passing two tests more.

    test_new_document_collect_namespaces
    test_find_with_namespace

    While solving the latter, I need to refactor and improve some
    methods done in the beginning of the project due to problems
    between qualified/local name between Java and libxml.