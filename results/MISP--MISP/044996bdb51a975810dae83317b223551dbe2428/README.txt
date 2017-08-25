commit 044996bdb51a975810dae83317b223551dbe2428
Author: Iglocska <andras.iklody@gmail.com>
Date:   Fri Oct 16 23:49:04 2015 +0200

    New feature: Proposal to delete attribute, fixes #315

    - Users can now propose a deletion to an attribute
      - also tied into the mass accept mechanism
      - new UI elements to go along with this

    - Code refactoring for category list retrievals
      - Until now, several methods got the list of categories from the validation code
      - Was awkward with a fake empty element that had to be removed
      - altered the validation code to read the categoryDefinitions array instead