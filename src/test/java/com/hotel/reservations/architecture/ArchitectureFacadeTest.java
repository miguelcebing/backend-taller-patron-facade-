package com.hotel.reservations.architecture;

import com.hotel.reservations.controller.HotelController;
import com.hotel.reservations.service.AccessService;
import com.hotel.reservations.service.AdditionalServiceService;
import com.hotel.reservations.service.BillingService;
import com.hotel.reservations.service.RateService;
import com.hotel.reservations.service.RoomService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * The Facade pattern as an executable rule: the controller only knows the facade
 * and the subsystems do not know each other.
 */
class ArchitectureFacadeTest {

    @Test
    void controllerOnlyDependsOnTheFacade() {
        for (Field field : HotelController.class.getDeclaredFields()) {
            assertFalse(field.getType().getSimpleName().endsWith("Service"),
                    "Controller must not inject subsystems: " + field.getType().getSimpleName());
        }
    }

    @Test
    void subsystemsDoNotKnowEachOther() {
        List<Class<?>> subsystems = List.of(
                RoomService.class,
                RateService.class,
                AdditionalServiceService.class,
                BillingService.class,
                AccessService.class);

        for (Class<?> subsystem : subsystems) {
            for (Field field : subsystem.getDeclaredFields()) {
                assertFalse(field.getType().getSimpleName().endsWith("Service"),
                        subsystem.getSimpleName() + " depends on " + field.getType().getSimpleName());
            }
        }
    }
}
