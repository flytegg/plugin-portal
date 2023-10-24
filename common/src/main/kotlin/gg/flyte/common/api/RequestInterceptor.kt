package gg.flyte.common.api


import okhttp3.Interceptor
import okhttp3.Response

class RequestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        return chain.proceed(chain.request()).apply {
            println("${request.url} | $code | ${request.method}")
        }

    }

}