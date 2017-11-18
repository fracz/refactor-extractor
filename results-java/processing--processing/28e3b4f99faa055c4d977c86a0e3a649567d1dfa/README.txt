commit 28e3b4f99faa055c4d977c86a0e3a649567d1dfa
Author: Jakub Valtar <jakub.valtar@gmail.com>
Date:   Fri Oct 30 03:27:08 2015 +0100

    Make CompletionCandidate immutable

    This one goes from ASTGenerator on a background thread to the JList
    which displays code suggestions. Until refactored, I'm making it
    immutable with convenience methods returning mutated copies to prevent
    possible threading issues.