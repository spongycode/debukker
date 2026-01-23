package com.spongycode.debukker.network

import com.spongycode.debukker.models.NetworkRequest


fun generateCurlCommand(request: NetworkRequest): String {
    return buildString {
        append("curl")
        
        if (request.method != "GET") {
            append(" -X ${request.method}")
        }
        
        request.headers.forEach { (key, value) ->
            append(" \\\n  -H '${key}: ${value}'")
        }
        
        request.body?.let { body ->
            val escapedBody = body.replace("'", "'\\''")
            append(" \\\n  -d '${escapedBody}'")
        }
        
        append(" \\\n  '${request.url}'")
    }
}

