package com.micro.productos.controller;

import com.micro.productos.entity.Producto;
import com.micro.productos.service.ProductoService;

import io.swagger.v3.oas.annotations.Operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    @Autowired
    private ProductoService service;

    @Value("${api.key}")
    private String apiKey;

    private void validarApiKey(String headerKey) {
        if (!apiKey.equals(headerKey)) {
            log.warn("Intento de acceso con API Key inválida: {}", headerKey);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas, intente de nuevo");
        }
    }
    
    @Operation(summary = "Permite realizar la creación de un producto", description = "Crea un producto en base de datos de acuerdo al request envíado.")
    @PostMapping
    public ResponseEntity<?> crear(@RequestHeader("x-api-key") String key, @RequestBody Producto p) {
        try {
            validarApiKey(key);
            log.info("Creando nuevo producto: {}", p.getNombre());
            Producto creado = service.crear(p);
            return ResponseEntity.status(HttpStatus.CREATED).body(jsonApiWrapper("productos", creado.getId(), creado));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getReason());
        } catch (Exception e) {
            log.error("Error al crear producto", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrió un error al crear el producto.");
        }
    }

    @Operation(summary = "Permite obtener el contenido de un producto", description = "Realiza la busqueda de un producto en base de datos de acuerdo al ID envíado.")
    @GetMapping("/obtener/{id}")
    public ResponseEntity<?> obtener(@RequestHeader("x-api-key") String key, @PathVariable("id") Long id) {
        try {
            validarApiKey(key);
            log.info("Obteniendo producto con ID: {}", id);
            Producto p = service.obtener(id);
            return ResponseEntity.ok(jsonApiWrapper("productos", p.getId(), p));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getReason());
        } catch (NoSuchElementException e) {
            log.warn("Producto con ID {} no encontrado", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado.");
        } catch (Exception e) {
            log.error("Error al obtener producto con ID {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrió un error al obtener el producto.");
        }
    }
    
    @Operation(summary = "Permite actualizar el contenido de un producto", description = "Realiza la busqueda de un producto en base de datos de acuerdo al ID envíado y realiza la actualización de los datos solicitados")
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<?> actualizar(@RequestHeader("x-api-key") String key, @PathVariable("id") Long id, @RequestBody Producto p) {
        try {
            validarApiKey(key);
            log.info("Actualizando producto con ID: {}", id);
            Producto actualizado = service.actualizar(id, p);
            return ResponseEntity.ok(jsonApiWrapper("productos", actualizado.getId(), actualizado));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getReason());
        } catch (NoSuchElementException e) {
            log.warn("Producto con ID {} no encontrado para actualizar", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado.");
        } catch (Exception e) {
            log.error("Error al actualizar producto con ID {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrió un error al actualizar el producto.");
        }
    }
    
    @Operation(summary = "Permite eliminar un producto", description = "Realiza la busqueda de un producto en base de datos de acuerdo al ID envíado y realiza la eliminación del producto solicitado")
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminar(@RequestHeader("x-api-key") String key, @PathVariable("id") Long id) {
        try {
            validarApiKey(key);
            log.info("Intentando eliminar producto con ID: {}", id);

            boolean eliminado = service.eliminar(id);

            if (eliminado) {
                log.info("Producto con ID {} eliminado exitosamente", id);
                return ResponseEntity.ok("Producto eliminado correctamente.");
            } else {
                log.warn("Producto con ID {} no encontrado para eliminar", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado.");
            }

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getReason());
        } catch (Exception e) {
            log.error("Error inesperado al eliminar producto con ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrió un error al eliminar el producto.");
        }
    }
    
    @Operation(summary = "Listar todos los productos", description = "Devuelve una lista paginada de productos.")
    @GetMapping
    public ResponseEntity<?> listar(@RequestHeader("x-api-key") String key,
                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                    @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            validarApiKey(key);
            log.info("Listando productos - página: {}, tamaño: {}", page, size);
            Page<Producto> productos = service.listar(PageRequest.of(page, size));

            List<Map<String, Object>> data = productos.stream()
                    .map(p -> (Map<String, Object>) jsonApiWrapper("productos", p.getId(), p).get("data"))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("data", data));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getReason());
        } catch (Exception e) {
            log.error("Error al listar productos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrió un error al listar los productos.");
        }
    }

    private Map<String, Object> jsonApiWrapper(String type, Long id, Producto p) {
        return Map.of(
                "data", Map.of(
                        "type", type,
                        "id", id,
                        "attributes", Map.of(
                                "nombre", p.getNombre(),
                                "precio", p.getPrecio()
                        )
                )
        );
    }
}
