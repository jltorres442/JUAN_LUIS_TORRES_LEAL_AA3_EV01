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
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad JPA de la tabla {@code Pedido}.
 *
 * <p><strong>Relaciones:</strong> un cliente realiza muchos pedidos y un
 * vendedor registra muchos pedidos, por eso ambas son {@link ManyToOne}.
 * Hibernate genera automáticamente el {@code JOIN} para traer el cliente y
 * el vendedor junto con cada pedido.</p>
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
@Entity
@Table(name = "Pedido")
public class Pedido implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer idPedido;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    /** {@code precision = 12, scale = 2} corresponde a {@code DECIMAL(12,2)}. */
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;

    /** Cliente que realiza el pedido (columna {@code id_cliente}). */
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    /** Vendedor que registra el pedido (columna {@code id_vendedor}). */
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_vendedor", nullable = false)
    private Vendedor vendedor;

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @JsonIgnore
    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    @JsonIgnore
    public Vendedor getVendedor() {
        return vendedor;
    }

    public void setVendedor(Vendedor vendedor) {
        this.vendedor = vendedor;
    }

    // ------------------------------------------------------------------
    // Accesos de conveniencia: exponen la relación como datos planos
    // (id y nombre) para las vistas JSP, los formularios y el JSON de React.
    // ------------------------------------------------------------------

    public Integer getIdCliente() {
        return cliente == null ? null : cliente.getIdCliente();
    }

    /** @param idCliente cliente seleccionado en el formulario */
    public void setIdCliente(Integer idCliente) {
        this.cliente = Cliente.referencia(idCliente);
    }

    public Integer getIdVendedor() {
        return vendedor == null ? null : vendedor.getIdVendedor();
    }

    /** @param idVendedor vendedor seleccionado en el formulario */
    public void setIdVendedor(Integer idVendedor) {
        this.vendedor = Vendedor.referencia(idVendedor);
    }

    public String getNombreCliente() {
        return cliente == null ? null : cliente.getNombre();
    }

    public String getNombreVendedor() {
        return vendedor == null ? null : vendedor.getNombre();
    }

    /**
     * Crea un pedido que solo contiene la llave primaria.
     *
     * @param idPedido llave primaria, o {@code null}
     * @return referencia con solo el id, o {@code null}
     */
    public static Pedido referencia(Integer idPedido) {
        if (idPedido == null) {
            return null;
        }
        Pedido pedido = new Pedido();
        pedido.setIdPedido(idPedido);
        return pedido;
    }
}
