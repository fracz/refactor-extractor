commit 530dc412c4d4700268e52920e6114f7941171706
Author: Misko Hevery <misko@hevery.com>
Date:   Fri Aug 12 12:03:27 2011 -0700

    refactor(scope): use double-linked-list for children

    Much faster $destroy operations for large ng:repeat sets.