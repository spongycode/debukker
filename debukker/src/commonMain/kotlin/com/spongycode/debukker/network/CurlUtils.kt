package com.spongycode.debukker.network

import com.spongycode.debukker.models.NetworkRequest

fun generateCurlCommand(request: NetworkRequest): String {
    return buildString {
        append("curl '${request.url}'")
        append(" \\\n  -X '${request.method}'")
        request.headers.forEach { (key, value) ->
            append(" \\\n  -H '${key}: ${value}'")
        }
        request.body?.let { body ->
            val escapedBody = body.replace("'", "'\\''")
            append(" \\\n  -d '${escapedBody}'")
        }
        append(" \\\n  --compressed")
    }
}

