package com.rserene.chosen.server.http;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import com.rserene.chosen.server.main.RSLBCore;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Interceptor.Chain;
import okio.Buffer;
import okio.BufferedSource;
import org.jetbrains.annotations.NotNull;

public class LoggingInterceptor implements Interceptor {
   private final RSLBCore core;

   public LoggingInterceptor(RSLBCore core) {
      this.core = core;
   }

   @NotNull
   public Response intercept(@NotNull Chain chain) throws IOException {
      Request request = chain.request();
      this.core.logDebug(String.format("--> %s %s", request.method(), request.url()));
      RequestBody requestBody = request.body();
      if (requestBody != null) {
         Buffer bf = new Buffer();
         requestBody.writeTo(bf);
         long size = bf.size();
         if (size > 0L) {
            this.core.logDebug(String.format("--> (%d bytes)", size));
         }
      }

      long startNs = System.nanoTime();

      Response response;
      try {
         response = chain.proceed(request);
      } catch (Exception e) {
         this.core.logDebug("<-- HTTP FAILED", e);
         throw e;
      }

      long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
      this.core.logDebug(String.format("<-- %s %s (%dms)", response.code(), response.request().url(), tookMs));
      ResponseBody body = response.body();
      if (body != null) {
         BufferedSource source = body.source();
         source.request(Long.MAX_VALUE);
         Buffer buffer = source.getBuffer();
         long size = buffer.size();
         if (size > 0L) {
            this.core.logDebug(String.format("<-- (%d bytes)", size));
         }
      }

      return response;
   }
}