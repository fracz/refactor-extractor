commit 0d608d041f37a659d8d8ba7a9b688e132587035d
Author: Igor Minar <igor@angularjs.org>
Date:   Wed Aug 13 00:16:33 2014 -0700

    refactor($compile): automatically append end comment nodes to all element-transclusion templates

    Previously we would do it manually in all of our structural directives.

    BREAKING CHANGE: element-transcluded directives now have an extra comment automatically appended to their cloned DOM

    This comment is usually needed to keep track the end boundary in the event child directives modify the root node(s).
    If not used for this purpose it can be safely ignored.