package com.micro.productos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "producto")
public class ProductoServiceProperties {

    private Retry retry = new Retry();
    private Timeout timeout = new Timeout();

    public static class Retry {
        private int maxAttempts;
        private long delay;

        // getters y setters
        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

        public long getDelay() { return delay; }
        public void setDelay(long delay) { this.delay = delay; }
    }

    public static class Timeout {
        private long milliseconds;

        public long getMilliseconds() { return milliseconds; }
        public void setMilliseconds(long milliseconds) { this.milliseconds = milliseconds; }
    }

    public Retry getRetry() { return retry; }
    public Timeout getTimeout() { return timeout; }

    public void setRetry(Retry retry) { this.retry = retry; }
    public void setTimeout(Timeout timeout) { this.timeout = timeout; }
}
