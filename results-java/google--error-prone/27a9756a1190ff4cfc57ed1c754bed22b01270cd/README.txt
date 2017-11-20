commit 27a9756a1190ff4cfc57ed1c754bed22b01270cd
Author: cushon <cushon@google.com>
Date:   Mon Nov 2 17:19:32 2015 -0800

    Fix all of the doclint

    This brings Error Prone into compliance with the "improved" 1.8 doclint errors.

    * broken links were replaced with @code tags

    * raw annotations in javadoc were escaped with @code

    * <pre>{@code ... }</pre> was used for multi-line code snippets, *except* where
      the code includes the literal @. In those cases the old-
      style <pre><code> ... </code></pre> was used with {@literal @} for the @
      and html-entity escaping for angle brackets. Since `{@literal @}` is the
      literal " @" and `{@literal@}` is a parse error, lines starting with @
      were manually un-indented one space.

    RELNOTES: N/A
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=106897404