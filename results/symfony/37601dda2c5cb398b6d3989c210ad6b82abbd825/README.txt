commit 37601dda2c5cb398b6d3989c210ad6b82abbd825
Merge: 16b5614 b5990be
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Jan 7 09:02:39 2015 +0100

    feature #13252 [Serializer] Refactoring and object_to_populate support. (dunglas)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [Serializer] Refactoring and object_to_populate support.

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | License       | MIT
    | Doc PR        | n/a

    This PR is mainly a refactoring. It move up the common code of `PorpertyNormalizer` and `GetSetMethodNormalizer` into `AbstractNormalizer`.

    It also adds a news feature: the ability to use an existing object instance for denormalization:

    ```php
    $dummy = new GetSetDummy();
    $dummy->setFoo('foo');

    $obj = $this->normalizer->denormalize(
        array('bar' => 'bar'),
        'GetSetDummy',
         null,
         array('object_to_populate' => $dummy)
    );

    // $obj and $dummy are references to the same object
    ```

    Commits
    -------

    b5990be [Serializer] Refactoring and object_to_populate support.