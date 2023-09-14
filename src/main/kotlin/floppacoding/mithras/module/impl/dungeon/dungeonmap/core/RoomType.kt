package floppacoding.mithras.module.impl.dungeon.dungeonmap.core

enum class RoomType {
    BLOOD,

    /**
     * For the yellow mini-boss room.
     * @see MINIBOSS
     */
    CHAMPION,
    ENTRANCE,
    FAIRY,

    /**
     * For regular brown rooms when it is not known whether it is a mobs or mini-boss room.
     * @see MOBS
     * @see MINIBOSS
     */
    NORMAL,

    /**
     * For normal rooms with mobs clear.
     */
    MOBS,

    /**
     * For normal rooms with mini-boss clear.
     * @see CHAMPION
     */
    MINIBOSS,
    PUZZLE,
    RARE,
    TRAP,
    BOSS,

    /**
     * For the question mark rooms on the map.
     */
    UNKNOWN,
    REGION
}
