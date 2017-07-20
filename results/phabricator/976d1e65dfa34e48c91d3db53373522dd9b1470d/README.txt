commit 976d1e65dfa34e48c91d3db53373522dd9b1470d
Author: tuomaspelkonen <tuomas.pelkonen@fb.com>
Date:   Tue Apr 19 20:03:55 2011 -0700

    Fixed image macro with '-' in the name.

    Summary:
    Fixed the image macro regex not to use '-' as the separator.
    Also minor improvement to randomon.

    Test Plan:
    Tried different image marcors.

    Reviewed By: jungejason
    Reviewers: jungejason
    CC: epriestley, jungejason
    Differential Revision: 153