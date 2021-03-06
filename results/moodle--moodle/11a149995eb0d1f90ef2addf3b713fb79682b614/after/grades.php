<?PHP // $Id$
      // grades.php - created with Moodle 1.7 beta + (2006101003)


$string['activities'] = 'Activities';
$string['addcategory'] = 'Add Category';
$string['addcategoryerror'] = 'Could not add category.';
$string['addexceptionerror'] = 'Error occurred while adding exception for userid:gradeitem';
$string['addfeedback'] = 'Add Feedback';
$string['additem'] = 'Add Grade Item';
$string['addoutcomeitem'] = 'Add Outcome Item';
$string['aggregateextracreditmeanall'] = 'Mean of all grades (extra credits)';
$string['aggregateextracreditmeangraded'] = 'Mean of non-empty grades (extra credits)';
$string['aggregatemeanall'] = 'Mean of all grades';
$string['aggregatemeangraded'] = 'Mean of non-empty grades';
$string['aggregatemedianall'] = 'Median of all grades';
$string['aggregatemediangraded'] = 'Median of non-empty grades';
$string['aggregateminall'] = 'Smallest grade of all grades';
$string['aggregatemingraded'] = 'Smallest grade of non-empty grades';
$string['aggregatemaxall'] = 'Highest grade of all grades';
$string['aggregatemaxgraded'] = 'Highest grade of non-empty grades';
$string['aggregatemodeall'] = 'Mode of non-empty grades of all grades';
$string['aggregatemodegraded'] = 'Mode of non-empty grades';
$string['aggregateweightedmeanall'] = 'Weighted mean of all grades';
$string['aggregateweightedmeangraded'] = 'Weighted mean of non-empty grades';
$string['aggregation'] = 'Aggregation';
$string['aggregationposition'] = 'Aggregation position';
$string['aggregationview'] = 'Aggregation view';
$string['allgrades'] = 'All grades by category';
$string['allstudents'] = 'All Students';
$string['autosort'] = 'Auto-sort';
$string['average'] = 'Average';
$string['averagesdecimalpoints'] = 'Decimals in column averages';
$string['averagesdisplaytype'] = 'Column averages display type';
$string['badgrade'] = 'Supplied grade is invalid';
$string['baduser'] = 'Supplied user is invalid';
$string['bonuspoints'] = 'Bonus Points';
$string['bulkcheckboxes'] = 'Bulk checkboxes';
$string['calculation'] = 'Calculation';
$string['calculationadd'] = 'Add calculation';
$string['calculationedit'] = 'Edit calculation';
$string['calculationview'] = 'View calculation';
$string['calculationsaved'] = 'Calculation saved';
$string['categories'] = 'Categories';
$string['categoriesanditems'] = 'Categories and Items';
$string['categoriesedit'] = 'Edit Categories and Items';
$string['category'] = 'Category';
$string['categoryedit'] = 'Edit Category';
$string['categoryname'] = 'Category name';
$string['changesitedefaults'] = 'Change site defaults';
$string['choosecategory'] = 'Select Category';
$string['compact'] = 'Compact';
$string['configaggregationposition'] = 'The position of the aggregation column in the grader report table, in reference to the real grades.';
$string['configaggregationview'] = 'The way aggregations are displayed: either alongside the real grades, or in a compact form in which only one type is shown in the table at once: the real grades OR the aggregated grades.';
$string['configaveragesdecimalpoints'] = 'The number of decimal points to display for each average (group or whole), below a column of grades. This can be overriden per grading item.';
$string['configaveragesdisplaytype'] = 'Specifies how to display the averages for each column in the grader report. The default and recommended value is Inherit, which first checks the display type of each column, and if such is not set explicitly, defaults to the more general Grade Display Type. If other display types are selected, they ignore the individual settings for each column, and use exclusively the selected type.';
$string['configbulkcheckboxes'] = 'Checkboxes near each grade for Bulk grade operations.';
$string['configdecimalpoints'] = 'The number of decimal points to display for each grade. This can be overriden per grading item.';
$string['configenableajax'] = 'Adds a layer of AJAX functionality to the grader report, simplifying and speeding up common operations. Depends on Javascript being switched on at the user\'s browser level.';
$string['configenableoutcomes'] = 'Support for Outcomes (also known as Competencies, Goals, Standards or Criteria) means that we can grade things using one or more scales that are tied to outcome statements. Enabling outcomes makes such special grading possible throughout the site.';
$string['configgradeboundary'] = 'A percentage boundary over which grades will be assigned a grade letter (if the Letter grade display type is used). ';
$string['configgradedisplaytype'] = 'Grades can be shown as real grades, as percentages (in reference to the minimum and maximum grades) or as letters (A, B, C etc..)';
$string['configgradeletter'] = 'A letter or other symbol used to represent a range of grades.';
$string['configgradeletterdefault'] = 'A letter or other symbol used to represent a range of grades. Leave this field empty to use the site default (currently $a).';
$string['configmeanselection'] = 'Select which types of grades will be included in the column averages. Cells with no grade can be ignored, or counted as 0 (default setting).';
$string['configquickfeedback'] = 'Quick Feedback adds a text input element in each grade cell on the grader report, allowing you to edit many grades at once. You can then click the Update button to perform all these changes at once, instead of one at a time.';
$string['configquickgrading'] = 'Quick Grading adds a text input element in each grade cell on the grader report, allowing you to edit the feedback for many grades at once. You can then click the Update button to perform all these changes at once, instead of one at a time.';
$string['configrangesdecimalpoints'] = 'The number of decimal points to display for each range, above a column of grades. This can be overriden per grading item.';
$string['configrangesdisplaytype'] = 'Specifies how to display the range for each column in the grader report. The default and recommended value is Inherit, which first checks the display type of each column, and if such is not set explicitly, defaults to the more general Grade Display Type. If other display types are selected, they ignore the individual settings for each column, and use exclusively the selected type.';
$string['configshowcalculations'] = 'Whether to show calculator icons near each grade item and category, tooltips over calculated items and a visual indicator that a column is calculated.';
$string['configshoweyecons'] = 'Whether to show a show/hide icon near each grade (controlling its visibility to the user).';
$string['configshowactivityicons'] = 'Show an activity icon next to each grade item linked to an activity, in the grader report.';
$string['configshowaverages'] = 'Show column averages in the grader report.';
$string['configshowgroups'] = 'Show group averages and means in the grader report.';
$string['configshowlocks'] = 'Whether to show a lock/unlock icon near each grade.';
$string['configshowfeedback'] = 'Whether to show a feedback icon (for adding/editing) near each grade.';
$string['configshowranges'] = 'Display a row showing the range of possible for each grading item in the grader report.';
$string['configshowuserimage'] = 'Whether to show the user\'s profile image next to the name in the grader report.';
$string['configstudentsperpage'] = 'The number of students to display per page in the grader report.';
$string['configstudentsperpagedefault'] = 'The number of students to display per page in the grader report. Leave this field empty to use the site default (currently $a).';
$string['contract'] = 'Contract Category';
$string['createcategory'] = 'Create Category';
$string['createcategoryerror'] = 'Could not create a new category';
$string['creatinggradebooksettings'] = 'Creating Gradebook settings';
$string['csv'] = 'CSV';
$string['curveto'] = 'Curve To';
$string['decimalpoints'] = 'Overall decimal points';
$string['default'] = 'Default';
$string['deletecategory'] = 'Delete Category';
$string['displaylettergrade'] = 'Display Letter Grades';
$string['displaypercent'] = 'Display Percents';
$string['displaypoints'] = 'Display Points';
$string['displayweighted'] = 'Display Weighted Grades';
$string['droplow'] = 'Drop the lowest';
$string['dropped'] = 'Dropped';
$string['dropxlowest'] = 'Drop X Lowest';
$string['dropxlowestwarning'] = 'Note: If you use drop x lowest the grading assumes that all items in the category have the same point value. If point values differ results will be unpredictable';
$string['duplicatescale'] = 'Duplicate scale';
$string['edit'] = 'Edit';
$string['editcalculation'] = 'Edit Calculation';
$string['editfeedback'] = 'Edit Feedback';
$string['editgrade'] = 'Edit Grade';
$string['editoutcomes'] = 'Edit outcomes';
$string['edittree'] = 'Categories and items';
$string['enableajax'] = 'Enable AJAX';
$string['enableoutcomes'] = 'Enable outcomes';
$string['encoding'] = 'Encoding';
$string['errorgradevaluenonnumeric'] = 'Received non-numeric for low or high grade for';
$string['errorcalculationnoequal'] = 'Formula must start with equal sign (=1+2)';
$string['errorcalculationunknown'] = 'Invalid formula';
$string['errornocategorizedid'] = 'Could not get an uncategorized id!';
$string['errornocourse'] = 'Could not get course information';
$string['errorreprintheadersnonnumeric'] = 'Received non-numeric value for reprint-headers';
$string['exceptions'] = 'Exceptions';
$string['excluded'] = 'Excluded';
$string['expand'] = 'Expand Category';
$string['export'] = 'Export';
$string['exportplugins'] = 'Export plugins';
$string['extracredit'] = 'Extra Credit';
$string['extracreditwarning'] = 'Note: Setting all items for a category to extra credit will effectively remove them from the grade calculation. Since there will be no point total';
$string['feedback'] = 'Feedback';
$string['feedbackadd'] = 'Add feedback';
$string['feedbackedit'] = 'Edit feedback';
$string['feedbackview'] = 'View feedback';
$string['feedbacksaved'] = 'Feedback saved';
$string['finalgrade'] = 'Final grade';
$string['forelementtypes'] = ' for the selected $a';
$string['forstudents'] = 'For Students';
$string['full'] = 'Full';
$string['grade'] = 'Grade';
$string['gradebook'] = 'Gradebook';
$string['gradebookhiddenerror'] = 'The gradebook is currently set to hide everything from students.';
$string['gradeboundary'] = 'Letter grade boundary';
$string['gradecategory'] = 'Grade Category';
$string['gradecategoryhelp'] = 'Grade Category Help';
$string['gradedisplaytype'] = 'Grade display type';
$string['gradeexceptions'] = 'Grade Exceptions';
$string['gradeexceptionshelp'] = 'Grade Exceptions Help';
$string['gradehelp'] = 'Grade Help';
$string['gradeitem'] = 'Grade item';
$string['gradeitemlocked'] = 'Grading locked';
$string['gradeitemsinc'] = 'Grade items to be included';
$string['gradeitemaddusers'] = 'Exclude from Grading';
$string['gradeitemmembersselected'] = 'Excluded from Grading';
$string['gradeitemnonmembers'] = 'Included in Grading';
$string['gradeitemremovemembers'] = 'Include in Grading';
$string['gradeitems'] = 'Grade Items';
$string['gradeletter'] = 'Grade Letter';
$string['gradeletterhelp'] = 'Grade Letter Help';
$string['gradeletternote'] = 'To delete a grade letter just empty any of the<br /> three text areas for that letter and click submit.';
$string['grademax'] = 'Maximum grade';
$string['grademin'] = 'Minimum grade';
$string['gradeoutcomeitem'] = 'Grade outcome item';
$string['gradepass'] = 'Grade to pass';
$string['graderreport'] = 'Grader report';
$string['gradessettings'] = 'Grade settings';
$string['groupavg'] = 'Group average';
$string['hidden'] = 'Hidden';
$string['importplugins'] = 'Import plugins';
$string['itemsedit'] = 'Edit grade item';
$string['gradepreferences'] = 'Grade Preferences';
$string['gradepreferenceshelp'] = 'Grade Preferences Help';
$string['grades'] = 'Grades';
$string['gradetype'] = 'Grade type';
$string['gradeview'] = 'View Grade';
$string['gradeweighthelp'] = 'Grade Weight Help';
$string['hideadvanced'] = 'Hide Advanced Features';
$string['hidecalculations'] = 'Hide calculations';
$string['hidecategory'] = 'Hidden';
$string['hideeyecons'] = 'Hide show/hide icons';
$string['hideaverages'] = 'Hide averages';
$string['hidegroups'] = 'Hide groups';
$string['hidelocks'] = 'Hide locks';
$string['hidefeedback'] = 'Hide feedback';
$string['hideranges'] = 'Hide ranges';
$string['highgradeascending'] = 'Sort by high grade ascending';
$string['highgradedescending'] = 'Sort by high grade descending';
$string['highgradeletter'] = 'High';
$string['identifier'] = 'Identify user by';
$string['import'] = 'Import';
$string['importcsv'] = 'Import CSV';
$string['importfailed'] = 'Import failed';
$string['importfile'] = 'Import file';
$string['importpreview'] = 'Import preview';
$string['importsuccess'] = 'Grade import success';
$string['importxml'] = 'Import XML';
$string['incorrectcourseid'] = 'Course ID was incorrect';
$string['inherit'] = 'Inherit';
$string['item'] = 'Item';
$string['iteminfo'] = 'Item info';
$string['itemname'] = 'Item name';
$string['items'] = 'Items';
$string['keephigh'] = 'Keep the highest';
$string['left'] = 'Left';
$string['lettergrade'] = 'Letter Grade';
$string['lettergradenonnumber'] = 'Low and/or High grade were non-numeric for';
$string['letter'] = 'Letter';
$string['letters'] = 'Letters';
$string['linkedactivity'] = 'Linked activity';
$string['lock'] = 'Lock';
$string['locked'] = 'Locked';
$string['locktime'] = 'Locked until';
$string['lowest'] = 'Lowest';
$string['lowgradeletter'] = 'Low';
$string['mapfrom'] = 'Map from';
$string['mapto'] = 'Map to';
$string['max'] = 'Highest';
$string['maxgrade'] = 'Max Grade';
$string['mappings'] = 'Grade item mappings';
$string['meanall'] = 'All grades';
$string['meangraded'] = 'Non-empty grades';
$string['meanselection'] = 'Grades selected for averages';
$string['median'] = 'Median';
$string['min'] = 'Lowest';
$string['mode'] = 'Mode';
$string['movingelement'] = 'Moving $a';
$string['multfactor'] = 'Multiplicator';
$string['newcategory'] = 'New category';
$string['no'] = 'No';
$string['nocategories'] = 'Grade categories could not be added or found for this course';
$string['nocategoryname'] = 'No category name was given.';
$string['nocategoryview'] = 'No category to view by';
$string['nogradeletters'] = 'No grade letters set';
$string['nogradesreturned'] = 'No grades returned';
$string['nolettergrade'] = 'No letter grade for';
$string['nomode'] = 'NA';
$string['nonnumericweight'] = 'Received non-numeric value for';
$string['nonweightedpct'] = 'non-weighted %%';
$string['noselectedcategories'] = 'no categories were selected.';
$string['noselecteditems'] = 'no items were selected.';
$string['notteachererror'] = 'You must be a teacher to use this feature.';
$string['numberofgrades'] = 'Number of grades';
$string['onascaleof'] = ' on a scale of $a->grademin to $a->grademax';
$string['operations'] = 'Operations';
$string['outcome'] = 'Outcome';
$string['outcomecreate'] = 'Add a new outcome';
$string['outcomeitem'] = 'Outcome item';
$string['outcomeitemsedit'] = 'Edit outcome item';
$string['outcomes'] = 'Outcomes';
$string['outcomescustom'] = 'Custom outcomes';
$string['outcomescourse'] = 'Outcomes used in course';
$string['outcomescoursecustom'] = 'Custom used (no remove)';
$string['outcomescoursenotused'] = 'Standard not used';
$string['outcomescourseused'] = 'Standard used (no remove)';
$string['outcomescourse'] = 'Outcomes used in course';
$string['outcomename'] = 'Outcome name';
$string['outcomereport'] = 'Outcome report';
$string['outcomesstandard'] = 'Standard outcomes';
$string['outcomesstandardavailable'] = 'Available standard outcomes';
$string['outcomestandard'] = 'Standard outcome';
$string['outcomes'] = 'Outcomes';
$string['overridden'] = 'Overridden';
$string['overallavg'] = 'Overall average';
$string['pctoftotalgrade'] = '%% of total grade';
$string['percent'] = 'Percent';
$string['percentage'] = 'Percentage';
$string['percentascending'] = 'Sort by percent ascending';
$string['percentdescending'] = 'Sort by percent descending';
$string['percentshort'] = '%%';
$string['plusfactor'] = 'Offset';
$string['points'] = 'points';
$string['pointsascending'] = 'Sort by points ascending';
$string['pointsdescending'] = 'Sort by points descdending';
$string['preferences'] = 'Preferences';
$string['prefgeneral'] = 'General';
$string['prefletters'] = 'Grade letters and boundaries';
$string['prefrows'] = 'Special rows';
$string['prefshow'] = 'Show/hide toggles';
$string['quickfeedback'] = 'Quick Feedback';
$string['quickgrading'] = 'Quick Grading';
$string['range'] = 'Range';
$string['rangesdecimalpoints'] = 'Decimals shown in ranges';
$string['rangesdisplaytype'] = 'Range display type';
$string['rank'] = 'Rank';
$string['rawpct'] = 'Raw %%';
$string['real'] = 'Real';
$string['report'] = 'Report';
$string['reportplugins'] = 'Report plugins';
$string['reportsettings'] = 'Report settings';
$string['reprintheaders'] = 'Reprint Headers';
$string['right'] = 'Right';
$string['savechanges'] = 'Save Changes';
$string['savepreferences'] = 'Save Preferences';
$string['scaledpct'] = 'Scaled %%';
$string['selectdestination'] = 'Select destination of $a';
$string['septab'] = 'Tab';
$string['sepcomma'] = 'Comma';
$string['separator'] = 'Separator';
$string['setcategories'] = 'Set Categories';
$string['setcategorieserror'] = 'You must first set the categories for your course before you can give weights to them.';
$string['setgradeletters'] = 'Set Grade Letters';
$string['setpreferences'] = 'Set Preferences';
$string['setting'] = 'Setting';
$string['settings'] = 'Settings';
$string['setweights'] = 'Set Weights';
$string['showallstudents'] = 'Show All Students';
$string['showactivityicons'] = 'Show activity icons';
$string['showaverages'] = 'Show column averages';
$string['showcalculations'] = 'Show calculations';
$string['showeyecons'] = 'Show show/hide icons';
$string['showfeedback'] = 'Show feedback';
$string['showgroups'] = 'Show groups';
$string['showhiddenitems'] = 'Show Hidden Items';
$string['showlocks'] = 'Show locks';
$string['showranges'] = 'Show ranges';
$string['showuserimage'] = 'Show user profile images';
$string['sitedefault'] = 'Site default ($a)';
$string['sitewide'] = 'Site-wide';
$string['sort'] = 'sort';
$string['sortasc'] = 'Sort in ascending order';
$string['sortdesc'] = 'Sort in descending order';
$string['sortbyfirstname'] = 'Sort by Firstname';
$string['sortbylastname'] = 'Sort by Lastname';
$string['standarddeviation'] = 'Standard Deviation';
$string['stats'] = 'Statistics';
$string['statslink'] = 'Stats';
$string['student'] = 'Student';
$string['studentsperpage'] = 'Students per page';
$string['subcategory'] = 'Normal Category';
$string['synclegacygrades'] = 'Synchronise legacy grades';
$string['topcategory'] = 'Super Category';
$string['total'] = 'Total';
$string['totalweight100'] = 'The total weight is equal to 100';
$string['totalweightnot100'] = 'The total weight is not equal to 100';
$string['turnfeedbackoff'] = 'Turn feedback off';
$string['turnfeedbackon'] = 'Turn feedback on';
$string['typenone'] = 'None';
$string['typescale'] = 'Scale';
$string['typetext'] = 'Text';
$string['typevalue'] = 'Value';
$string['uncategorised'] = 'Uncategorised';
$string['unlock'] = 'Unlock';
$string['unused'] = 'Unused';
$string['uploadgrades'] = 'Upload grades';
$string['useadvanced'] = 'Use Advanced Features';
$string['usedcourses'] = 'Used courses';
$string['usedgradeitem'] = 'Used grade item';
$string['usenooutcome'] = 'Use no outcome';
$string['usenoscale'] = 'Use no scale';
$string['usepercent'] = 'Use Percent';
$string['user'] = 'User';
$string['userpreferences'] = 'User preferences';
$string['useweighted'] = 'Use Weighted';
$string['viewbygroup'] = 'Group';
$string['viewgrades'] = 'View Grades';
$string['weight'] = 'weight';
$string['weightedascending'] = 'Sort by weighted percent ascending';
$string['weighteddescending'] = 'Sort by weighted percent descending';
$string['weightedpct'] = 'weighted %%';
$string['weightedpctcontribution'] = 'weighted %% contribution';
$string['writinggradebookinfo'] = 'Writing Gradebook settings';
$string['xml'] = 'XML';
$string['yes'] = 'Yes';
$string['yourgrade'] = 'Your grade';
?>