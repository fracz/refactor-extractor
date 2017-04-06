commit bda2bba2be7a52bf39fb1b257b6363edc7b71173
Author: Misko Hevery <misko@hevery.com>
Date:   Tue Jul 26 12:06:14 2011 -0700

    feat(jqlite): added .inheritedData method and $destroy event.

    - refactored .scope() to use .inheritedData() instead.
    - .bind('$destroy', callback) will call when the DOM element is removed