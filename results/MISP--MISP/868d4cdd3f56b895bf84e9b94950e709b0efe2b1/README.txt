commit 868d4cdd3f56b895bf84e9b94950e709b0efe2b1
Author: Iglocska <andras.iklody@gmail.com>
Date:   Sun Dec 20 13:41:52 2015 +0100

    First version of the sightings

    - add / delete sightings via REST
    - add sightings via the UI
    - View sightings info on an event and attribute level (event view only for now)
    - differentiate between own sightings and that of other orgs (additional information via popover still coming)

    - settings:
      - 1. enable / disable sightings server wide
      - 2. set sightings policy
        - a. Only Event owner can see sightings + everyone sees what they themeselves contribute
        - b. Anyone that contributes sightings to an event can see the sightings data
        - c. Everyone that can see the event can see the sightings
      - 3. Anonymisisation (in progress, data correctly retrieved in business logic)
        - a. if true, then only own org + "other" is shown
        - b. otherwise all orgs that submitted sightings are shown

    Further improvements needed for version 1 of sightings:
      - 1. Delete via the interface
      - 2. View detailed sightings information
      - 3. Graph the sightings data for the event
      - 4. Include the Sightings data in the XML/JSON views
      - 5. View sighting for attribute / event via the API