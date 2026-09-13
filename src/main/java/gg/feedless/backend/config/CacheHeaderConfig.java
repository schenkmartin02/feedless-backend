package gg.feedless.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

import java.time.Duration;

@Configuration
public class CacheHeaderConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebContentInterceptor interceptor = new WebContentInterceptor();

        // Champions
        interceptor.addCacheMapping(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic(), "/champions/**");

        // matches
        interceptor.addCacheMapping(CacheControl.maxAge(Duration.ofDays(1)).cachePublic(), "/matches/*");

        // changelog
        interceptor.addCacheMapping(CacheControl.maxAge(Duration.ofDays(1)).cachePublic(), "/changelog");

        // stats
        interceptor.addCacheMapping(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic(), "/stats");

        // ladder * search
        interceptor.addCacheMapping(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic(), "/ladder", "/search");

        // player
        interceptor.addCacheMapping(CacheControl.maxAge(Duration.ofSeconds(60)).cachePublic(), "/players/*/*/*", "/players/*/*/*/matches", "/players/*/*/*/champions");
        interceptor.addCacheMapping(CacheControl.maxAge(Duration.ofSeconds(30)).cachePublic(), "/players/*/*/*/live");
        interceptor.addCacheMapping(CacheControl.noStore(), "/players/*/*/*/refresh");

        interceptor.setCacheControl(CacheControl.noStore());

        registry.addInterceptor(interceptor);
    }
}

