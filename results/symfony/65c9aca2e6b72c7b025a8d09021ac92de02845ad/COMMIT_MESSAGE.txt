commit 65c9aca2e6b72c7b025a8d09021ac92de02845ad
Merge: eede330 dce66c9
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Mar 1 03:56:57 2014 +0100

    feature #10352 [DataCollector] Improves the readability of the collected arrays in the profiler (fabpot)

    This PR was merged into the 2.5-dev branch.

    Discussion
    ----------

    [DataCollector] Improves the readability of the collected arrays in the profiler

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | License       | MIT

    This PR is based on #10155.

    Original description:

    It simply improves the readability of the collected arrays in the profiler:

    __before__:
    ```
    Array(date => Array(year => , month => , day => ), time => Array(hour => ))
    ```

    __after__:
    ```
    [
      date => [
          year => ,
          month => ,
          day =>
      ],
      time => [
          hour =>
      ]
    ]
    ```

    Commits
    -------

    dce66c9 removed double-stringification of values in the profiler
    1cda2d4 [HttpKernel] tweaked value exporter
    3f297ea Improves the readability of the collected arrays in the profiler.