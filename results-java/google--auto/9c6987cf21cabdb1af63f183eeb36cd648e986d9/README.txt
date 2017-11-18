commit 9c6987cf21cabdb1af63f183eeb36cd648e986d9
Author: cushon <cushon@google.com>
Date:   Wed Jan 7 17:42:39 2015 -0800

    Fixes to enable compilation with a stock javac9

    The latest version of javac9 fixes a long standing type inference bug, with the
    side-effect that javac is no longer able to perform type inference for some
    edge cases involving bounded wildcards:

    The improved type inference introduced in Java >= 8
    works around the worst of these issues. Since the improved inference is
    disabled for this code by the javacopts in
    additional fixes are required
    to support compiling with a stock javac9 and -source/-target 7.

    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=99779515