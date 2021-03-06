package com.aerospike.skyhook.listener.map

import com.aerospike.client.Key
import com.aerospike.client.Record
import com.aerospike.client.Value
import com.aerospike.client.cdt.MapOperation
import com.aerospike.client.cdt.MapReturnType
import com.aerospike.client.listener.RecordListener
import com.aerospike.skyhook.command.RequestCommand
import com.aerospike.skyhook.listener.BaseListener
import com.aerospike.skyhook.util.Typed
import io.netty.channel.ChannelHandlerContext

class MapDelCommandListener(
    ctx: ChannelHandlerContext
) : BaseListener(ctx), RecordListener {

    override fun handle(cmd: RequestCommand) {
        require(cmd.argCount >= 3) { argValidationErrorMsg(cmd) }

        val key = createKey(cmd.key)
        val operation = MapOperation.removeByKeyList(
            aeroCtx.bin,
            getValues(cmd), MapReturnType.COUNT
        )
        client.operate(
            null, this, defaultWritePolicy,
            key, operation, *systemOps()
        )
    }

    private fun getValues(cmd: RequestCommand): List<Value> {
        return cmd.args.drop(2)
            .map { Typed.getValue(it) }
    }

    override fun onSuccess(key: Key?, record: Record?) {
        if (record == null) {
            writeNullArray()
            flushCtxTransactionAware()
        } else {
            try {
                writeResponse(record.bins[aeroCtx.bin])
                flushCtxTransactionAware()
            } catch (e: Exception) {
                closeCtx(e)
            }
        }
    }
}
