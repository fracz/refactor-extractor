commit cb1270836ce7c9f201f9957ee046c0c160a3ff15
Author: Stanislav Erokhin <Stanislav.Erokhin@jetbrains.com>
Date:   Tue Aug 15 19:07:09 2017 +0300

    [NI] Introduced ResolutionAtom's

    Introduced new model for resolution result: tree of ResolvedAtoms.
    Moved all postprocessing for arguments to front-end module.
    Do not create freshDescriptor -- use freshTypeSubstitutor directly.
    Removed Candidates for variables+invoke.
    Add lazy way for argument analysis -- do not analyze all arguments
    if we have subtyping error in first argument, but if we want report
    all errors, then all arguments checks will be performed.

    Future improvements:
      - optimize constraint system usage inside ResolutionCandidate
      - improve constraint system API
      - improve diagnostic handlers