package com.rserene.chosen.server.http;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import com.rserene.chosen.server.main.RSLBCore;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Interceptor.Chain;
import org.jetbrains.annotations.NotNull;

public class RetryInterceptor implements Interceptor {
   private final RSLBCore core;
   private final int retry;
   private final long delay;

   public RetryInterceptor(RSLBCore core, int retry, long delay) {
      this.core = core;
      this.retry = retry;
      this.delay = delay;
   }

   @NotNull
   public Response intercept(@NotNull Chain chain) throws IOException {
      Request request = chain.request();
      int tc = 0;

      while (true) {
         try {
            return chain.proceed(request);
         } catch (IOException e) {
            this.core.logDebug(tc + " retry failed.", e);
            if (tc >= this.retry) {
               throw e;
            }

            try {
               TimeUnit.MILLISECONDS.sleep(this.delay);
            } catch (InterruptedException ex) {
               throw new InterruptedRetryException(ex);
            }

            this.core.logDebug("--> " + ++tc + " retry.");
         }
      }
   }
}