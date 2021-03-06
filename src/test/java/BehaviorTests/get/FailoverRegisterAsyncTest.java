/**
 * The MIT License
 *
 * Copyright for portions of failover-safe are held by creatorchina Inc (c) 2020.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


package BehaviorTests.get;

import com.creatorchina.core.FailoverRegister;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.Assert;
import org.junit.Test;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import static BehaviorTests.get.MockServer.*;
import static spark.Spark.get;

/**
 * @ClassName: FailoverRegisterTest
 * @Description: 4
 * @Author: jack.liang
 * @Date: 2020/6/29 下午11:41
 **/
public class FailoverRegisterAsyncTest extends BddTest {

    static {
        get("/failover/register", (request, response) -> {
            Thread.sleep(100000);
            return "register";
        });
    }

    @Test
    public void simpleMethodFailoverTestForAsyncOrSync() throws ExecutionException, InterruptedException {
        Unirest.config().socketTimeout(2000);
        Unirest.config().connectTimeout(2000);

        CompletableFuture<HttpResponse> future = FailoverRegister.
                instance.
                buildPolicy()
                .onFailedAttempt((e) -> System.out.println("onFailedAttempt"))
                .withMaxAttempts(6)
                .withDelay(Duration.ofSeconds(2)).
                        onFailure((e) -> System.out.println("onFailure")).
                        assemblyFailover(() -> Unirest.get(FAILOVER).asString(),null);

        HttpResponse httpResponse = future.get();
        if (httpResponse == null){
            System.out.println("null");
            return;
        }
        Assert.assertNotNull(httpResponse.getBody());
        Assert.assertEquals(httpResponse.getBody(), "register");
    }
}
