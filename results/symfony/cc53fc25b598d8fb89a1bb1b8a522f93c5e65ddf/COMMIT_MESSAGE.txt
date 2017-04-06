commit cc53fc25b598d8fb89a1bb1b8a522f93c5e65ddf
Merge: 150a138 20dbe47
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Dec 2 15:14:10 2012 +0100

    merged branch Tobion/routing-loaders (PR #6165)

    This PR was merged into the master branch.

    Commits
    -------

    20dbe47 added annotation
    c73cb8a add default for pattern for clarity
    ddd8918 make id attribute required
    62536e5 refactor to an xsd:group
    451dcdc it should be possible to define the defaults, req. and options in any order, just like in YAML

    Discussion
    ----------

    improve routing xml scheme

    bc break: no

    Main points:
    - the xml scheme only allowed defaults, requirements and options in this specific order. but the XmlFileLoader does not have the restriction and the YAML definions does not have such an restriction either. this is now fixed. so you can use
    ```
    <requirement key="_locale">en</requirement>
    <default key="_controller">Foo</default>
    ```
    Before it had the be first all defaults, then all requirements, then all options.
    - make id attribute required

    For more changes see commits.