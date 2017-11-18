commit 226527bf6c5daf0274cf4d557d86253dcfeffd12
Author: nathan.sweet <nathan.sweet@6c4fd544-2939-11df-bb46-9574ba5d0bfa>
Date:   Fri Dec 3 12:46:06 2010 +0000

    [added] Texture#setWrap.
    [added] Bunch of functionality to TextureRegion.
    [changed] Sprite, BitmapFont to use TextureRegion. Moved Sprite texture manipulating methods into TextureRegion. This is an API breaking change!
    [changed] FileHandle, better exception trying to read/write a directory.
    [added] getColor to BitmapFont.
    [fixed] Sprite#rotate90.
    [fixed] SpriteBatch draw method, javadocs were wrong and params were int when should be float.
    [added] SpriteBatch and SpriteCache draw methods for TextureRegion.
    [added] A packed font to SpriteSheetTest.
    [added] Missing tiles.png from LWJGL backend.

    Note: A few more refactoring items still remain: SpriteSheet is currently broken! TextureAtlas dies and SpriteSheet becomes TextureAtlas. TextureAtlas needs to provide Sprites that respect offset for proper position of whitespace stripped regions. SpriteSheetPacker becomes TexturePacker.