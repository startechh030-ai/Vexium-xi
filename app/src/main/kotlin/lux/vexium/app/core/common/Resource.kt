package lux.vexium.app.core.common

/**
 * A generic wrapper for API/data responses.
 * Use this in ViewModels to represent loading, success, and error states.
 */
sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : Resource<Nothing>()
}
