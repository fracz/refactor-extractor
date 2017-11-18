commit 115ba578c764b96e1ee36052fb13de19fa5dca49
Author: Lisa Luo <lluo92@gmail.com>
Date:   Tue Sep 29 17:18:18 2015 -0400

    ProjectAdapter refactor for proper data loading

    Previous implementation introduced adapter position bug, where position was reset to 0 due to adapter being reset each time. With this refactor, data is updated and the adapter is notified without being reset.