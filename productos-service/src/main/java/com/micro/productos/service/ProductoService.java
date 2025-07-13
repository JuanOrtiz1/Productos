package com.micro.productos.service;

import com.micro.productos.config.ProductoServiceProperties;
import com.micro.productos.entity.Producto;
import com.micro.productos.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.annotation.PostConstruct;

@Service
public class ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

    @Autowired
    private ProductoRepository repository;

    @Autowired
    private ProductoServiceProperties properties;

    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        this.executorService = Executors.newCachedThreadPool();
    }

    @Retryable(
        value = {TransientDataAccessException.class, TimeoutException.class},
        maxAttemptsExpression = "#{@productoServiceProperties.retry.maxAttempts}",
        backoff = @Backoff(delayExpression = "#{@productoServiceProperties.retry.delay}")
    )
    public Producto crear(Producto producto) throws Exception {
        return ejecutarConTimeout(() -> {
            Producto creado = repository.save(producto);
            log.info("Producto creado exitosamente - ID: {}, nombre: {}", creado.getId(), creado.getNombre());
            return creado;
        });
    }

    public Producto obtener(Long id) throws Exception {
        return ejecutarConTimeout(() -> {
            log.info("Buscando producto por ID: {}", id);
            return repository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Producto no encontrado - ID: {}", id);
                        return new NoSuchElementException("Producto no encontrado");
                    });
        });
    }

    public Producto actualizar(Long id, Producto producto) throws Exception {
        return ejecutarConTimeout(() -> {
            log.info("Actualizando producto - ID: {}", id);
            Producto existente = obtener(id);
            existente.setNombre(producto.getNombre());
            existente.setPrecio(producto.getPrecio());
            Producto actualizado = repository.save(existente);
            log.info("Producto actualizado - ID: {}, nuevo nombre: {}, nuevo precio: {}",
                    actualizado.getId(), actualizado.getNombre(), actualizado.getPrecio());
            return actualizado;
        });
    }

    public boolean eliminar(Long id) throws Exception {
        return ejecutarConTimeout(() -> {
            log.info("Intentando eliminar producto - ID: {}", id);
            Optional<Producto> producto = repository.findById(id);
            if (producto.isPresent()) {
                repository.deleteById(id);
                log.info("Producto eliminado exitosamente - ID: {}", id);
                return true;
            } else {
                log.warn("Producto no encontrado para eliminar - ID: {}", id);
                return false;
            }
        });
    }

    public Page<Producto> listar(Pageable pageable) throws Exception {
        return ejecutarConTimeout(() -> {
            log.info("Listando productos - página: {}, tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());
            Page<Producto> productos = repository.findAll(pageable);
            log.info("Se encontraron {} productos", productos.getTotalElements());
            return productos;
        });
    }

    // Método genérico para ejecutar con timeout
    private <T> T ejecutarConTimeout(Callable<T> task) throws Exception {
        Future<T> future = executorService.submit(task);
        try {
            return future.get(properties.getTimeout().getMilliseconds(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.error("Operación excedió el tiempo máximo de espera");
            throw e;
        }
    }

    // Método de recuperación si fallan los reintentos
    @Recover
    public Producto recuperar(Exception e, Producto producto) {
        log.error("Fallo luego de reintentos - Error: {}", e.getMessage());
        throw new RuntimeException("Error persistente al procesar el producto", e);
    }
}
