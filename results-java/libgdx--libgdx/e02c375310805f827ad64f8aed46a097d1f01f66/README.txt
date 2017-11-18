commit e02c375310805f827ad64f8aed46a097d1f01f66
Author: davedx@gmail.com <davedx@gmail.com@6c4fd544-2939-11df-bb46-9574ba5d0bfa>
Date:   Fri Jan 7 11:18:44 2011 +0000

    [Changed] Allowed meshes to be hidden. This also allows KeyframedModels to specify which of their meshes should be drawn. This is useful for models with 'optional' attachments like helmets, backpacks, etc.
    [Changed] Added blending options to material. Probably should be improved.
    [Changed] Changed parameter names in TextureDict to match Gdx.Texture.