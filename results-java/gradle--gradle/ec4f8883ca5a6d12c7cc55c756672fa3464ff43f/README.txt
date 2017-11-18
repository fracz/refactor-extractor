commit ec4f8883ca5a6d12c7cc55c756672fa3464ff43f
Author: Luke Daley <ld@ldaley.com>
Date:   Mon Jul 14 20:41:16 2014 +1000

    Replace uses of model rules with rule source classes.

    The goal is to get rid of the model rules class to make evolving the model registry easier. Only benefit of this change at this point is the improved error reporting due to the strong rule identity provided by rule sources.