package com.udea.CourierSync.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.udea.CourierSync.services.VehicleService;
import com.udea.CourierSync.DTO.VehicleDTO;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicles", description = "API para gestión de vehículos")
public class VehicleController {

  @Autowired
  private VehicleService vehicleService;

  @Operation(summary = "Crear un nuevo vehículo", description = "Crea un nuevo vehículo en el sistema con la información proporcionada (placa, modelo, capacidad máxima y disponibilidad).")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Vehículo creado exitosamente", content = @Content(schema = @Schema(implementation = VehicleDTO.class))),
      @ApiResponse(responseCode = "400", description = "Datos inválidos")
  })
  @CrossOrigin
  @PostMapping
  public ResponseEntity<VehicleDTO> create(@RequestBody VehicleDTO dto) {
    VehicleDTO created = vehicleService.createVehicle(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Operation(summary = "Listar todos los vehículos", description = "Obtiene una lista de todos los vehículos registrados en el sistema.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lista de vehículos obtenida exitosamente", content = @Content(schema = @Schema(implementation = VehicleDTO.class)))
  })
  @GetMapping
  public List<VehicleDTO> list() {
    return vehicleService.findAll();
  }

  @Operation(summary = "Obtener un vehículo por ID", description = "Obtiene los detalles de un vehículo específico mediante su ID.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Vehículo encontrado", content = @Content(schema = @Schema(implementation = VehicleDTO.class))),
      @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
  })
  @GetMapping("/{id}")
  public ResponseEntity<VehicleDTO> get(
      @Parameter(description = "ID del vehículo", required = true) @PathVariable Long id) {
    return vehicleService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Actualizar un vehículo", description = "Actualiza la información de un vehículo existente.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Vehículo actualizado exitosamente", content = @Content(schema = @Schema(implementation = VehicleDTO.class))),
      @ApiResponse(responseCode = "400", description = "Datos inválidos"),
      @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
  })
  @CrossOrigin
  @PutMapping("/{id}")
  public ResponseEntity<VehicleDTO> update(
      @Parameter(description = "ID del vehículo", required = true) @PathVariable Long id,
      @RequestBody VehicleDTO dto) {
    VehicleDTO updated = vehicleService.update(id, dto);
    return ResponseEntity.ok(updated);
  }

  @Operation(summary = "Eliminar un vehículo", description = "Elimina un vehículo del sistema mediante su ID.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Vehículo eliminado exitosamente"),
      @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @Parameter(description = "ID del vehículo", required = true) @PathVariable Long id) {
    vehicleService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
