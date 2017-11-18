commit cca323568f4c9b014ae37309dadd60181a3798a9
Author: peter <peter@jetbrains.com>
Date:   Fri Oct 2 09:39:20 2015 +0200

    improve find usages performance in XML: don't query references from tags and attributes that won't be of any use

    For that, introduce HintedReferenceHost API and pass reference hints into psi.getReferences