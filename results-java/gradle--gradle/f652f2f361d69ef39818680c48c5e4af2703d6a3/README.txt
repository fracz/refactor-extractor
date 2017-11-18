commit f652f2f361d69ef39818680c48c5e4af2703d6a3
Author: Gary Hale <ghhale@computer.org>
Date:   Tue Sep 16 21:36:52 2014 -0400

    Minor refactoring for component selection rule targeting
    - Moved all validation to ModuleIdentifierNotationParser
    - Changed ComponentSelectionMatchingSpec to accept ModuleIdentifier in constructor
    - Wrapped notation parser errors in a contextual exception
    +review REVIEW-5180