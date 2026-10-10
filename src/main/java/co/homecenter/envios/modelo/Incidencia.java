package co.homecenter.envios.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * Entidad JPA de la tabla {@code Incidencia}: un problema reportado durante
 * la entrega. Un envío puede tener muchas incidencias ({@link ManyToOne}).
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
@Entity
@Table(name = "Incidencia")
public class Incidencia implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incidencia")
    private Integer idIncidencia;

    /** {@code columnDefinition} indica que en MySQL la columna es de tipo TEXT. */
    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_envio", nullable = false)
    private EnvioPedido envio;

    public Integer getIdIncidencia() {
        return idIncidencia;
    }

    public void setIdIncidencia(Integer idIncidencia) {
        this.idIncidencia = idIncidencia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @JsonIgnore
    public EnvioPedido getEnvio() {
        return envio;
    }

    public void setEnvio(EnvioPedido envio) {
        this.envio = envio;
    }

    // --- Accesos de conveniencia para vistas, formularios y JSON ---

    public Integer getIdEnvio() {
        return envio == null ? null : envio.getIdEnvio();
    }

    public void setIdEnvio(Integer idEnvio) {
        this.envio = EnvioPedido.referencia(idEnvio);
    }

    /** @return dirección del envío afectado */
    public String getDireccionEnvio() {
        return envio == null ? null : envio.getDireccionEntrega();
    }
}
