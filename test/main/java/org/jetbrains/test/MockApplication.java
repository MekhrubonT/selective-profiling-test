package org.jetbrains.test;

import org.jetbrains.test.profiller.Profiling;
import org.junit.Test;

public class MockApplication {
    @Profiling(type = Profiling.NameType.OWN, ownName = "a")
    void a() {
        b();
        c();
        d();
    }
    @Profiling(type = Profiling.NameType.OWN, ownName = "b")
    void b() {
        c();
        d();
    }
    @Profiling(type = Profiling.NameType.OWN, ownName = "c")
    void c() {
        d();
    }
    @Profiling(type = Profiling.NameType.OWN, ownName = "d")
    void d() {
        for (int i = 0; i < 10; i++) {
            e(i);
        }
    }
    @Profiling(type = Profiling.NameType.OWN, ownName = "e")
    void e(int i) {
    }

}
