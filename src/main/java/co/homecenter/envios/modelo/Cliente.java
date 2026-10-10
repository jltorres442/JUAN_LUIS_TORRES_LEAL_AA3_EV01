package co.homecenter.envios.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Entidad JPA de la tabla {@code Cliente}: la persona que realiza pedidos
 * en Homecenter.
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
@Entity
@Table(name = "Cliente")
public class Cliente extends Usuario {

    private static final long serialVersionUID = 1L;

    /**
     * Llave primaria. {@link GenerationType#IDENTITY} indica que la genera
     * MySQL con {@code AUTO_INCREMENT}; Hibernate la lee después del INSERT.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(name = "direccion", nullable = false, length = 200)
    private String direccion;

    /**
     * Momento en que se creó la cuenta. Lo asigna la base de datos
     * ({@code DEFAULT CURRENT_TIMESTAMP}), por eso Hibernate no lo incluye
     * en los INSERT ni en los UPDATE.
     */
    @Column(name = "fecha_registro", insertable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    /**
     * Crea un cliente que solo contiene la llave primaria. Se usa para
     * indicar una relación (por ejemplo, el cliente de un pedido) sin cargar
     * todos sus datos; el DAO lo reemplaza luego por el registro real.
     *
     * @param idCliente llave primaria, o {@code null}
     * @return referencia con solo el id, o {@code null}
     */
    public static Cliente referencia(Integer idCliente) {
        if (idCliente == null) {
            return null;
        }
        Cliente cliente = new Cliente();
        cliente.setIdCliente(idCliente);
        return cliente;
    }
}
