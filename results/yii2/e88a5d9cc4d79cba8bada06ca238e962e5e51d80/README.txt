commit e88a5d9cc4d79cba8bada06ca238e962e5e51d80
Author: Alexander Makarov <sam@rmcreative.ru>
Date:   Sat Nov 22 02:19:10 2014 +0300

    Fixes #4823 and #6005: `yii message` accuracy and error handling were improved by using PHP tokenizer instead of regular expressions. Removed eval() as well.