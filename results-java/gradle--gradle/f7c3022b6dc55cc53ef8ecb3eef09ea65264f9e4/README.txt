commit f7c3022b6dc55cc53ef8ecb3eef09ea65264f9e4
Author: Adam Murdoch <adam@gradle.com>
Date:   Wed Feb 8 15:29:53 2017 +1100

    Some refactoring to move the internal representation of file dependencies a little closer to other dependencies, so that `SelectedFileDependencyResults` is-a `SelectedArtifactResults`.