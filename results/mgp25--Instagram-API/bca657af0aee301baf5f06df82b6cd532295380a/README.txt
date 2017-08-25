commit bca657af0aee301baf5f06df82b6cd532295380a
Author: SteveJobzniak <SteveJobzniak@users.noreply.github.com>
Date:   Sat May 27 00:38:53 2017 +0200

    Instagram: Finished refactoring, revert account

    It's way too hard to group all functions into sub-spaces. So I've
    decided to undo the "account" group, and cancel the idea of a "people"
    group (for users/friendships).

    Instead, the sub-spaces have already solved the design goal:

    - We now have sub-spaces for related functions for Direct messaging,
      Live broadcasts, Media actions, Story actions and Timeline actions.

    - We can finally name functions in those sub-spaces very clearly, since
      they will not clash with anything outside.

    They're now easy to find, in clean sub-spaces, such as:

    $ig->story->uploadPhoto();
    $ig->timeline->uploadVideo();
    $ig->direct->sendText();

    But it's nearly impossible to group ALL other class actions into
    subgroups too, because things like "login()" belongs to "Account"
    related actions, but we obviously want that on the main class.

    So we have to draw the line somewhere. And I draw it here. The
    user-related and account-related functions are way too many, and way too
    inconsistent to be able to easily divide them into groups.

    Those will stay in the main class. Other contributors are welcome to
    create more groups, with lots of thought and care. But it's fine that we
    only created five major sub-groups (Direct, Live, Media, Story and
    Timeline) and leave the rest in the core.

    We've shrunken the core from ~5000 lines to ~3000 lines, and made the
    functions easier to find and better named, so that's a win already!