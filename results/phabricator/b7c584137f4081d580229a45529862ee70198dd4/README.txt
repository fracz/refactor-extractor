commit b7c584137f4081d580229a45529862ee70198dd4
Author: epriestley <git@epriestley.com>
Date:   Thu Jun 6 14:52:40 2013 -0700

    Begin generalizing custom fields

    Summary:
    Ref T1703. We have currently have two custom field implementations (Maniphest, Differential) and are about to add a third (User, see D6122). I'd like to generalize custom fields before doing a third implementation, so we don't back ourselves into the ApplicationTransactions corner we have with Maniphest/Differential/Audit.

    For the most part, the existing custom fields work well and can be directly generalized. There are three specific things I want to improve, though:

      - Integration with ApplicationSearch: Custom fields aren't indexable. ApplicationSearch is now online and seems stable and good. D5278 provides a template for a backend which can integrate with ApplicationSearch, and ApplicationSearch solves many of the other UI problems implied by exposing custom fields into search (principally, giant pages full of query fields). Generally, I want to provide stronger builtin integration between custom fields and ApplicationSearch.
      - Integration with ApplicationTransactions: Likewise, custom fields should support more native integrations with ApplicationTransactions, which are also online and seem stable and well designed.
      - Selection and sorting: Selecting and sorting custom fields is a huge mess right now. I want to move this into config now that we have the UI to support it, and move away from requiring users to subclass a ton of stuff just to add a field.

    For ApplicationSearch, I've adopted and generalized D5278.

    For ApplicationTransactions, I haven't made any specific affordances yet.

    For selection and sorting, I've partially implemented config-based selection and sorting. It will work like this:

      - We add a new configuration value, like `differential.fields`. In the UI, this is a draggable list of supported fields. Fields can be reordered, and most fields can be disabled.
      - We load every avialable field to populate this list. New fields will appear at the bottom.
      - There are two downsides to this approach:
        - If we add fields in the upstream at a later date, they will appear at the end of the list if an install has customized list order or disabled fields, even if we insert them elsewhere in the upstream.
        - If we reorder fields in the upstream, the reordering will not be reflected in install which have customized the order/availability.
        - I think these are both acceptable costs. We only incur them if an admin edits this config, which implies they'll know how to fix it if they want to.
        - We can fix both of these problems with a straightforward configuration migration if we want to bother.
      - There are numerous upsides to this approach:
        - We can delete a bunch of code and replace it with simple configuration.
        - In general, we don't need the "selector" classes anymore.
        - Users can enable available-but-disabled fields with one click.
        - Users can add fields by putting their implementations in `src/extensions/` with zero subclassing or libphutil stuff.
        - Generally, it's super easy for users to understand.

    This doesn't actually do anything yet and will probably see some adjustments before anything starts running it.

    Test Plan: Static checks only, this code isn't reachable yet.

    Reviewers: chad, seporaitis

    Reviewed By: chad

    CC: aran

    Maniphest Tasks: T1703

    Differential Revision: https://secure.phabricator.com/D6147