package com.hotel.reservations.controller;

import com.hotel.reservations.dto.AvailabilityItem;
import com.hotel.reservations.dto.InvoiceResponse;
import com.hotel.reservations.dto.ReservationResponse;
import com.hotel.reservations.exception.InvalidStateException;
import com.hotel.reservations.exception.ReservationNotFoundException;
import com.hotel.reservations.facade.HotelFacade;
import com.hotel.reservations.model.ReservationStatus;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.model.Season;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HotelController.class)
class HotelControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private HotelFacade facade;

    private static final String VALID_BODY = """
            {"guestName":"Ana Perez","document":"123","email":"ana@mail.com",
             "roomNumber":201,"checkInDate":"2099-12-20","checkOutDate":"2099-12-23"}
            """;

    @Test
    void createReservation_valid_returns201() throws Exception {
        when(facade.createReservation(any())).thenReturn(reservation("id-1", ReservationStatus.CONFIRMED, null));
        mvc.perform(post("/api/hotel/reservations").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("id-1"));
    }

    @Test
    void createReservation_invalidEmail_returns400() throws Exception {
        String invalid = """
                {"guestName":"Ana","document":"1","email":"not-an-email",
                 "roomNumber":201,"checkInDate":"2099-12-20","checkOutDate":"2099-12-23"}
                """;
        mvc.perform(post("/api/hotel/reservations").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    void availability_returns200WithList() throws Exception {
        when(facade.checkAvailability(any(), any(), any()))
                .thenReturn(List.of(new AvailabilityItem(201, RoomType.DOUBLE, true,
                        new BigDecimal("180.00"), 3, Season.HIGH, new BigDecimal("540.00"))));

        mvc.perform(get("/api/hotel/availability")
                        .param("checkIn", "2099-12-20").param("checkOut", "2099-12-23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomNumber").value(201))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    void availability_withoutParams_returns400() throws Exception {
        mvc.perform(get("/api/hotel/availability")).andExpect(status().isBadRequest());
    }

    @Test
    void addService_valid_returns200() throws Exception {
        when(facade.addService(eq("id-1"), any())).thenReturn(reservation("id-1", ReservationStatus.CONFIRMED, null));
        mvc.perform(post("/api/hotel/reservations/id-1/services").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceType\":\"SPA\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void addService_invalidType_returns400() throws Exception {
        mvc.perform(post("/api/hotel/reservations/id-1/services").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceType\":\"CASINO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkIn_returns200WithKey() throws Exception {
        when(facade.checkIn("id-1"))
                .thenReturn(reservation("id-1", ReservationStatus.CHECKED_IN, "550e8400-e29b-41d4-a716-446655440000"));
        mvc.perform(put("/api/hotel/reservations/id-1/check-in"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.digitalKey").value("550e8400-e29b-41d4-a716-446655440000"));
    }

    @Test
    void checkOut_returns200WithTotal() throws Exception {
        when(facade.checkOut("id-1")).thenReturn(invoice());
        mvc.perform(put("/api/hotel/reservations/id-1/check-out"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(665.00));
    }

    @Test
    void getReservation_notFound_returns404() throws Exception {
        when(facade.getReservation("xxx")).thenThrow(new ReservationNotFoundException("xxx"));
        mvc.perform(get("/api/hotel/reservations/xxx"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void checkOutWithoutCheckIn_returns409() throws Exception {
        when(facade.checkOut("id-1"))
                .thenThrow(new InvalidStateException(ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN));
        mvc.perform(put("/api/hotel/reservations/id-1/check-out")).andExpect(status().isConflict());
    }

    private ReservationResponse reservation(String id, ReservationStatus status, String digitalKey) {
        ReservationResponse.RateView rate = new ReservationResponse.RateView(RoomType.DOUBLE, Season.HIGH,
                new BigDecimal("120.00"), new BigDecimal("1.50"), new BigDecimal("180.00"), 3,
                new BigDecimal("540.00"));
        return new ReservationResponse(id, status,
                new ReservationResponse.GuestView("Ana Perez", "123", "ana@mail.com"), 201, RoomType.DOUBLE,
                LocalDate.parse("2099-12-20"), LocalDate.parse("2099-12-23"), 3, Season.HIGH, rate,
                List.of(), new BigDecimal("540.00"), digitalKey);
    }

    private InvoiceResponse invoice() {
        return new InvoiceResponse("F-0001", "id-1",
                new ReservationResponse.GuestView("Ana Perez", "123", "ana@mail.com"),
                new InvoiceResponse.RoomView(201, RoomType.DOUBLE),
                LocalDate.parse("2099-12-20"), LocalDate.parse("2099-12-23"), 3, Season.HIGH,
                List.of(new InvoiceResponse.LineView("Lodging DOUBLE", 3, new BigDecimal("180.00"), new BigDecimal("540.00")),
                        new InvoiceResponse.LineView("Spa", 1, new BigDecimal("50.00"), new BigDecimal("50.00")),
                        new InvoiceResponse.LineView("Breakfast", 3, new BigDecimal("15.00"), new BigDecimal("45.00")),
                        new InvoiceResponse.LineView("Transfer", 1, new BigDecimal("30.00"), new BigDecimal("30.00"))),
                new BigDecimal("540.00"), new BigDecimal("125.00"), new BigDecimal("665.00"), LocalDateTime.now());
    }
}
