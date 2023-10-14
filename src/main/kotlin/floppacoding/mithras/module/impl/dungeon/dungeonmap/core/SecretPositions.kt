package floppacoding.mithras.module.impl.dungeon.dungeonmap.core

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import floppacoding.mithras.config.jsonutils.SetBlockPosAdapter
import net.minecraft.util.math.BlockPos


class SecretPositions (
    @JsonAdapter(SetBlockPosAdapter::class)
    @SerializedName("chest")
    val chests: Set<BlockPos>,
    @JsonAdapter(SetBlockPosAdapter::class)
    @SerializedName("item")
    val itemDrops: Set<BlockPos>,
    @JsonAdapter(SetBlockPosAdapter::class)
    @SerializedName("wither")
    val witherEssences: Set<BlockPos>
)