commit b45f19c0e26aebd919c388585c762f50760f6d2f
Author: Alex Tkachman <alex.tkachman@gmail.com>
Date:   Thu Aug 30 09:33:10 2012 +0300

    GenerationState refactored to be (almost) immutable and methodless (modulo little protection from reuse)
    GenerationStrategy introduced to handle separately cases of regular compilation and building of JetLightClasses (this was only real overridable behavior of GenerationState in old code)
    state subpackage introduced