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

/**
 * Entidad JPA de la tabla {@code Transportista}: el conductor que entrega
 * los pedidos en la dirección del cliente.
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
@Entity
@Table(name = "Transportista")
public class Transportista extends Usuario {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transportista")
    private Integer idTransportista;

    /** Número de licencia de conducción. */
    @Column(name = "licencia", nullable = false, length = 50)
    private String licencia;

    /**
     * Envío asignado actualmente (relación N:1, opcional). Si es {@code null}
     * el transportista está disponible. {@code optional = true} permite el
     * valor NULL en la columna {@code id_envio}.
     */
    @ManyToOne(optional = true)
    @JoinColumn(name = "id_envio")
    private EnvioPedido envio;

    public Integer getIdTransportista() {
        return idTransportista;
    }

    public void setIdTransportista(Integer idTransportista) {
        this.idTransportista = idTransportista;
    }

    public String getLicencia() {
        return licencia;
    }

    public void setLicencia(String licencia) {
        this.licencia = licencia;
    }

    /** @return envío asignado (se omite en el JSON; se exponen el id y la dirección) */
    @JsonIgnore
    public EnvioPedido getEnvio() {
        return envio;
    }

    public void setEnvio(EnvioPedido envio) {
        this.envio = envio;
    }

    // ------------------------------------------------------------------
    // Accesos de conveniencia usados por las vistas JSP, los formularios y la API JSON
    // ------------------------------------------------------------------

    /** @return llave del envío asignado, o {@code null} si está disponible */
    public Integer getIdEnvio() {
        return envio == null ? null : envio.getIdEnvio();
    }

    /** @param idEnvio llave del envío a asignar; {@code null} lo deja disponible */
    public void setIdEnvio(Integer idEnvio) {
        this.envio = EnvioPedido.referencia(idEnvio);
    }

    /** @return dirección del envío asignado, o {@code null} */
    public String getDireccionEnvio() {
        return envio == null ? null : envio.getDireccionEntrega();
    }

    /**
     * Crea un transportista que solo contiene la llave primaria.
     *
     * @param idTransportista llave primaria, o {@code null}
     * @return referencia con solo el id, o {@code null}
     */
    public static Transportista referencia(Integer idTransportista) {
        if (idTransportista == null) {
            return null;
        }
        Transportista transportista = new Transportista();
        transportista.setIdTransportista(idTransportista);
        return transportista;
    }
}
