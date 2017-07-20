commit a63274289c896c3a851ec4045c172468a6b5aa06
Author: epriestley <git@epriestley.com>
Date:   Thu Jun 2 17:13:25 2011 -0700

    Link macro thumbs and show an exact page count

    Summary:
    Some of the improvements from T175: link macro thumbnails to the full image, and
    pull an exact count out of the database.

    Test Plan:
    Clicked a thumb, looked at pager.

    Reviewed By: tuomaspelkonen
    Reviewers: tuomaspelkonen, tomo
    CC: aran, tuomaspelkonen
    Differential Revision: 397