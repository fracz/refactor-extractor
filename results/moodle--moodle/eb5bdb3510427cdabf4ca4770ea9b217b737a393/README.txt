commit eb5bdb3510427cdabf4ca4770ea9b217b737a393
Author: Petr Skoda <skodak@moodle.org>
Date:   Mon Aug 16 19:11:21 2010 +0000

    MDL-23797 improved detection of PAGE->context abuse andproblems; fixed incorrect use of this->context instead of this->_context; missing PAGE->context does not throw fatal error any more