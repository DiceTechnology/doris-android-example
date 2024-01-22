package com.dice.doris.csai.player

import com.diceplatform.doris.entity.Source

/* Holds the configured source properties from the `MainActivity` which will be used in the `PlayerActivity`
 upon launch. Note that this an anti-pattern (must be avoided in a real app), but for now this is the
 easiest way to "pass" the configured source properties to the player (passing through the intent is
 not possible, as some of the exo-doris specific components are not serializable). */
object SourceHolder {
    var source: Source? = null
}
