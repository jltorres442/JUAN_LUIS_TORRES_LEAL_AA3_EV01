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
import java.time.LocalDateTime;

/**
 * Entidad JPA de la tabla {@code Notificacion}: un mensaje enviado a un
 * transportista. Un transportista recibe muchas notificaciones ({@link ManyToOne}).
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
@Entity
@Table(name = "Notificacion")
public class Notificacion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Integer idNotificacion;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    /** Hibernate convierte {@link LocalDateTime} en la columna {@code DATETIME}. */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_transportista", nullable = false)
    private Transportista transportista;

    public Integer getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(Integer idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    @JsonIgnore
    public Transportista getTransportista() {
        return transportista;
    }

    public void setTransportista(Transportista transportista) {
        this.transportista = transportista;
    }

    // --- Accesos de conveniencia para vistas, formularios y JSON ---

    public Integer getIdTransportista() {
        return transportista == null ? null : transportista.getIdTransportista();
    }

    public void setIdTransportista(Integer idTransportista) {
        this.transportista = Transportista.referencia(idTransportista);
    }

    /** @return nombre del transportista destinatario */
    public String getNombreTransportista() {
        return transportista == null ? null : transportista.getNombre();
    }
}
