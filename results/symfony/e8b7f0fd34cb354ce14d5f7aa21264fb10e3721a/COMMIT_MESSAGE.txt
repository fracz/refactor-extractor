commit e8b7f0fd34cb354ce14d5f7aa21264fb10e3721a
Merge: c1bd3b5 21291ca
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Mar 25 21:28:18 2013 +0100

    merged branch jfsimon/issue-7413 (PR #7456)

    This PR was merged into the master branch.

    Discussion
    ----------

    Improve bytes conversion method

    This PR improves bytes conversion `regex` method introduced in #7413 (thanks to @vicb's comments).

    * Adds support of `+` prefix.
    * Adds support of blank chars between `+`, number and unit.
    * Adds support of octal/hexa bases.

    Notice that this can not be unit tested for `ServerParams` and `UploadedFile` classes because `ini_set()` function does not work with `post_max_size` and `upload_max_filesize` settings.

    For information, this convertion is located in 3 classes:
    * `Symfony\Component\Form\Extension\Validator\Util\ServerParams`
    * `Symfony\Component\HttpFoundation\File\UploadedFile`
    * `Symfony\Component\HttpKernel\DataCollector\MemoryDataCollector`

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #7413

    Commits
    -------

    21291ca improved bytes conversion method