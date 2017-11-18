commit 8616949935276f891ad3bd58f0411767c2538dae
Author: Manuel Klimek <klimek@google.com>
Date:   Tue Apr 19 18:03:27 2016 +0000

    Rename CppCompilationContext.getCompilationPrerequisites to
    getTransitiveCompilationPrerequisites. The missing 'transitive' in the name is
    misleading.

    This change is a preparation for further refactorings that will in the end
    introduce a getCompilationPrerequisites method that actually returns the
    compilation prerequisites for a rule - as that will be require some more things
    to be shuffled around in CppCompilationContext, this change gets the purely
    syntactical change in first.

    --
    MOS_MIGRATED_REVID=120247461