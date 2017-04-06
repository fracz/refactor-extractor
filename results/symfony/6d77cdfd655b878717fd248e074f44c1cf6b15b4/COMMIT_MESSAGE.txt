commit 6d77cdfd655b878717fd248e074f44c1cf6b15b4
Merge: 4abd0c6 7bab217
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Feb 19 14:57:15 2017 -0800

    feature #21478 [Asset] Add support for preloading with links and HTTP/2 push (dunglas)

    This PR was squashed before being merged into the 3.3-dev branch (closes #21478).

    Discussion
    ----------

    [Asset] Add support for preloading with links and HTTP/2 push

    | Q             | A
    | ------------- | ---
    | Branch?       | master
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | n/a
    | License       | MIT
    | Doc PR        | todo

    Allows compatible clients to preload mandatory assets like scripts, stylesheets or images according to [the "preload" working draft of the W3C](https://www.w3.org/TR/preload/).
    Thanks to this PR, Symfony will automatically adds `Link` HTTP headers with a `preload` relation for mandatory assets.  If an intermediate proxy supports HTTP/2 push, it will convert preload headers. For instance [Cloudflare supports this feature](https://blog.cloudflare.com/using-http-2-server-push-with-php/).

    It dramatically increases pages speed and make the web greener because only one TCP connection is used to fetch all mandatory assets (decrease servers and devices loads, improve battery lives).

    Usage:

    Updated version:

    ```html
    <html>
        <body>
        Hello
        <script src="{{ preload(asset('/scripts/foo.js'), 'script') }}"></script>
        </body>
    </html>
    ```

    ~~First proposal:~~

    ```html
    <html>
        <body>
        Hello
        <script src="{{ preloaded_asset('/scripts/foo.js', 'script') }}"></script>
        </body>
    </html>
    ```

    - [x] Add tests

    Commits
    -------

    7bab21700d [Asset] Add support for preloading with links and HTTP/2 push