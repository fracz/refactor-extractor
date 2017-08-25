commit 70bbfd318693e9f0822d50aac7f4247026682b31
Author: stronk7 <stronk7>
Date:   Fri Sep 24 19:20:32 2004 +0000

    Scheduled backup improvements:

    - Old (>4 hour) directories are deleted at the beginning of the
      execution, so the disk full problem should be out!
    - There are 2 types of errors:
        - Controlled errors: The backup process finish although something
            has been wrong.
        - Uncontrolled errors: The backup process dies (memory fault, apache
            problem, electric problem...:-)).
    - Now scheduled backup is able to detect previous uncontrolled errors to
      avoid repeating the same course always. It'll be deferred to the next
      execution.
    - Every time that a backup cycle has finished, admins will receive an email
      with the summary of the execution.
    - The 'ERROR' text will be present in email subjects if something was wrong.

    With this, all should be ok (I hope).

    Merged from MOODLE_14_STABLE