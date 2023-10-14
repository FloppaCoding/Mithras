package floppacoding.mithras.module.impl.dungeon.dungeonmap.core

import com.google.gson.annotations.SerializedName


enum class RoomShape(val size: Int) {
    @SerializedName("1x1")
    I_1X1(1),
    @SerializedName("1x2")
    I_1X2(2),
    @SerializedName("1x3")
    I_1X3(3),
    @SerializedName("1x4")
    I_1X4(4),
    @SerializedName("2x2")
    A_2X2(4),
    L(3);

}