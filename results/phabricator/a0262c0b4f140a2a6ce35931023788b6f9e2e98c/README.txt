commit a0262c0b4f140a2a6ce35931023788b6f9e2e98c
Author: epriestley <git@epriestley.com>
Date:   Fri Feb 14 10:24:40 2014 -0800

    Remove `tokenizer.ondemand`, and always load on demand

    Summary:
    Ref T4420. Tokenizers currently operate in "preload" or "ondemand" modes. In the former mode, which is default, they'll try to load the entire result list when a page loads.

    The theory here was that this would slightly improve the experience for small installs, and once they got big enough they could switch to "ondemand". In practice, several issues have arisen:

      - We generally don't have a good mechanism for telling installs that they should tweak perf config -- `metamta.send-immediately` is the canonical example here. Some large installs are probably affected negatively by not knowing to change this setting, and having settings like this is generally annoying.
      - We have way way too much config now.
      - With the advent of ApplicationSearch, pages like Maniphest make many redundant loads to prefill sources like projects. Most of the time, this data is not used. It's far simpler to switch everything to ondemand than try to deal with this, and dealing with this would mean creating two very complex divergent pathways in the codebase for a mostly theoretical performance benefit which only impacts tiny installs.
      - We've been using `tokenizer.ondemand` forever on `secure.phabricator.com` since we have many thousands of user accounts, and it doesn't seem sluggish and works properly.

    Removing this config is an easy fix which makes the codebase simpler.

    I've retained the ability to use preloaded sources, since they may make sense in some cases (in at least one case -- task priorities -- adding a static source pathway might make sense), and they're part of Javelin itself. However, the code will no longer ever go down that pathway.

    Test Plan: Used `secure.phabricator.com` for years with this setting enabled.

    Reviewers: btrahan, chad

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T4420

    Differential Revision: https://secure.phabricator.com/D8232