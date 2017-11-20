commit e086daf5646e20ad5a7c55cdca4801e6d6037bce
Author: Marques Lee <marques.lee@thoughtworks.com>
Date:   Fri Feb 24 00:51:12 2017 -0800

    Further progress on new console log user interaction

      - parse and color executed commands, stdout/err, material updates, environment variables, task and job results, etc
      - add icons in gutter to help quickly identify task status
      - improve section boundary detection
      - preparation section (env variables, material updates) distinguish error lines in output
      - timestamps are hidden by default, but can be toggled in the console action bar
      - styling!