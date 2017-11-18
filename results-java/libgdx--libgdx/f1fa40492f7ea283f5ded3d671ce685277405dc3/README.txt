commit f1fa40492f7ea283f5ded3d671ce685277405dc3
Author: badlogicgames <badlogicgames@6c4fd544-2939-11df-bb46-9574ba5d0bfa>
Date:   Sun Oct 31 20:39:39 2010 +0000

    [refactored] Mesh now uses VertexArray, VertexBufferObject and IndexBufferObject
    [refactored] SpriteBatch now only binds the Mesh once. Will probably only have an effect if a shitload of sprites have been rendered or there's a lot of texture changes.
    [fixed] ShaderProgram.dispose() had to unbind the program before a delete.