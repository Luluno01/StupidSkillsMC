package vip.untitled.stupidskills.effects.stupideffect

import vip.untitled.stupidskills.effects.stupideffect.meta.RemainingStupidDurationMetadataValue
import vip.untitled.stupidskills.effects.stupideffect.meta.StupidMetadataValue
import vip.untitled.stupidskills.effects.stupideffect.meta.StupidPigMetadataValue

interface StupidFeatureCollection {
    var store: AccumulatedStupidityStore
    var stupidityTags: StupidityTags
    var stupidMetadataHandler: StupidMetadataValue.Companion.StupidMetadataHandler
    var stupidState: StupidState
    var remainingStupidDurationMetadataHandler: RemainingStupidDurationMetadataValue.Companion.RemainingStupidDurationMetadataHandler
    var stupidPigHandler: StupidPigMetadataValue.Companion.StupidPigHandler
}