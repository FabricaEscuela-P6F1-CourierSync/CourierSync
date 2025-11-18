package com.udea.CourierSync.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udea.CourierSync.DTO.VehicleDTO;
import com.udea.CourierSync.services.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private VehicleService vehicleService;

  @Autowired
  private ObjectMapper objectMapper;

  private VehicleDTO vehicleDTO;

  @BeforeEach
  void setUp() {
    vehicleDTO = new VehicleDTO();
    vehicleDTO.setId(1L);
    vehicleDTO.setPlate("ABC123");
    vehicleDTO.setModel("Toyota Corolla");
    vehicleDTO.setMaximumCapacity(500.0);
    vehicleDTO.setAvailable(true);
  }

  @Test
  void testCreateVehicle_Success() throws Exception {
    when(vehicleService.createVehicle(any(VehicleDTO.class))).thenReturn(vehicleDTO);

    mockMvc.perform(post("/api/vehicles")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(vehicleDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.plate").value("ABC123"))
        .andExpect(jsonPath("$.model").value("Toyota Corolla"));

    verify(vehicleService, times(1)).createVehicle(any(VehicleDTO.class));
  }

  @Test
  void testGetAllVehicles_Success() throws Exception {
    VehicleDTO vehicleDTO2 = new VehicleDTO();
    vehicleDTO2.setId(2L);
    vehicleDTO2.setPlate("XYZ789");

    when(vehicleService.findAll()).thenReturn(Arrays.asList(vehicleDTO, vehicleDTO2));

    mockMvc.perform(get("/api/vehicles"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[1].id").value(2L));

    verify(vehicleService, times(1)).findAll();
  }

  @Test
  void testGetVehicleById_Success() throws Exception {
    when(vehicleService.findById(1L)).thenReturn(Optional.of(vehicleDTO));

    mockMvc.perform(get("/api/vehicles/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.plate").value("ABC123"));

    verify(vehicleService, times(1)).findById(1L);
  }

  @Test
  void testGetVehicleById_NotFound() throws Exception {
    when(vehicleService.findById(999L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/vehicles/999"))
        .andExpect(status().isNotFound());

    verify(vehicleService, times(1)).findById(999L);
  }

  @Test
  void testUpdateVehicle_Success() throws Exception {
    VehicleDTO updatedDTO = new VehicleDTO();
    updatedDTO.setModel("Honda Civic");
    updatedDTO.setAvailable(false);

    when(vehicleService.update(eq(1L), any(VehicleDTO.class))).thenReturn(updatedDTO);

    mockMvc.perform(put("/api/vehicles/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updatedDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.model").value("Honda Civic"))
        .andExpect(jsonPath("$.available").value(false));

    verify(vehicleService, times(1)).update(eq(1L), any(VehicleDTO.class));
  }

  @Test
  void testDeleteVehicle_Success() throws Exception {
    doNothing().when(vehicleService).deleteById(1L);

    mockMvc.perform(delete("/api/vehicles/1"))
        .andExpect(status().isNoContent());

    verify(vehicleService, times(1)).deleteById(1L);
  }
}
