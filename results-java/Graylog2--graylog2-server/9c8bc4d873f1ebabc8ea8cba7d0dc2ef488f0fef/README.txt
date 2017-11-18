commit 9c8bc4d873f1ebabc8ea8cba7d0dc2ef488f0fef
Author: Dennis Oelkers <dennis@torch.sh>
Date:   Tue Mar 11 13:19:17 2014 +0100

    Adding support for creating/updating field presence stream rule types

    Unfortunately this stream rule type differs from all the others because
    it has no rule value. To disable the value field in the stream rule
    dialog I added a very dirty hack to the javascript which just hides
    those fields when this stream rule type is selected from the dropdown.
    This was done in a hackish way for two reasons:
    - We decided to refrain from bigger changes to the 0.20 branch in favor
      of the refactored development branch
    - Doing this in a proper way would mean to introduce a dynamic way of
      creating the stream rule add/update form using a dynamic list of
      required attributes. This is possible, but it would mean that we would
      have to change the semantics of the persisted model. Right now we are
      about to refactor all persisted models to a new persistance layer,
      which would make any work to support a behaviour like this obsolete.

    Sincerely yours,