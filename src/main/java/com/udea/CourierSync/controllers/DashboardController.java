package com.udea.CourierSync.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.udea.CourierSync.repository.ShipmentRepository;
import com.udea.CourierSync.repository.ClientRepository;
import com.udea.CourierSync.repository.UserRepository;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Dashboard", description = "API para métricas y estadísticas del sistema (solo ADMIN)")
public class DashboardController {

  @Autowired
  private ShipmentRepository shipmentRepository;

  @Autowired
  private ClientRepository clientRepository;

  @Autowired
  private UserRepository userRepository;

  @Operation(summary = "Obtener métricas del sistema", description = "Obtiene métricas generales del sistema incluyendo el total de envíos, clientes y usuarios. Solo disponible para usuarios con rol ADMIN.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Métricas obtenidas exitosamente", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json")),
      @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN")
  })
  @GetMapping("/metrics")
  public Map<String, Object> metrics() {
    long shipments = shipmentRepository.count();
    long clients = clientRepository.count();
    long users = userRepository.count();

    return Map.of(
        "shipments", shipments,
        "clients", clients,
        "users", users);
  }
}
