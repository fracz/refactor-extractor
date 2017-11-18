commit 4a89ad39d21fcb6fdd9170dafc99a19a33b05705
Author: limpbizkit <limpbizkit@d779f126-a31b-0410-b53b-1d3aecad763e>
Date:   Thu Jul 24 21:32:33 2008 +0000

    Added requireBinding() convenience method to AbstractModule.

    Also, improved the missing binding warning to include the full type information
      "missing binding to Callable<String>"
    rather than just the partial information
      "missing binding to Callable"

    git-svn-id: https://google-guice.googlecode.com/svn/trunk@566 d779f126-a31b-0410-b53b-1d3aecad763e