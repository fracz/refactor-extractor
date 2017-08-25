commit df997f841f93d676d782a46cfb8547ebc7319102
Author: Petr Skoda <skodak@moodle.org>
Date:   Mon Jun 21 15:30:49 2010 +0000

    MDL-21782 reworked enrolment framework, the core infrastructure is in place, the basic plugins are all implemented; see the tracker issue for list of unfinished bits, expect more changes and improvements during the next week

    AMOS START
        MOV [sendcoursewelcomemessage,core_admin],[sendcoursewelcomemessage,enrol_self]
        MOV [configsendcoursewelcomemessage,core_admin],[sendcoursewelcomemessage_desc,enrol_self]
        MOV [enrolstartdate,core],[enrolstartdate,enrol_self]
        MOV [enrolenddate,core],[enrolenddate,enrol_self]
        CPY [welcometocourse,core],[welcometocourse,enrol_self]
        CPY [welcometocoursetext,core],[welcometocoursetext,enrol_self]
        MOV [notenrollable,core],[notenrollable,core_enrol]
        MOV [enrolenddaterror,core],[enrolenddaterror,enrol_self]
        MOV [enrolmentkeyhint,core],[passwordinvalidhint,enrol_self]
        MOV [coursemanager,core_admin],[coursecontact,core_admin]
        MOV [configcoursemanager,core_admin],[coursecontact_desc,core_admin]
        MOV [enrolledincourserole,core],[enrolledincourserole,enrol_manual]
        MOV [enrolme,core],[enrolme,core_enrol]
        MOV [unenrol,core],[unenrol,core_enrol]
        MOV [unenrolme,core],[unenrolme,core_enrol]
        MOV [enrolmentnew,core],[enrolmentnew,core_enrol]
        MOV [enrolmentnewuser,core],[enrolmentnewuser,core_enrol]
        MOV [enrolments,core],[enrolments,core_enrol]
        MOV [enrolperiod,core],[enrolperiod,core_enrol]
        MOV [unenrolroleusers,core],[unenrolroleusers,core_enrol]
    AMOS END