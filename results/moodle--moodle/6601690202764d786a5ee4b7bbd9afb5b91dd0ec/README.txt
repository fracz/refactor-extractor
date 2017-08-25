commit 6601690202764d786a5ee4b7bbd9afb5b91dd0ec
Author: Gregory Zbitnev <zbitnev@hotmail.com>
Date:   Wed Jan 28 17:01:24 2015 +0400

    MDL-18309 Course: enrol/index.php returnurl improvement

    This patch improves usability of enrolment page in case of course is not
    enrollable. 'Continue' button now returns student to referring page instead of
    main moodle page (as it used to be). To make this improvement, passing correct
    returnurl parameter to enrol/index.php page was implemented for links that may
    be accessible for not-enrolled students.