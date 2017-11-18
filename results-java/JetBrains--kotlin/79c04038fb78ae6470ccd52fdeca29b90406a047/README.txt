commit 79c04038fb78ae6470ccd52fdeca29b90406a047
Author: Alex Tkachman <alex.tkachman@gmail.com>
Date:   Wed Sep 12 10:31:48 2012 +0300

    accessor for private constructors #KT-2716 fixed

    also on the way done small refactoring of JetTypeMapper
    - few code duplicates replace by methods
    - mapToCallableMethod(ConstructorDescriptor) introduced