commit dea73ebecf0c3ce2c36145267718484a46ebb18a
Author: Alexey Andreev <Alexey.Andreev@jetbrains.com>
Date:   Tue Jun 20 15:34:36 2017 +0300

    Refactor generator of JS source map

    - refactor pipeline for generation of source map
    - generate "empty" mappings for nodes that impossible
      to map to something reasonable
    - generate more accurate locations in source maps for specific
      JS AST nodes
    - for binary operation nodes parser now returns location
      of binary operator tokens instead of location of first operand
    - change completely how source map remapper works