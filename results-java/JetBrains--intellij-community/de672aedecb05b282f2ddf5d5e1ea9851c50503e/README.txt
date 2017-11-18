commit de672aedecb05b282f2ddf5d5e1ea9851c50503e
Author: Aleksey Pivovarov <AMPivovarov@gmail.com>
Date:   Mon May 30 17:59:28 2016 +0300

    vcs: refactor AnnotateDiffViewerAction - prepare for ThreeSide viewers

    * hide Side logic inside ViewerAnnotator
    * remove lots of "noinspection unchecked"