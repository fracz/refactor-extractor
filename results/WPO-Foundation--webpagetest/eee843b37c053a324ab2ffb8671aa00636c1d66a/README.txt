commit eee843b37c053a324ab2ffb8671aa00636c1d66a
Author: Stefan Burnicki <stefan.burnicki@iteratec.de>
Date:   Thu Jun 23 11:45:57 2016 +0200

    refactor: Use *forStep functions in TestRunResult

    Most functions don't need to pass the run number and cached
    status anymore, which will make the transition to multistep easy.

    GetDevToolsEvents was removed, as there is no usage left in the project.