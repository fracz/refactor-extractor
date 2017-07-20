commit a1f5d37357d6fbf548062350603da033c7df271f
Author: epriestley <git@epriestley.com>
Date:   Mon May 8 16:12:27 2017 -0700

    Improve the behavior of PhabricatorFileEditField for Macros

    Summary:
    See D17848. This improves things a little bit in two cases:

    Case 1:

      - Create a macro.
      - Pick a valid file.
      - Pick an invalid name.
      - Submit form.
      - Before patch: your file is lost and you have to pick it again.
      - After patch: your file is "held" in the form, you just can't see it in the UI. If you submit again, it keeps the same file. If you pick a new file, it uses that one instead.

    Case 2:

      - Apply D17848.
      - Delete the `if ($value) {` thing that I'm weirded out about (see inline).
      - Edit a macro.
      - Don't pick a new file.
      - Before patch: error, can't null the image PHID.
      - Afer patch: not picking a new file means "keep the same file", but you can't tell from the UI.

    Basically, the behaviors are good now, they just aren't very clear from the UI since "the field has an existing/just-submitted value" and "the field is empty" look the same. I think this is still a net win and we can fix up the UI later.

    Test Plan: See workflows above.

    Reviewers: chad

    Reviewed By: chad

    Differential Revision: https://secure.phabricator.com/D17853