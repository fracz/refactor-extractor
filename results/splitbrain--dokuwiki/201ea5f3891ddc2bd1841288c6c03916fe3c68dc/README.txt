commit 201ea5f3891ddc2bd1841288c6c03916fe3c68dc
Author: Andreas Gohr <andi@splitbrain.org>
Date:   Tue Aug 4 11:57:07 2009 +0200

    improved list handling

    Ignore-this: 2e4f3fbfb28917ee66cf3e1925c806d3

    This patch adds multiple enhancements to handling lists and indented
    code blocks in the editor.

    1. Pressing enter when in a list item or code block will keep the indention
       and adds a new list point
    2. Pressing space at the start of a list item will indent the item to the
       next level
    3. Pressing bckspace at the start of a list item will outdent the item
       to the previous level or delete the list bullet when you are at the
       1st level already
    4. A new type of formatting button called formatln is added. It applies
       formatting to several lines. It's used for the list buttons currently
       and makes it possible to convert mutiple lines to a list

    This enhncement are currently only tested in Firefox are most likely to
    break IE compatibility. A compatibility patch will be submitted later

    note: development was part of the ICKE 2.0 project see
          http://www.icke-projekt.de for info

    darcs-hash:20090804095707-7ad00-e565c66087c7121188ad7ece8265d9f64f7e6947.gz