commit 2dea0e347de54b0311cac7174e8981a33c9ff329
Author: noud <noud4@home.nl>
Date:   Fri Aug 3 12:00:16 2012 +0200

    Correlation performance gain.

    in Config/bootstrap.php add
    Configure::write('CyDefSIG.correlation', 'sql');

    possible values:
    - default, like it was
    - db, correlation in database
    - sql, selection on attributes i.s.o. per attribute
      (sql improvement possible if result conform db above)

    Network activity, ip-src
    30 class-C network ip addresses
    (7650 tupels) (time in ms)

              default     db    sql
    all         25366  16601  15941
                24839  16604  15611
    paginated   16759   8447   6615
                17734   8639   8846

    this is used in both:
    - events/view/<id>
    - attributes/event/<id>