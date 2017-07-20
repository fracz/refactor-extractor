commit c3de3450a7c6076f363dba8caca727a8e66450ec
Author: Alexander Makarov <sam@rmcreative.ru>
Date:   Thu Jun 19 18:55:07 2014 +0400

    Fixes #3939: `\yii\Inflector::slug()` improvements:

    - Added protected `\yii\Inflector::transliterate()` that could be replaced with custom translit implementation.
    - Added proper tests for both intl-based slug and PHP fallback.
    - Removed character maps for non-latin languages.
    - Improved overall slug results.
    - Added note about the fact that intl is required for non-latin languages to requirements checker.