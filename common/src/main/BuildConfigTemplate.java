package cn.zbx1425.mtrsteamloco;

import java.time.Instant;

public interface BuildConfig {

    String MOD_VERSION = "@version@";

    Instant BUILD_TIME = Instant.ofEpochSecond(@build_time@);
}