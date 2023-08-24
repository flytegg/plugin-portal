package gg.flyte.common.api

import gg.flyte.common.type.logger.LogType
import gg.flyte.common.type.logger.Logger
import gg.flyte.common.type.logger.getStatusType
import okhttp3.Interceptor
import okhttp3.Response

class RequestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        return chain.proceed(chain.request()).apply {
            Logger.log(request.url.toString(), code.getStatusType(), LogType.valueOf(request.method))
        }

    }

}