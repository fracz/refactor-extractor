commit a39558260529b5361f1aa6e875282b9916850857
Merge: 74d539d 7783a05
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Jun 22 22:22:54 2011 +0200

    merged branch stloyd/choicetype (PR #1371)

    Commits
    -------

    7783a05 Removed unused code from DateType Additional tests for ChoiceType and DateType based code
    cdd39ac Added ability to set "empty_value" for `DateTimeType`, `DateType` and `TimeType` Additional tests covering added code
    af4a7d7 More tests and more compatible code, with some suggestions from @helmer
    527b738 Test covered version of fix for issue #1336

    Discussion
    ----------

    [Form] Added ability to set "empty_value" for choice list

    Hey,

    This PR is similar to #1336, but this one is fully test covered and have few change in behavior:

    - if choice field is not set as non-required, `empty_value` is not added automaticly,
    - also `empty_value` is not set if field have option `multiple` or `expanded`,
    - `empty_value` for `DateType` and `TimeType` can be set "global" or per field, i.e.:

    ```
    $builder->add('date', 'choice', array('required' => false, 'empty_value' => array('day' => 'Choose day')));
    ```

    - `DateType` and `TimeType` code was cleaned a bit,
    - added missing option to set up choice list as required when using PHP templates

    ---------------------------------------------------------------------------

    by stloyd at 2011/06/20 04:55:45 -0700

    @fabpot I'm just not sure is that change with removing "auto-adding" of `empty_value` is good (probably BC)

    ---------------------------------------------------------------------------

    by lenar at 2011/06/20 05:24:02 -0700

    Now this is a really nice way to hijack work done by others. Really encourages newcomers. Gratz!

    ---------------------------------------------------------------------------

    by fabpot at 2011/06/20 05:57:40 -0700

    @lenar: if the code in this PR is yours (at least partly), I'm not going to merge it. @stloyd, can you clarify this issue with @lenar? Thanks.

    ---------------------------------------------------------------------------

    by lenar at 2011/06/20 06:21:11 -0700

    It's @helmer's mostly, not mine, but the issue stays.

    ---------------------------------------------------------------------------

    by fabpot at 2011/06/20 06:26:15 -0700

    No matter who the code belongs to, Git allows us to keep track of all contributors. So, we need to do our best to not loose any code ownership.

    ---------------------------------------------------------------------------

    by helmer at 2011/06/20 06:58:03 -0700

    I do not care much for ownership, just that this kind of cooperation (or lack thereof) is kind of exhausting. Closed #1336.

    ---------------------------------------------------------------------------

    by stloyd at 2011/06/20 07:47:53 -0700

    @fabpot, @lenar: This PR is inspired by #1336, made by @helmer, but after looking at his code and talking with him, we cant (IMO) get an consensus. So I wrote this PR as an another way to fix issue described in #1336.

    __Summary__: I don't think this one is better than fix at #1336, it's more like another approach to fix that issue.

    ---------------------------------------------------------------------------

    by helmer at 2011/06/20 08:15:59 -0700

    @stloyd: I actually think your variant is better, so good job there, thanks.

    It just ain't nice to:
    1) Comment on my changes being useless due to lack of tests
    2) Writing brand new testsuite from your perspective that "proves" my approach is "wrong" (while ignoring my answers, why I did something precisely like I did, which I did in sync with @fabpot comments on his first attempt to improve the issue)
    3) Saying my PR is broken because your new tests against it fail
    4) Changing functionality to "fix" something that was not really broken

    Other than that, I wanted to contribute a few lines to improve something relatively simple, and it ended up in a huge mess with more lost hours than I planned to spend on it.

    On the bright side, we ended up with something good (:

    ---------------------------------------------------------------------------

    by stloyd at 2011/06/20 08:32:30 -0700

    @helmer: 1) & 2) Sorry for that "bad language", but you get me wrong a bit. Tests was written for code in master (there was no problem to change them to work with your POV). 3) Same as before, you can adopt tests easily, but never mind. Maybe later we could cooperate better ;-)
    About 4) I mentioned it in description of this PR and that was point I was disagreeing with you (also about how "default" options are adopted in fields) :-)