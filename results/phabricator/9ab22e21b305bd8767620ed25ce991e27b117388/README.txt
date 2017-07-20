commit 9ab22e21b305bd8767620ed25ce991e27b117388
Author: epriestley <git@epriestley.com>
Date:   Wed Dec 30 04:36:48 2015 -0800

    Allow installs to customize project icons

    Summary:
    Ref T10010. Ref T5819. General alignment of the stars:

      - There were some hacks in Conduit around stripping `fa-...` off icons when reading and writing that I wanted to get rid of.
      - We probably have room for a subtitle in the new heavy nav, and using the icon name is a good starting point (and maybe good enough on its own?)
      - The project list was real bad looking with redundant tag/names, now it is very slightly less bad looking with non-redundant types?
      - Some installs will want to call Milestones something else, and this gets us a big part of the way there.
      - This may slightly help to reinforce "tag" vs "policy" vs "group" stuff?

    ---

    I'm letting installs have enough rope to shoot themselves in the foot (e.g., define 100 icons). It isn't the end of the world if they reuse icons, and is clearly their fault.

    I think the cases where 100 icons will break down are:

      - Icon selector dialog may get very unwieldy.
      - Query UI will be pretty iffy/huge with 100 icons.

    We could improve these fairly easily if an install comes up with a reasonable use case for having 100 icons.

    ---

    The UI on the icon itself in the list views is a little iffy -- mostly, it's too saturated/bold.

    I'd ideally like to try either:

      - rendering a "shade" version (i.e. lighter, less-saturated color); or
      - rendering a "shade" tag with just the icon in it.

    However, there didn't seem to be a way to do the first one right now (`fa-example sh-blue` doesn't work) and the second one had weird margins/padding, so I left it like this for now. I figure we can clean it up once we build the thick nav, since that will probably also want an identical element.

    (I don't want to render a full tag with the icon + name since I think that's confusing -- it looks like a project/object tag, but is not.)

    Test Plan:
    {F1049905}

    {F1049906}

    Reviewers: chad

    Reviewed By: chad

    Subscribers: 20after4, Luke081515.2

    Maniphest Tasks: T5819, T10010

    Differential Revision: https://secure.phabricator.com/D14918