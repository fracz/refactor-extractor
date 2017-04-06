commit 229828da8ac72efe14f3b23cff04598a9afcd87e
Merge: adb7860 f9b88c6
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Jul 8 21:33:54 2014 +0200

    feature #11210 [FrameworkBundle] Improving bad bundle exception in _controller (weaverryan)

    This PR was merged into the 2.6-dev branch.

    Discussion
    ----------

    [FrameworkBundle] Improving bad bundle exception in _controller

    ...ntroller in a routeHi guys!

    This improves the exception message when you use a bad bundle name in the `_controller` syntax in a routing file. Here is the before and after of the message with this mistake (real bundle is `KnpUniversityBundle`):

    ```yaml
    some_route:
        pattern:  /
        defaults: { _controller: "Knp2UniversityBundle:Course:index" }
    ```

    ![screen shot 2014-06-23 at 9 27 55 pm](https://cloud.githubusercontent.com/assets/121003/3367065/448e8298-fb54-11e3-92ea-9bf04510cb6d.png)

    ![screen shot 2014-06-23 at 9 48 14 pm](https://cloud.githubusercontent.com/assets/121003/3367063/3c79cf36-fb54-11e3-87c4-29428248ee47.png)

    Notice the before and after behavior is the same `InvalidArgumentException` (just a different message).

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT
    | Doc PR        |

    Catching the plain `InvalidArgumentException` from `Kernel::getBundles()` seems a bit "loose". Should we consider creating a new exception (e.g. `BundleDoesNotExistException`) that extends `InvalidArgumentException` and throw it from inside `Kernel::getBundles`? This would allow us to catch more precisely, and it seems like it would be BC.

    Suggestions and thoughts warmly welcome!

    Thanks!

    Commits
    -------

    f9b88c6 Improving the exception message when the bundle name is wrong for the controller in a route