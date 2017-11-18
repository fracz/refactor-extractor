commit 382a10ba143167c791affc767f1f1520c2a9e923
Author: Vladyslav Zhukovskii <vzhukovskii@codenvy.com>
Date:   Tue Jul 19 18:01:42 2016 +0300

    #1850: Remove closed file from the editor's popup list

    Event calling was reorganized in editor tabs and list item, so after closing file, the last ones also removes from the editor's popup list.