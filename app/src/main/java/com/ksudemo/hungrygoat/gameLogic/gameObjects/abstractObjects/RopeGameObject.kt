package com.ksudemo.hungrygoat.gameLogic.gameObjects.abstractObjects

import com.ksudemo.hungrygoat.constants.enums.GameObjectTags
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.rope.Rope
import com.ksudemo.hungrygoat.gameLogic.gameObjects.inheritedObject.rope.RopeSegment

abstract class RopeGameObject(
    objectFrom: GameObject,
    objectTo: GameObject,
    tg: GameObjectTags,
) : GameObject((objectFrom.x + objectTo.x) / 2, (objectFrom.y + objectTo.y) / 2, tg, 0f) {

    val ropeSegments: HashSet<RopeSegment> = hashSetOf()
    val attachedRopesHashSet: HashSet<Rope> = hashSetOf()
}