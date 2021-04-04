package com.aerospike.redispike.listener.key

import com.aerospike.client.Key
import com.aerospike.client.Value
import com.aerospike.client.listener.ExistsArrayListener
import com.aerospike.redispike.command.RequestCommand
import com.aerospike.redispike.config.AerospikeContext
import com.aerospike.redispike.listener.BaseListener
import io.netty.channel.ChannelHandlerContext

class ExistsCommandListener(
    aeroCtx: AerospikeContext,
    ctx: ChannelHandlerContext
) : BaseListener(aeroCtx, ctx), ExistsArrayListener {

    override fun handle(cmd: RequestCommand) {
        require(cmd.argCount >= 2) { argValidationErrorMsg(cmd) }

        val keys = getKeys(cmd)
        aeroCtx.client.exists(
            null, this,
            null, keys.toTypedArray()
        )
    }

    private fun getKeys(cmd: RequestCommand): Set<Key> {
        return cmd.args!!.drop(1)
            .map { createKey(Value.get(it)) }
            .toSet()
    }

    override fun onSuccess(keys: Array<out Key>?, exists: BooleanArray?) {
        try {
            val count = exists?.count { it } ?: 0
            writeLong(ctx, count)
            ctx.flush()
        } catch (e: Exception) {
            closeCtx(e)
        }
    }
}
