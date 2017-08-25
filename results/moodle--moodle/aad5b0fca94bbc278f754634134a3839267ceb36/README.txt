commit aad5b0fca94bbc278f754634134a3839267ceb36
Author: jamiesensei <jamiesensei>
Date:   Thu May 15 12:27:27 2008 +0000

    MDL-14200 "Add group and course averages" added extra rows to the end of the table in quiz overview report also MDL-14187 "Improve tablelib - improve api and add functionality to download table contents in a variety of formats - XLS, ODS and CSV" implemented the first of my proposals for improving tablelib. Added a extra method to use keyed arrays to add a row to a table. Seems obvious that this is an improvement to tablelib and it is an addition to the API so won't affect existing code.

    Merged from 1.9 branch.