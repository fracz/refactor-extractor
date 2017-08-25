commit 0e68cdd9d2f4ecc284742b8809f2b9f1a0513eae
Author: Uwe.Tews <uwe.tews@localhost>
Date:   Sat Sep 19 13:22:32 2009 +0000

    - replace internal "eval()" calls by "include" during rendering process
    - speed improvment for templates which have included subtemplates
        the compiled code of included templates is merged into the compiled code of the parent template
    - added logical operator "xor" for {if} tag
    - changed parameter ordering for Smarty2 BC
        fetch($template, $cache_id = null, $compile_id = null, $parent = null)
        display($template, $cache_id = null, $compile_id = null, $parent = null)
        createTemplate($template, $cache_id = null, $compile_id = null, $parent = null)
    - property resource_char_set is now replaced by constant SMARTY_RESOURCE_CHAR_SET
    - fixed handling of classes in registered blocks
    - speed improvement of lexer on text sections