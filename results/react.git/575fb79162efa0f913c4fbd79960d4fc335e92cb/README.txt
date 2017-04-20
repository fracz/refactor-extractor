commit 575fb79162efa0f913c4fbd79960d4fc335e92cb
Author: Dan Abramov <dan.abramov@gmail.com>
Date:   Thu Mar 31 17:58:53 2016 +0100

    Add ReactDebugInstanceMap

    This PR is the first in a series of pull requests split from the new `ReactPerf` implementation in #6046.

    Here, we introduce a new module called `ReactDebugInstanceMap`. It will be used in `__DEV__` and, when the `__PROFILE__` gate is added, in the `__PROFILE__` builds. It will *not* be used in the production builds.

    This module acts as a mapping between “debug IDs” (a new concept) and the internal instances. Not to be confused with the existing `ReactInstanceMap` that maps internal instances to public instances.

    What are the “debug IDs” and why do we need them? Both the new `ReactPerf` and other consumers of the devtool API, such as React DevTools, need access to some data from the internal instances, such as the instance type display name, current props and children, and so on. Right now we let such tools access internal instances directly but this hurts our ability to refactor their implementations and burdens React DevTools with undesired implementation details such as having to support React ART in a special way.[1]

    The purpose of adding `ReactDebugInstanceMap` is to only expose “debug IDs” instead of the internal instances to any devtools. In a future RP, whenever there is an event such as mounting, updating, or unmounting a component, we will emit an event in `ReactDebugTool` with the debug ID of the instance. We will also add an introspection API that lets the consumer pass an ID and get the information about the current children, props, state, display name, and so on, without exposing the internal instances.

    `ReactDebugInstanceMap` has a concept of “registering” an instance. We plan to add the hooks that register an instance as soon as it is created, and unregister it during unmounting. It will only be possible to read information about the instance while it is still registered. If we add support for reparenting, we should be able to move the (un)registration code to different places in the component lifecycle without changing this code. The currently registered instances are held in the `registeredInstancesByIDs` dictionary.

    There is also a reverse lookup dictionary called `allInstancesToIDs` which maps instances back to their IDs. It is implemented as a `WeakMap` so the keys are stable and we’re not holding onto the unregistered instances. If we’re not happy with `WeakMap`, one possible alternative would be to add a new field called `_debugID` to all the internal instances, but we don’t want to do this in production. Using `WeakMap` seems like a simpler solution here (and stable IDs are a nice bonus). This, however, means that the `__DEV__` (and the future `__PROFILE__`) builds will only work in browsers that support our usage of `WeakMap`.

    [1]: https://github.com/facebook/react-devtools/blob/577ec9b8d994fd26d76feb20a1993a96558b7745/backend/getData.js