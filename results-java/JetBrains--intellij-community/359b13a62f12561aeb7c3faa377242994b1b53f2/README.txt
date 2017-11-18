commit 359b13a62f12561aeb7c3faa377242994b1b53f2
Author: Andrey Vlasovskikh <andrey.vlasovskikh@gmail.com>
Date:   Wed Jan 20 13:14:26 2016 +0300

    Resolve PyReferenceImpl refs to all outer definitions in the current file

    This applies to all outer definitions except imports where we resolve
    only to the latest imports as we used to do. This is required for
    detecting and optimizing unused imports and adding imports during
    refactorings.

    The implementation of ResolveProcessor has been rewritten as
    PyResolveProcessor.