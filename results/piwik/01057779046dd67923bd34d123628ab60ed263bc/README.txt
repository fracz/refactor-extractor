commit 01057779046dd67923bd34d123628ab60ed263bc
Author: Benaka Moorthi <benaka.moorthi@gmail.com>
Date:   Fri Jul 12 02:31:39 2013 -0400

    Refs #4040, #4041, move all ViewDataTable properties to the viewProperties array and allow these properties to be specified through new display metadata. Converted the Actions, Goals, UserSettings and VisitTime controllers.

    Notes:
      - Includes refactoring of ExcludeLowPopulation filter.