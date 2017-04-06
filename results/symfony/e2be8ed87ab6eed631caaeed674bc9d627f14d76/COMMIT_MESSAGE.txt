commit e2be8ed87ab6eed631caaeed674bc9d627f14d76
Merge: e4e3ce6 1422506
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue May 22 11:55:33 2012 +0200

    merged branch bschussek/issue3903 (PR #4341)

    Commits
    -------

    1422506 [Form] Clarified the usage of "constraints" in the UPGRADE file
    af41a1a [Form] Fixed typos
    ac69394 [Form] Allowed native framework errors to be mapped as well
    59d6b55 [Form] Fixed: error mapping aborts if reaching an unsynchronized form
    9eda5f5 [Form] Fixed: RepeatedType now maps all errors to the first field
    215b687 [Form] Added capability to process "." rules in "error_mapping"
    c9c4900 [Form] Fixed: errors are not mapped to unsynchronized forms anymore
    c8b61d5 [Form] Renamed FormMapping to MappingRule and moved some logic there to make rules more extendable
    d0d1fe6 [Form] Added more information to UPGRADE and CHANGELOG
    0c09a0e [Form] Made $name parameters optional in PropertyPathBuilder:replaceBy(Index|Property)
    081c643 [Form] Updated UPGRADE and CHANGELOG
    bbffd1b [Form] Fixed index checks in PropertyPath classes
    ea5ff77 [Form] Fixed issues mentioned in the PR comments
    7a4ba52 [EventDispatcher] Added class UnmodifiableEventDispatcher
    306324e [Form] Greatly improved the error mapping done in DelegatingValidationListener
    8f7e2f6 [Validator] Fixed: @Valid does not recurse the traversal of collections anymore by default
    5e87dd8 [Form] Added tests for the case when "property_path" is null or false. Instead of setting "property_path" to false, you should set "mapped" to false instead.
    2301b15 [Form] Tightened PropertyPath validation to reject any empty value (such as false)
    7ff2a9b Revert "[Form] removed a constraint in PropertyPath as the path can definitely be an empty string for errors attached on the main form (when using a constraint defined with the 'validation_constraint' option)"
    860dd1f [Form] Adapted Form to create a deterministic property path by default
    03f5058 [Form] Fixed property name in PropertyPathMapperTest
    c2a243f [Form] Made PropertyPath deterministic: "[prop]" always refers to indices (array or ArrayAccess), "prop" always refers to properties
    2996340 [Form] Extracted FormConfig class to simplify the Form's constructor

    Discussion
    ----------

    [Form] Improved the error mapping and made property paths deterministic

    Bug fix: yes
    Feature addition: no
    Backwards compatibility break: **yes**
    Symfony2 tests pass: yes
    Fixes the following tickets: #1971, #2945, #3272, #3308, #3903, #4329
    Probably fixes: #2729
    Todo: -

    This PR is ready for review.

    The algorithm for assigning errors to forms in the form tree was improved a lot. Also the error mapping works better now. There are still a few features to be added (e.g. wildcards "*"), but these can be implemented now pretty easily.

    This PR breaks PR in that a form explicitely needs to set the "data_class" option if it wants to map to an object and needs to leave that option empty if it wants to map to an array.

    Furthermore, property paths must be deterministic now: `foo` now only maps to `(g|s)etFoo()`, but not the index `["foo"]` (array or ArrayAccess), while `[foo]` only maps to the latter but not the former. See #3903 for more information.

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-19T21:35:24Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1377086) (merged 9e346990 into 22294617).

    ---------------------------------------------------------------------------

    by Tobion at 2012-05-20T01:47:48Z

    Good stuff in general :)

    ---------------------------------------------------------------------------

    by bschussek at 2012-05-20T09:19:18Z

    Fixed everything mentioned here so far.

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-20T09:22:22Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1379548) (merged 49918bef into 22294617).

    ---------------------------------------------------------------------------

    by Tobion at 2012-05-20T14:29:14Z

    many occurences of two spaces after `@param` (should be only one space).

    ---------------------------------------------------------------------------

    by Koc at 2012-05-20T14:40:18Z

    Sorry, I'm cannot observe all changes for form component in 2.1, so I have a question:

    ```php
    <?php

    protected $isPrivate;

    public function isPrivate() {}

    public function setPrivate() {}
    ```

    Is it possible validate this property with accessors/mutators from code above in 2.1 now?

    ---------------------------------------------------------------------------

    by bschussek at 2012-05-20T14:41:09Z

    The type after `@param` used to be aligned with the type of the `@return` tag. Let's get the PHPDoc-guidelines straight before nitpicking more on these trivialities.

    ---------------------------------------------------------------------------

    by bschussek at 2012-05-20T14:42:34Z

    @Koc Please move your question to the user mailing list, let's keep this PR on topic.

    ---------------------------------------------------------------------------

    by bschussek at 2012-05-20T14:45:42Z

    Fixed everything mentioned until now.

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-20T14:47:48Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1380903) (merged 03d60a03 into f433f6b0).

    ---------------------------------------------------------------------------

    by bschussek at 2012-05-20T15:18:12Z

    CHANGELOG/UPGRADE is now updated.

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-20T15:19:39Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1381047) (merged 48cc3eca into f433f6b0).

    ---------------------------------------------------------------------------

    by Tobion at 2012-05-20T16:16:51Z

    All the deprecated methods and changed constructor arguments should probably be mentioned in the changelog/upgrade.

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-21T07:31:47Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1386621) (merged c0ef69a1 into 1407f112).

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-21T08:01:46Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1386826) (merged 4f3fc1fe into 1407f112).

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-21T09:22:30Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1387263) (merged e3675050 into 1407f112).

    ---------------------------------------------------------------------------

    by bschussek at 2012-05-21T09:43:08Z

    This PR now fixes #1971.

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-21T09:45:51Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1387370) (merged de33f9ef into 1407f112).

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-21T11:06:53Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1387838) (merged da3b562e into 1407f112).

    ---------------------------------------------------------------------------

    by bschussek at 2012-05-21T11:07:45Z

    This PR now fixes #2945.

    ---------------------------------------------------------------------------

    by bschussek at 2012-05-21T15:33:33Z

    Native errors (such as "invalid", "extra_fields" etc.) are now respected by the "error_mapping" option as well. The option "validation_constraint" was deprecated, "constraints" is its replacement and a lot handier, because it allows you to work easily with arrays.

    ```php
    <?php
    $builder
        ->add('name', 'text', array(
            'constraints' => new NotBlank(),
        ))
        ->add('phoneNumber', 'text', array(
            'constraints' => array(
                new NotBlank(),
                new MinLength(7),
                new Type('numeric')
            )
        ));
    ```

    Ready for review again.

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-21T15:33:45Z

    This pull request [fails](http://travis-ci.org/symfony/symfony/builds/1390239) (merged e162f56d into ea33d4d3).

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-21T15:40:02Z

    This pull request [fails](http://travis-ci.org/symfony/symfony/builds/1390367) (merged e8729a7f into ea33d4d3).

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-21T16:06:03Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1390663) (merged ef39aba4 into ea33d4d3).

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-22T08:54:36Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1398153) (merged af41a1a5 into e4e3ce6c).

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-22T09:26:12Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1398415) (merged 14225067 into e4e3ce6c).