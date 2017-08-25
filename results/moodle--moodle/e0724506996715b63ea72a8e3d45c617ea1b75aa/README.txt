commit e0724506996715b63ea72a8e3d45c617ea1b75aa
Author: skodak <skodak>
Date:   Mon Oct 8 23:09:10 2007 +0000

    MDL-11578 ,  MDL-11578 , MDL-11573 :
    * renamed Preferences to "My report preferences"
    * renamed Site defaults to "Report defaults"
    * rename Change site defaults to "Change report defaults"
    * reintroduced Inherit option to Grader preferences - Nicolas was right, it is not possible to implement preference overrides without it with our current get_pref inplementation
    * new separate option in plugin selector "Course settings"
    * moved displaytype, decimals and aggregation position to "Course settings"
    * created new table grade_settings + related functions grade_get/set_preference()
    * user report now uses grade_seq class instead of grade_item::fetch_all(); added preloading of grade items into grade_grade instances
    * other minor bugfixing/cleanup/improvements

    Please note that the Grader report preferences and its defaults must be resaved again - sorry.