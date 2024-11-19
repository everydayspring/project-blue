package com.sparta.projectblue.config;

public class CacheKey {
    public static final long DEFAULT_EXPIRE_SEC = 60L * 5L;
    public static final String PERFORMER = "performer";
    public static final String PERFORMERS = "performers";
    public static final long PERFORMER_EXPIRE_SEC = 60L * 5L;

    private CacheKey() {
        throw new UnsupportedOperationException("캐싱 키");
    }
}
