package co.homecenter.envios.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Entidad JPA de la tabla {@code Envio_Pedido}: el despacho físico de un
 * pedido, gestionado por el personal de logística.
 *
 * <ul>
 *   <li>{@link OneToOne} con {@link Pedido}: cada pedido tiene un único envío.</li>
 *   <li>{@link ManyToOne} con {@link Logistica}: un responsable gestiona muchos envíos.</li>
 * </ul>
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
@Entity
@Table(name = "Envio_Pedido")
public class EnvioPedido implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_envio")
    private Integer idEnvio;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDate fechaEnvio;

    @Column(name = "direccion_entrega", nullable = false, length = 250)
    private String direccionEntrega;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_pedido", nullable = false, unique = true)
    private Pedido pedido;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_logistica", nullable = false)
    private Logistica logistica;

    public Integer getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(Integer idEnvio) {
        this.idEnvio = idEnvio;
    }

    public LocalDate getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDate fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @JsonIgnore
    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    @JsonIgnore
    public Logistica getLogistica() {
        return logistica;
    }

    public void setLogistica(Logistica logistica) {
        this.logistica = logistica;
    }

    // --- Accesos de conveniencia para vistas, formularios y JSON ---

    public Integer getIdPedido() {
        return pedido == null ? null : pedido.getIdPedido();
    }

    public void setIdPedido(Integer idPedido) {
        this.pedido = Pedido.referencia(idPedido);
    }

    public Integer getIdLogistica() {
        return logistica == null ? null : logistica.getIdLogistica();
    }

    public void setIdLogistica(Integer idLogistica) {
        this.logistica = Logistica.referencia(idLogistica);
    }

    /** @return nombre del cliente dueño del pedido enviado */
    public String getNombreCliente() {
        return pedido == null ? null : pedido.getNombreCliente();
    }

    /** @return nombre del responsable de logística */
    public String getNombreLogistica() {
        return logistica == null ? null : logistica.getNombre();
    }

    /**
     * Crea un envío que solo contiene la llave primaria.
     *
     * @param idEnvio llave primaria, o {@code null}
     * @return referencia con solo el id, o {@code null}
     */
    public static EnvioPedido referencia(Integer idEnvio) {
        if (idEnvio == null) {
            return null;
        }
        EnvioPedido envio = new EnvioPedido();
        envio.setIdEnvio(idEnvio);
        return envio;
    }
}
