commit 96f3c51eeac6efd62649a22368457d6d69f8d1c5
Author: Kevin Gorham <kevin@silverchalice.com>
Date:   Thu Jun 13 17:09:05 2013 -0600

    [issue 70] Added support for generating Gson compatible types. Changes include: Added Gson dependency. Minor refactor of Annotator interface in order to add GsonAnnotator. Extending an AbstractAnnotator makes creating implementations a tad easier. GsonAnnotator logic only adds SerializedName annotation to fields when it's necessary.