commit 4c022beafc3c643f54ad5ff704a9c00c2b996c14
Author: Liviu Valsan <liviu.valsan@cern.ch>
Date:   Wed Dec 7 16:33:17 2016 +0100

    - Performance improvements when exporting a large number of attributes into Bro format.
    - Fixed file header formatting for the export to Bro format (tabs used consistently).
    - Computing the time needed for generating the export to Bro format when done using a background job.
    - When generating the Bro export from the UI all the attributes are generated in one single text file similar to the CSV export instead of a zip file with different files inside.
    - Changed the file extension of Bro export files from ".intel" to ".txt".
    - Removed the allowNonIDS option from the Bro export as it doesn’t make sense to have it (Bro is an IDS).
    - Fixed some of the API endpoints which were not accepted (ACL issues).
    - Added support for a list of events that should be / should not be included in the export.
    - Added a new "meta.desc" column (added in Bro 2.5, see https://www.bro.org/sphinx/frameworks/intel.html) containing the description of the event and of the attribute.
    - Sanitized the exported data for Bro.
    - Fixed a number of value substitutions which were imported from Snort/Suricata and which were not working for Bro. Did instead substitutions needed for Bro.