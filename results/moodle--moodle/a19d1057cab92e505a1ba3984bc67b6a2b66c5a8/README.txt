commit a19d1057cab92e505a1ba3984bc67b6a2b66c5a8
Author: Marina Glancy <marina@moodle.com>
Date:   Fri Nov 18 15:35:24 2011 +0800

    MDL-30270, MDL-30269: rubric interface/usability improvements:

    - In rubric editor the line 'Current rubric status' is hidden if there is no status yet
    - If present the style of the status is the same as on manage.php page
    - For newly created rubric 'Add criterion' button is pre-pressed automatically
    - Changed JavaScript to work for Mac browsers default settings and for IPad
    - MDL-30269: added explanation message about score to grade mapping
    - fixed bug with non-javascript 'Add criterion' behaviour