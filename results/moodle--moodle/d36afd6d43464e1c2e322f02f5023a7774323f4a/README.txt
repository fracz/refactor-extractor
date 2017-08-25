commit d36afd6d43464e1c2e322f02f5023a7774323f4a
Author: skodak <skodak>
Date:   Tue Jan 16 23:42:26 2007 +0000

    MDL-8204 several installer improvements:
    * dirroot can not be changed now to prevent future problems - because we rely on dirname(_FILE_) in several places already
    * dirroot is now used to detect repeated installs into defferent directories in the same browser sessions
    * fixed previous button
    * changed library inlcudes again to fit the normal coding style