commit e9ed806d30b6e4dd7a9ab49132632c4cde66eed3
Author: dswitkin <dswitkin@59b500cc-1b3d-0410-9834-0bbf25fbcc57>
Date:   Tue Apr 8 15:25:13 2008 +0000

    Rewrote the Android M3 client to do continuous decoding, which means you no longer have to push the shutter button. Now you can just place the barcode in the viewfinder and it will display the contents as soon as it decodes them. That also means you no longer get "barcode not found" error dialogs which is a big improvement. Also made sure that capturing debug JPEGs uses unique filenames.

    git-svn-id: https://zxing.googlecode.com/svn/trunk@352 59b500cc-1b3d-0410-9834-0bbf25fbcc57