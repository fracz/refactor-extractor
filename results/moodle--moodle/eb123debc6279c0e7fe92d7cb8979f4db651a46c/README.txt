commit eb123debc6279c0e7fe92d7cb8979f4db651a46c
Author: skodak <skodak>
Date:   Thu Oct 8 22:34:34 2009 +0000

    MDL-19474 One year and a half ago we started designing and implementing our new databse abstraction layer which included brand new moodle native drivers. Today we are finally removing old legacy drivers that were still using adodb internally. From now on the adodb will be used only in some authentication and enrolment plugins, we are probably going to drop adodb there too later in 2.x. Thanks everybody who helped test and improve our new drivers, even bigger thanks to all the great developers who created ADOdb!