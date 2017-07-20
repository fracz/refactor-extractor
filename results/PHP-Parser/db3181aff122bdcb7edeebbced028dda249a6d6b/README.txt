commit db3181aff122bdcb7edeebbced028dda249a6d6b
Author: nikic <nikita.ppv@googlemail.com>
Date:   Sun Dec 4 16:52:43 2011 +0100

    More test coverage and doc string parsing fixes

    The parser didn't account for the additional newline after the content of doc strings, which is left there by the tokenizer for some reason. Additoinally esacape sequences were parsed in nowdoc strings.

    Additionally this contains some minor changes to the grammar: Some _list nonterminals were refactored to have the possible single elements in a reparate rule and only assemble those single elements. (This reduces duplication and gives better assignment of line number context.)