commit acec6599eb1c7ee5181ddcc88834e72c3dc16fc5
Merge: 64b6980 e2a50ef
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Nov 24 12:52:13 2012 +0100

    merged branch Tobion/patch-1 (PR #5104)

    This PR was merged into the master branch.

    Commits
    -------

    e2a50ef [OptionsResolver] fix normalizer without corresponding option
    5a53821 [OptionsResolver] fix removing normalizers

    Discussion
    ----------

    OptionsResolver: normalizer fix

    setNormalizer() -> replace() -> all() would generate an error.

    ---------------------------------------------------------------------------

    by bschussek at 2012-07-29T16:09:20Z

    Thank you for the fix! Could you please add a test case?

    ---------------------------------------------------------------------------

    by Tobion at 2012-07-30T15:42:26Z

    There is another problem: setNormalizer() (without setting an option) -> all()
    I suggest to simply ignore normalizers that have no corresponding option. Do you agree?

    ---------------------------------------------------------------------------

    by Tobion at 2012-07-30T16:19:24Z

    On the other hand, one could argue that a normalizer without option should also work like this:
    ```
    $this->options->setNormalizer('foo', function (Options $options) {
            return '';
    });
    $this->assertEquals(array('foo' => ''), $this->options->all());
    ```

    But when having a normalizer that wants a previous value as param, it does not work (because there is none).

    ---------------------------------------------------------------------------

    by stof at 2012-07-30T16:30:34Z

    @Tobion according to github, this need to be rebased

    ---------------------------------------------------------------------------

    by bschussek at 2012-07-30T19:16:48Z

    I guess setNormalizer() should check whether the option is set and fail otherwise. The second possibility, as you say, is to ignore them in all(). I'd prefer whatever is more efficient.

    ---------------------------------------------------------------------------

    by bschussek at 2012-07-30T19:17:27Z

    But setting a normalizer without setting an option, and having that option appear in the final options, does not make sense if you ask me.

    ---------------------------------------------------------------------------

    by Tobion at 2012-07-30T21:23:46Z

    Well it could make sense. If you want to override/normalize an option to a given value however it has been overloaded by others or just not overloaded at all. This is what normalizers do. I think its more consistent than the other solutions.
    Raising exception in setNormalizer would make the Class dependent on the order you call the methods, e.g. `setNormalizer(); set()` would not work. But the other way round would be ok.
    Ignoring some normalizers in `all` would be strange because they are there but not applied under some circumstances.

    ---------------------------------------------------------------------------

    by Tobion at 2012-07-30T21:42:40Z

    Added the fix. If you disagree tell me.

    ---------------------------------------------------------------------------

    by bschussek at 2012-08-04T09:30:18Z

    > Raising exception in setNormalizer would make the Class dependent on the order you call the methods, e.g. `setNormalizer(); set()` would not work. But the other way round would be ok.

    I think this would be a better solution. I dislike if the normalizer magically adds an option that does not exist. This could hide implementation error, e.g. when a refactoring removes an option, but the normalizer is forgotten. Can you throw an exception in this case?

    Should we find use cases that rely on this to work, we can soften the behavior and remove the exception.

    ---------------------------------------------------------------------------

    by Tobion at 2012-08-04T15:02:51Z

    Well, that would also make it impossible to set a normalizer for on optional option in OptionsResolver.
    So `setOptional` + `setNormalizers` would throw an exception which sounds counter-intuitive. Are you sure about that?

    ---------------------------------------------------------------------------

    by Tobion at 2012-08-17T11:47:58Z

    ping @bschussek

    ---------------------------------------------------------------------------

    by Tobion at 2012-10-07T22:31:44Z

    @bschussek ping

    ---------------------------------------------------------------------------

    by stof at 2012-10-13T18:04:30Z

    @bschussek ping

    ---------------------------------------------------------------------------

    by Tobion at 2012-11-08T09:55:15Z

    @bschussek please let's get this finished.