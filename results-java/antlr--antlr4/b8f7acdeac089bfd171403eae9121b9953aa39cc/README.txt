commit b8f7acdeac089bfd171403eae9121b9953aa39cc
Author: Terence Parr <parrt@antlr.org>
Date:   Tue Jul 31 18:26:39 2012 -0700

    refactor to create DFAState target, fill it in, then add to DFA only after it's complete. Combine some helper methods. simpler.