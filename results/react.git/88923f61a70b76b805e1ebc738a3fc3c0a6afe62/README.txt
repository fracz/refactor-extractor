commit 88923f61a70b76b805e1ebc738a3fc3c0a6afe62
Author: CommitSyncScript <jeffmo@fb.com>
Date:   Thu Jun 6 14:44:56 2013 -0700

    Improve Browser Support for `wheel` Event

    This improved browser support for the `wheel` event.

     - Try to use `wheel` event (DOM Level 3 Specification).
     - Fallback to `mousewheel` event.
     - Fallback to `DOMMouseWheel` (older Firefox).

    Also, since `wheel` is the standard event name, let's use that in React.

    NOTE: The tricky part was detecting if `wheel` is supported for IE9+ because `onwheel` does not exist.

    Test Plan:
    Execute the following in the console on a page with React:

      var React = require('React');
      React.renderComponent(React.DOM.div({
        style: {
          width: 10000,
          height: 10000
        },
        onWheel: function() {
          console.log('wheel');
        }
      }, null), document.body);

    Verified that mousewheel events are logged to the console.
    Verified in IE8-10, Firefox, Chrome, and Safari.