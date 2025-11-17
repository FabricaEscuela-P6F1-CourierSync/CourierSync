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

import com.udea.CourierSync.services.ClientService;
import com.udea.CourierSync.DTO.ClientDTO;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@Tag(name = "Clients", description = "API para gestión de clientes")
public class ClientController {

  @Autowired
  private ClientService clientService;

  @Operation(summary = "Crear un nuevo cliente", description = "Crea un nuevo cliente en el sistema con la información proporcionada.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente", content = @Content(schema = @Schema(implementation = ClientDTO.class))),
      @ApiResponse(responseCode = "400", description = "Datos inválidos")
  })
  @CrossOrigin
  @PostMapping
  public ResponseEntity<ClientDTO> create(@RequestBody ClientDTO dto) {
    ClientDTO created = clientService.createClient(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Operation(summary = "Listar todos los clientes", description = "Obtiene una lista de todos los clientes registrados en el sistema.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida exitosamente", content = @Content(schema = @Schema(implementation = ClientDTO.class)))
  })
  @GetMapping
  public List<ClientDTO> list() {
    return clientService.findAll();
  }

  @Operation(summary = "Obtener un cliente por ID", description = "Obtiene los detalles de un cliente específico mediante su ID.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Cliente encontrado", content = @Content(schema = @Schema(implementation = ClientDTO.class))),
      @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
  })
  @GetMapping("/{id}")
  public ResponseEntity<ClientDTO> get(
      @Parameter(description = "ID del cliente", required = true) @PathVariable Long id) {
    return clientService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Actualizar un cliente", description = "Actualiza la información de un cliente existente.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente", content = @Content(schema = @Schema(implementation = ClientDTO.class))),
      @ApiResponse(responseCode = "400", description = "Datos inválidos"),
      @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
  })
  @CrossOrigin
  @PutMapping("/{id}")
  public ResponseEntity<ClientDTO> update(
      @Parameter(description = "ID del cliente", required = true) @PathVariable Long id,
      @RequestBody ClientDTO dto) {
    ClientDTO updated = clientService.update(id, dto);
    return ResponseEntity.ok(updated);
  }

  @Operation(summary = "Eliminar un cliente", description = "Elimina un cliente del sistema mediante su ID.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Cliente eliminado exitosamente"),
      @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @Parameter(description = "ID del cliente", required = true) @PathVariable Long id) {
    clientService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
