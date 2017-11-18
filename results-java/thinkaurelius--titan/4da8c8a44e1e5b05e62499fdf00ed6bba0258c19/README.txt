commit 4da8c8a44e1e5b05e62499fdf00ed6bba0258c19
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Mon Aug 11 21:05:27 2014 -0400

    Additional addVertexWithLabel doc and refactoring

    * Created addVertexWithLabel(VertexLabel) in TitanGraph for symmetry
      with TitanTransaction

    * Updated labeled addVertex calls in the docs

    * Updated the section in docs/titanbasics.txt that specifically covers
      vertex labels and their creation

    * Updated javadoc for addVertexWithLabel methods in both TitanGraph
      and TitanTransaction