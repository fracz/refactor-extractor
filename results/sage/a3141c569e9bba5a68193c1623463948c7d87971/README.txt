commit a3141c569e9bba5a68193c1623463948c7d87971
Author: QWp6t <hi@qwp6t.me>
Date:   Sun Dec 18 15:50:08 2016 -0800

    Refactor Blade implementation (again), closes #1769 (#1777)

    * Squash bugs, reorganize, etc.
    * Use `get_body_class()` to apply filters on template data
    * Use `PHP_INT_MAX` as priority for `template_include` filter