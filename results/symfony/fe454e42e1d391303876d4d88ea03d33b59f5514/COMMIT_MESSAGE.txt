commit fe454e42e1d391303876d4d88ea03d33b59f5514
Merge: 6a0ee38 926aa48
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Dec 2 12:58:15 2016 +0100

    feature #20509 [Serializer] Allow to specify a single value in @Groups (dunglas)

    This PR was squashed before being merged into the 3.3-dev branch (closes #20509).

    Discussion
    ----------

    [Serializer] Allow to specify a single value in @Groups

    | Q             | A
    | ------------- | ---
    | Branch?       | master
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | https://github.com/symfony/symfony/pull/19374#issuecomment-256688002
    | License       | MIT
    | Doc PR        | todo

    Tiny DX improvement:

    Before:

    ```php
    use Symfony\Component\Serializer\Annotation\Groups;

    class Product
    {
        /**
         * @Groups({"admins"})
         */
        public $itemsSold;
    }
    ```

    Now allowed:

    ```php
    use Symfony\Component\Serializer\Annotation\Groups;

    class Product
    {
        /**
         * @Groups("admins")
         */
        public $itemsSold;
    }
    ```

    Commits
    -------

    926aa48 [Serializer] Allow to specify a single value in @Groups