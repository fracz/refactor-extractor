commit e72c38aabbc181065c24190e82214d7f8fe4144a
Author: Pavel V. Talanov <Pavel.Talanov@jetbrains.com>
Date:   Sat Oct 6 20:39:38 2012 +0400

    Make members in *Resolver classes private where possible and classes themselves final

    Overall: very roughly split monster class JavaDescriptorResolver into smaller pieces of functionality represented by *ResolverClasses
    It by no means improves abstraction (and no significant changes to the logic has been made) but makes the whole subsystem more structured and easier to understand/modify in the future