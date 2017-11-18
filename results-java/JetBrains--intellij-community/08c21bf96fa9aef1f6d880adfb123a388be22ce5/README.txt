commit 08c21bf96fa9aef1f6d880adfb123a388be22ce5
Author: Yaroslav Lepenkin <yaroslav.lepenkin@jetbrains.com>
Date:   Wed Jun 10 19:56:38 2015 +0300

    [JavaFormatter] Do not check if annotation is type annotation if next element is keyword. This change slightly improves formatting block building performance on our codebase(at least), since most of the annotations targets are methods and fields with visibility modifiers.