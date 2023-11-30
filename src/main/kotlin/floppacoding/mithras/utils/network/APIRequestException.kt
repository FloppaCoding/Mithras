package floppacoding.mithras.utils.network

class APIRequestException(message: String?, cause: Throwable?) : Exception(message, cause) {
    constructor(message: String): this(message, null)
    constructor(cause: Throwable): this(null, cause)
    constructor(): this(null, null)
}