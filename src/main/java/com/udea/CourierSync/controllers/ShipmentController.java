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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.udea.CourierSync.services.ShipmentService;
import com.udea.CourierSync.DTO.ShipmentDTO;
import com.udea.CourierSync.enums.ShipmentStatus;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@Tag(name = "Shipments", description = "API para gestión de envíos (shipments)")
public class ShipmentController {

  @Autowired
  private ShipmentService shipmentService;

  @Operation(summary = "Crear un nuevo envío", description = "Crea un nuevo envío en el sistema. Los ADMIN pueden crear envíos con cualquier estado, mientras que los OPERATOR solo pueden crear envíos con estado PENDING.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Envío creado exitosamente", content = @Content(schema = @Schema(implementation = ShipmentDTO.class))),
      @ApiResponse(responseCode = "400", description = "Datos inválidos"),
      @ApiResponse(responseCode = "403", description = "No autorizado para crear este tipo de envío")
  })
  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or (hasRole('OPERATOR') and #dto.status == 'PENDING')")
  public ResponseEntity<ShipmentDTO> create(@RequestBody ShipmentDTO dto) {
    ShipmentDTO created = shipmentService.createShipment(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Operation(summary = "Listar todos los envíos", description = "Obtiene una lista de todos los envíos en el sistema. Disponible para ADMIN, OPERATOR y DRIVER.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lista de envíos obtenida exitosamente", content = @Content(schema = @Schema(implementation = ShipmentDTO.class))),
      @ApiResponse(responseCode = "403", description = "No autorizado")
  })
  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'DRIVER')")
  public List<ShipmentDTO> list() {
    return shipmentService.findAll();
  }

  @Operation(summary = "Obtener un envío por ID", description = "Obtiene los detalles de un envío específico mediante su ID. Disponible para ADMIN, OPERATOR y DRIVER.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Envío encontrado", content = @Content(schema = @Schema(implementation = ShipmentDTO.class))),
      @ApiResponse(responseCode = "404", description = "Envío no encontrado"),
      @ApiResponse(responseCode = "403", description = "No autorizado")
  })
  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'DRIVER')")
  public ResponseEntity<ShipmentDTO> get(
      @Parameter(description = "ID del envío", required = true) @PathVariable Long id) {
    return shipmentService.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Actualizar un envío", description = "Actualiza la información de un envío existente. ADMIN puede actualizar cualquier envío, OPERATOR solo envíos pendientes, y DRIVER solo puede actualizar ciertos campos según las reglas de negocio.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Envío actualizado exitosamente", content = @Content(schema = @Schema(implementation = ShipmentDTO.class))),
      @ApiResponse(responseCode = "400", description = "Datos inválidos"),
      @ApiResponse(responseCode = "404", description = "Envío no encontrado"),
      @ApiResponse(responseCode = "403", description = "No autorizado para actualizar este envío")
  })
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or " +
      "(hasRole('OPERATOR') and @shipmentService.isShipmentPending(#id)) or " +
      "(hasRole('DRIVER') and @shipmentService.canDriverUpdateStatus(#id, #dto))")
  public ResponseEntity<ShipmentDTO> update(
      @Parameter(description = "ID del envío", required = true) @PathVariable Long id,
      @RequestBody ShipmentDTO dto) {
    ShipmentDTO updated = shipmentService.update(id, dto);
    return ResponseEntity.ok(updated);
  }

  @Operation(summary = "Eliminar un envío", description = "Elimina un envío del sistema. ADMIN puede eliminar cualquier envío, OPERATOR solo envíos pendientes.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Envío eliminado exitosamente"),
      @ApiResponse(responseCode = "404", description = "Envío no encontrado"),
      @ApiResponse(responseCode = "403", description = "No autorizado para eliminar este envío")
  })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or (hasRole('OPERATOR') and @shipmentService.isShipmentPending(#id))")
  public ResponseEntity<Void> delete(
      @Parameter(description = "ID del envío", required = true) @PathVariable Long id) {
    shipmentService.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Actualizar estado de un envío", description = "Actualiza el estado de un envío específico. Disponible para ADMIN y DRIVER. Permite agregar observaciones opcionales.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente", content = @Content(schema = @Schema(implementation = ShipmentDTO.class))),
      @ApiResponse(responseCode = "400", description = "Estado inválido"),
      @ApiResponse(responseCode = "404", description = "Envío no encontrado"),
      @ApiResponse(responseCode = "403", description = "No autorizado")
  })
  @PutMapping("/{id}/status")
  @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
  public ResponseEntity<ShipmentDTO> updateStatus(
      @Parameter(description = "ID del envío", required = true) @PathVariable Long id,
      @Parameter(description = "Nuevo estado del envío", required = true) @RequestParam ShipmentStatus status,
      @Parameter(description = "Observaciones opcionales sobre el cambio de estado") @RequestParam(required = false) String observations) {
    ShipmentDTO updated = shipmentService.updateStatus(id, status, observations);
    return ResponseEntity.ok(updated);
  }
}
