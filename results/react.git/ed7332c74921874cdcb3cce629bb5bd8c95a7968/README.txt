commit ed7332c74921874cdcb3cce629bb5bd8c95a7968
Author: Sebastian Markbage <sema@fb.com>
Date:   Tue Jan 27 00:55:25 2015 -0800

    Extract setState, setProps etc into ReactUpdateQueue

    I originally did this work so that we could allow setState to be called
    before the internal ReactCompositeComponent was constructed. It's unlikely
    that we'll go down that route now but this still seems like a better
    abstraction. It communicates that this is not immediately updating an
    object oriented class. It's just a queue which a minor optimization.
    It also avoids bloating the ReactCompositeComponent file.

    Since they both depend on the life cycle I break that out into a common
    shared dependency. In a follow up I'll refactor the life-cycle management.