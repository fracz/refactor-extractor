commit abc5f84539cbd228978b6579b1a7898f98798643
Author: Dan Poltawski <dan@moodle.com>
Date:   Thu Jul 3 14:30:36 2014 +0100

    MDL-44605 calendar: improved returnurl validation

    Now we will only redirect to a local url (though sesskey was
    already preventing this redirect from being open)