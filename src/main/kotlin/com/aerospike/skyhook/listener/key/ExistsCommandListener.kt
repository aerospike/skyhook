package com.aerospike.skyhook.listener.key

import com.aerospike.client.Key
import com.aerospike.client.listener.ExistsArrayListener
import com.aerospike.skyhook.command.RequestCommand
import com.aerospike.skyhook.listener.BaseListener
import io.netty.channel.ChannelHandlerContext

class ExistsCommandListener(
    ctx: ChannelHandlerContext
) : BaseListener(ctx), ExistsArrayListener {

    override fun handle(cmd: RequestCommand) {
        require(cmd.argCount >= 2) { argValidationErrorMsg(cmd) }

        val keys = getKeys(cmd)
        client.exists(
            null, this,
            null, keys.toTypedArray()
        )
    }

    private fun getKeys(cmd: RequestCommand): Set<Key> {
        return cmd.args.drop(1)
            .map { createKey(it) }
            .toSet()
    }

    override fun onSuccess(keys: Array<out Key>?, exists: BooleanArray?) {
        try {
            val count = exists?.count { it } ?: 0
            writeLong(count)
            flushCtxTransactionAware()
        } catch (e: Exception) {
            closeCtx(e)
        }
    }
}
