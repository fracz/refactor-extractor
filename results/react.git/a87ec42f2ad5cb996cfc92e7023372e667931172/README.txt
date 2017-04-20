commit a87ec42f2ad5cb996cfc92e7023372e667931172
Author: Sebastian Markbage <sema@fb.com>
Date:   Wed Oct 19 02:06:36 2016 -0700

    Add more life-cycles to Fiber

    This refactors the initialization process so that we can share
    it with the "module pattern" initialization.

    There are a few new interesting scenarios unlocked by this.
    E.g. constructor -> componentWillMount -> shouldComponentUpdate
    -> componentDidMount when a render is aborted and resumed.
    If shouldComponentUpdate returns true then we create a new
    instance instead of trying componentWillMount again or
    componentWillReceiveProps without being mounted.

    Another strange thing is that the "previous props and state"
    during componentWillReceiveProps, shouldComponentUpdate and
    componentWillUpdate are all the previous render attempt. However,
    componentDidMount's previous is the props/state at the previous
    commit. That is because the first three can execute multiple
    times before a didMount.