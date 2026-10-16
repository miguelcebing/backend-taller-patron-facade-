package com.hotel.reservations.service;

import com.hotel.reservations.exception.NoAvailabilityException;
import com.hotel.reservations.model.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomServiceTest {

    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = new RoomService();
        roomService.initialize();
    }

    @Test
    void hasAtLeast15RoomsAndAllTypes() {
        assertTrue(roomService.totalRooms() >= 15);
        var types = roomService.list(d("2026-03-10"), d("2026-03-12"), null).stream()
                .map(room -> room.getType()).distinct().toList();
        assertTrue(types.contains(RoomType.SINGLE));
        assertTrue(types.contains(RoomType.DOUBLE));
        assertTrue(types.contains(RoomType.SUITE));
    }

    @Test
    void halfOpenRanges_checkOutOnDayAllowsCheckInSameDay() {
        roomService.reserve("r1", 201, d("2026-03-10"), d("2026-03-12"));
        assertDoesNotThrow(() -> roomService.reserve("r2", 201, d("2026-03-12"), d("2026-03-14")));
    }

    @Test
    void overlappingRange_throwsNoAvailability() {
        roomService.reserve("r1", 201, d("2026-03-10"), d("2026-03-12"));
        assertThrows(NoAvailabilityException.class,
                () -> roomService.reserve("r2", 201, d("2026-03-11"), d("2026-03-13")));
    }

    @Test
    void releaseIsIdempotentAndFreesTheRoom() {
        roomService.reserve("r1", 201, d("2026-03-10"), d("2026-03-12"));
        roomService.release("r1");
        roomService.release("r1");
        assertTrue(roomService.isAvailable(201, d("2026-03-10"), d("2026-03-12")));
    }

    @Test
    void concurrency_onlyOneReservationWins() throws InterruptedException {
        int threads = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            final int id = i;
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    roomService.reserve("r" + id, 201, d("2026-04-01"), d("2026-04-03"));
                    successes.incrementAndGet();
                } catch (NoAvailabilityException ignored) {
                    // expected for the losers
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        assertTrue(ready.await(5, TimeUnit.SECONDS));
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(1, successes.get());
    }

    private LocalDate d(String iso) {
        return LocalDate.parse(iso);
    }
}
