commit 7ebd0d1efe471b94cdff8d73d6ffd02ffd178254
Author: epriestley <git@epriestley.com>
Date:   Fri May 6 12:58:53 2011 -0700

    Provide a web interface to view raw source text in Differential

    Summary:
    Add links to the 'standalone view' to grab the raw source text. I think this
    operation is rare enough that it's okay to hide it like this. I changed
    'Standalone View' to 'View Standalone / Raw' to improve discoverability.

    This also fixes the broken Standalone View links in Diffusion by no longer
    rendering them.

    Test Plan:
    viewed old and new sources for a changeset

    Reviewed By: tuomaspelkonen
    Reviewers: tuomaspelkonen, jungejason, aran
    CC: aran, tuomaspelkonen
    Differential Revision: 243