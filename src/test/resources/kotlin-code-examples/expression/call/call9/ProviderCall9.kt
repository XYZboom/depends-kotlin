package expression.call.call9

class ProviderCall9(
    private val receiverCall9: ReceiverCall9,
) {
    fun func(context: ReceiverCall9.() -> Unit) {
        receiverCall9.context()
    }
}