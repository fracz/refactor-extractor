commit 27cbf6b3de135ba7908db58b43422a661025db4a
Author: mark_story <mark@mark-story.com>
Date:   Mon Nov 18 21:21:07 2013 -0500

    Undeprecate Controller::paginate() and refactor code.

    * Un-deprecate Controller::paginate() as it now serves as a way to
      automate some behavior that does not belong in the PaginatorComponent.
    * Move adding the PaginatorHelper into the controller method.
    * Remove PaginatorComponent::settings. Settings are now passed into the
      paginate call, allowing PaginatorComponent to maintain very little
      state.
    * Update the various tests.