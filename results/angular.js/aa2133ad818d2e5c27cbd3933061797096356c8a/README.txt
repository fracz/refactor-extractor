commit aa2133ad818d2e5c27cbd3933061797096356c8a
Author: Matias Niemelä <matias@yearofmoo.com>
Date:   Sat Jul 6 00:48:54 2013 -0400

    fix(ngInclude): $animate refactoring + use transclusion

    BREAKING CHANGE: previously ngInclude only updated its content, after this change
    ngInclude will recreate itself every time a new content is included. This ensures
    that a single rootElement for all the included contents always exists, which makes
    definition of css styles for animations much easier.