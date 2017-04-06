commit e3a2ecf0dbd05c797b4fa8003ef851669c19692c
Author: Igor Minar <igor@angularjs.org>
Date:   Tue Aug 19 10:56:27 2014 -0700

    revert: refactor($compile): automatically append end comment nodes to all element-transclusion templates

    This reverts commit 0d608d041f37a659d8d8ba7a9b688e132587035d.

    The commits caused more breaking changes at Google than initially expected and since its
    benefit is small, so it's not worth keeping.