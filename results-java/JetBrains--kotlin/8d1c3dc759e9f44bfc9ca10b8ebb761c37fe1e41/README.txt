commit 8d1c3dc759e9f44bfc9ca10b8ebb761c37fe1e41
Author: Nikolay Krasko <nikolay.krasko@jetbrains.com>
Date:   Wed Feb 29 15:43:56 2012 +0400

    KT-1151 Code completion for not imported extension functions - refactorings:
    - Don't touch call resolver
    - Check only newly imported descriptors
    - Use Stepa's wrapper for getting boolean attribute from annotation