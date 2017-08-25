commit b56b7dd2cbb27f53fed1571b88554a26f2c3f775
Author: David Kuridža <david@kuridza.si>
Date:   Wed Jan 11 10:37:18 2012 +0100

    Replace deprecated `is_a()` with `instanceof`

    Function `is_a()` became deprecated with PHP 5.0.0 resulting in an `E_STRICT`
    warning. Function is no longer deprecated with PHP 5.3.0, however, using any
    version between 5.0.0 and 5.2.x and `E_DEPRECATED` enabled, `Strict standards`
    notice is shown. This fix refactors all `is_a()` occurrences using `instanceof`
    operator. For example,

      `if (is_a($this, 'SimplePie'))`

    becomes

      `if ($this instanceof SimplePie)`