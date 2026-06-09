package floppacoding.mithras.utils.inventory

import com.google.gson.annotations.Expose

open class SkyblockItem(
    @Expose
    val itemID: String,
    protected val attributes: List<ItemAttribute> = listOf()
) {

    constructor(itemID: String, vararg attributes: ItemAttribute) : this(itemID, listOf(*attributes))

    fun hasAttribute(attribute: ItemAttribute): Boolean = this.attributes.contains(attribute)
}

open class NamedSkyblockItem(
    val displayName: String,
    itemID: String,
    attributes: List<ItemAttribute> = listOf()
) : SkyblockItem(itemID, attributes) {
    constructor(displayName: String, itemID: String, vararg attributes: ItemAttribute) : this(
        displayName,
        itemID,
        attributes.asList()
    )
}