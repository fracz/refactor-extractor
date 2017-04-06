commit a05379eab091caff871c404e1425ccbda4131ed0
Merge: 269e27f 48491c4
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Oct 20 11:20:49 2014 +0200

    feature #12098 [Serializer] Handle circular references (dunglas)

    This PR was merged into the 2.6-dev branch.

    Discussion
    ----------

    [Serializer] Handle circular references

    | Q             | A
    | ------------- | ---
    | Bug fix?      | Yes: avoid infinite loops. Allows to improve #5347
    | New feature?  | yes (circular reference handler)
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | License       | MIT
    | Doc PR        | symfony/symfony-docs#4299

    This PR adds handling of circular references in the `Serializer` component.
    The number of allowed iterations is configurable (one by default).
    The behavior when a circular reference is detected is configurable. By default an exception is thrown. Instead of throwing an exception, it's possible to register a custom handler (e.g.: a Doctrine Handler returning the object ID).

    Usage:
    ```php
    use Symfony\Component\Serializer\Normalizer\GetSetMethodNormalizer;
    use Symfony\Component\Serializer\Serializer;

    class MyObj
    {
        private $id = 1312;

        public function getId()
        {
            return $this->getId();
        }

        public function getMe()
        {
            return $this;
        }
    }

    $normalizer = new GetSetMethodNormalizer();
    $normalizer->setCircularReferenceLimit(3);
    $normalizer->setCircularReferenceHandler(function ($obj) {
        return $obj->getId();
    });

    $serializer = new Serializer([$normalizer]);
    $serializer->normalize(new MyObj());
    ```

    Commits
    -------

    48491c4 [Serializer] Handle circular references