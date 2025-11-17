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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.udea.CourierSync.services.UserService;
import com.udea.CourierSync.DTO.UserDTO;
import com.udea.CourierSync.DTO.SignUpRequest;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Users", description = "API para gestión de usuarios del sistema (solo ADMIN)")
public class UserController {

  @Autowired
  private UserService userService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Operation(summary = "Crear un nuevo usuario", description = "Crea un nuevo usuario en el sistema. La contraseña se codifica automáticamente antes de almacenarse. Solo disponible para ADMIN.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente", content = @Content(schema = @Schema(implementation = UserDTO.class))),
      @ApiResponse(responseCode = "400", description = "Datos inválidos"),
      @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN")
  })
  @PostMapping("/new")
  public ResponseEntity<UserDTO> createNewUser(@RequestBody SignUpRequest signUpRequest) {
    UserDTO userDTO = signUpRequest.toUserDTO();
    userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
    UserDTO created = userService.create(userDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Operation(summary = "Listar todos los usuarios", description = "Obtiene una lista de todos los usuarios registrados en el sistema. Solo disponible para ADMIN.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente", content = @Content(schema = @Schema(implementation = UserDTO.class))),
      @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN")
  })
  @GetMapping
  public List<UserDTO> list() {
    return userService.findAll();
  }

  @Operation(summary = "Obtener un usuario por ID", description = "Obtiene los detalles de un usuario específico mediante su ID. Solo disponible para ADMIN.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(schema = @Schema(implementation = UserDTO.class))),
      @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
      @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN")
  })
  @GetMapping("/{id}")
  public ResponseEntity<UserDTO> get(
      @Parameter(description = "ID del usuario", required = true) @PathVariable Long id) {
    return userService.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Actualizar un usuario", description = "Actualiza la información de un usuario existente. Si se proporciona una nueva contraseña, se codifica automáticamente. Solo disponible para ADMIN.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente", content = @Content(schema = @Schema(implementation = UserDTO.class))),
      @ApiResponse(responseCode = "400", description = "Datos inválidos"),
      @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
      @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN")
  })
  @PutMapping("/{id}")
  public ResponseEntity<UserDTO> update(
      @Parameter(description = "ID del usuario", required = true) @PathVariable Long id,
      @RequestBody UserDTO dto) {
    // Si viene una contraseña nueva, codificarla antes de actualizar
    if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
      dto.setPassword(passwordEncoder.encode(dto.getPassword()));
    }
    UserDTO updated = userService.update(id, dto);
    return ResponseEntity.ok(updated);
  }

  @Operation(summary = "Eliminar un usuario", description = "Elimina un usuario del sistema mediante su ID. Solo disponible para ADMIN.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
      @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
      @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @Parameter(description = "ID del usuario", required = true) @PathVariable Long id) {
    userService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
