commit b3018d983e7073232aa35b990aa0677c0f2a7faf
Author: mknichel <mknichel@google.com>
Date:   Wed Jul 27 13:29:11 2016 -0700

    The Node object is a really memory expensive object to hold on to since it contains references to the entire AST and compiler. This change removes the pointer to the Node when constructing a SuggestedFix class for JavaScript refactoring. Since some tools later down the line need some information about the original matched node, a new class is introduced to keep track of just that information.

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=128621732