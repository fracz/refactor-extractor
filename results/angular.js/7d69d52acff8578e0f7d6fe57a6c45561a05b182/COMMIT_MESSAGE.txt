commit 7d69d52acff8578e0f7d6fe57a6c45561a05b182
Author: Matias Niemelä <matias@yearofmoo.com>
Date:   Thu Jul 11 20:58:23 2013 -0400

    chore(ngView): $animate refactoring + transclusion & tests

    BREAKING CHANGE: previously ngView only updated its content, after this change
    ngView will recreate itself every time a new content is included. This ensures
    that a single rootElement for all the included contents always exists, which makes
    definition of css styles for animations much easier.