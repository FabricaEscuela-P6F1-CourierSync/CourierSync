package com.udea.CourierSync.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udea.CourierSync.DTO.ClientDTO;
import com.udea.CourierSync.services.ClientService;
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

@WebMvcTest(controllers = ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ClientService clientService;

  @Autowired
  private ObjectMapper objectMapper;

  private ClientDTO clientDTO;

  @BeforeEach
  void setUp() {
    clientDTO = new ClientDTO();
    clientDTO.setId(1L);
    clientDTO.setName("John Doe");
    clientDTO.setEmail("john@example.com");
    clientDTO.setPhone("1234567890");
    clientDTO.setAddress("123 Main St");
  }

  @Test
  void testCreateClient_Success() throws Exception {
    when(clientService.createClient(any(ClientDTO.class))).thenReturn(clientDTO);

    mockMvc.perform(post("/api/clients")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(clientDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("John Doe"))
        .andExpect(jsonPath("$.email").value("john@example.com"));

    verify(clientService, times(1)).createClient(any(ClientDTO.class));
  }

  @Test
  void testGetAllClients_Success() throws Exception {
    ClientDTO clientDTO2 = new ClientDTO();
    clientDTO2.setId(2L);
    clientDTO2.setName("Jane Doe");

    when(clientService.findAll()).thenReturn(Arrays.asList(clientDTO, clientDTO2));

    mockMvc.perform(get("/api/clients"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[1].id").value(2L));

    verify(clientService, times(1)).findAll();
  }

  @Test
  void testGetClientById_Success() throws Exception {
    when(clientService.findById(1L)).thenReturn(Optional.of(clientDTO));

    mockMvc.perform(get("/api/clients/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("John Doe"));

    verify(clientService, times(1)).findById(1L);
  }

  @Test
  void testGetClientById_NotFound() throws Exception {
    when(clientService.findById(999L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/clients/999"))
        .andExpect(status().isNotFound());

    verify(clientService, times(1)).findById(999L);
  }

  @Test
  void testUpdateClient_Success() throws Exception {
    ClientDTO updatedDTO = new ClientDTO();
    updatedDTO.setName("John Updated");
    updatedDTO.setEmail("john.updated@example.com");

    when(clientService.update(eq(1L), any(ClientDTO.class))).thenReturn(updatedDTO);

    mockMvc.perform(put("/api/clients/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updatedDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("John Updated"));

    verify(clientService, times(1)).update(eq(1L), any(ClientDTO.class));
  }

  @Test
  void testDeleteClient_Success() throws Exception {
    doNothing().when(clientService).deleteById(1L);

    mockMvc.perform(delete("/api/clients/1"))
        .andExpect(status().isNoContent());

    verify(clientService, times(1)).deleteById(1L);
  }
}
