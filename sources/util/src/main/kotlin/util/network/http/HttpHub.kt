package util.network.http

import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import util.Option
import util.maid
import util.global.executeAndGetSilent
import util.global.logger


/**
 * HttpHub provides interface for interacting with http protocol.
 */
class HttpHub {
    internal val log = logger("http")
    internal val hc = HttpClients.createDefault().apply { maid.internalAdd({ this.close() }, 1, "http-client") }

    // interface

    fun get(url: String): Option<String> {
        return executeAndGetSilent { EntityUtils.toString(hc.execute(RequestBuilder.get(url).build()).entity) }
    }

    fun getString(requestBuilder: RequestBuilder): Option<String> {
        return getString(requestBuilder.build())
    }

    fun getString(request: HttpUriRequest): Option<String> {
        log.debug { "requesting ${request.uri}" }
        return executeAndGetSilent { EntityUtils.toString(hc.execute(request).entity) }
    }

}
