package com.example.rwellnitz.tasktest.auto;

import java.util.concurrent.atomic.AtomicLong;

final class IdGenerator {
    private final AtomicLong NEXT_ID = new AtomicLong(0);

    long nextId() {
        return NEXT_ID.getAndIncrement();
    }
}
