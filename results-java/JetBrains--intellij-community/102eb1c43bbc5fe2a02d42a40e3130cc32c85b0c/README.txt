commit 102eb1c43bbc5fe2a02d42a40e3130cc32c85b0c
Author: Tagir Valeev <Tagir.Valeev@jetbrains.com>
Date:   Thu May 18 13:06:31 2017 +0700

    DfaFactType improvements after review IDEA-CR-21121

    1. DfaFactMap#calcFromVariable: unchecked warning fixed
    2. NullnessUtil extracted out of CanBeNullFactType
    3. CanBeNullFactType is anonymous now
    4. New Nullness methods moved to NullnessUtil