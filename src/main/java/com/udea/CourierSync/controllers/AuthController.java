package com.udea.CourierSync.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.udea.CourierSync.DTO.LoginRequest;
import com.udea.CourierSync.security.JwtTokenProvider;
import com.udea.CourierSync.security.UserPrincipal;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private JwtTokenProvider tokenProvider;

  @Operation(summary = "Login user", description = "Authenticates a user and returns a JWT token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully authenticated", content = @Content(schema = @Schema(implementation = Map.class))),
      @ApiResponse(responseCode = "401", description = "Invalid credentials"),
  })
  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              loginRequest.getEmail(),
              loginRequest.getPassword()));

      SecurityContextHolder.getContext().setAuthentication(authentication);

      String jwt = tokenProvider.generateToken(authentication);
      
      // Obtener información del usuario autenticado
      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
      
      // Debug: Verificar authorities
      System.out.println("🔍 AuthController - Authorities: " + userPrincipal.getAuthorities());
      
      String role = userPrincipal.getAuthorities().stream()
          .findFirst()
          .map(authority -> {
            String authorityStr = authority.getAuthority();
            System.out.println("🔍 AuthController - Authority encontrada: " + authorityStr);
            return authorityStr.replace("ROLE_", "");
          })
          .orElseThrow(() -> {
            System.err.println("❌ AuthController - Usuario sin rol asignado. Email: " + userPrincipal.getUsername());
            return new RuntimeException("Usuario sin rol asignado");
          });
      
      // Validar que el rol sea válido
      if (!role.matches("ADMIN|OPERATOR|DRIVER")) {
          System.err.println("❌ AuthController - Rol inválido: " + role);
          throw new RuntimeException("Rol de usuario inválido: " + role);
      }
      
      // Debug: Log para verificar qué se está devolviendo
      System.out.println("🔍 AuthController - Login exitoso:");
      System.out.println("  - Email: " + userPrincipal.getUsername());
      System.out.println("  - Nombre: " + userPrincipal.getName());
      System.out.println("  - Rol: " + role);
      
      Map<String, Object> response = Map.of(
          "accessToken", jwt,
          "role", role,
          "name", userPrincipal.getName() != null ? userPrincipal.getName() : "",
          "email", userPrincipal.getUsername()
      );
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      System.err.println("❌ AuthController - Error en login:");
      System.err.println("  - Mensaje: " + e.getMessage());
      System.err.println("  - Stack trace:");
      e.printStackTrace();
      return ResponseEntity.status(401).body(Map.of(
          "error", "Error de autenticación",
          "message", e.getMessage() != null ? e.getMessage() : "Error desconocido"
      ));
    }
  }
}