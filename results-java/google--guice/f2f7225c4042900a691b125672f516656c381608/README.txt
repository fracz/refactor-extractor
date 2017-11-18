commit f2f7225c4042900a691b125672f516656c381608
Author: limpbizkit <limpbizkit@d779f126-a31b-0410-b53b-1d3aecad763e>
Date:   Tue Sep 8 00:35:45 2009 +0000

    Possibly the riskiest change in order to improve JSR 330 integration: Now it's possible to directly bind a javax.inject.Provider type literal using our DSL.

    Generally this change would not be binary-compatible with our released API, but type erasure makes it Just Work.

    git-svn-id: https://google-guice.googlecode.com/svn/trunk@1080 d779f126-a31b-0410-b53b-1d3aecad763e