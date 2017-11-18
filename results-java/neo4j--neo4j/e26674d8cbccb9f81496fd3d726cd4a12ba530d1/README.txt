commit e26674d8cbccb9f81496fd3d726cd4a12ba530d1
Author: Pontus Melke <pontusmelke@gmail.com>
Date:   Thu May 12 11:05:46 2016 +0200

    Remove compiled runtime from 2.3

    In 2.3 the compiled runtime has some unwanted dependencies that makes refactoring hard,
      so let's just remove it.