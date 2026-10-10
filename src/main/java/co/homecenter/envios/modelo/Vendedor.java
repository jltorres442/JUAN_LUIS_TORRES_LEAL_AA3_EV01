package co.homecenter.envios.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA de la tabla {@code Vendedor}: el asesor comercial que
 * registra los pedidos de los clientes.
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
@Entity
@Table(name = "Vendedor")
public class Vendedor extends Usuario {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vendedor")
    private Integer idVendedor;

    /**
     * Indica si el vendedor está habilitado para registrar pedidos.
     * Hibernate convierte el {@code boolean} de Java en el {@code TINYINT(1)} de MySQL.
     */
    @Column(name = "registro_pedido", nullable = false)
    private boolean registroPedido = true;

    public Integer getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(Integer idVendedor) {
        this.idVendedor = idVendedor;
    }

    public boolean isRegistroPedido() {
        return registroPedido;
    }

    public void setRegistroPedido(boolean registroPedido) {
        this.registroPedido = registroPedido;
    }

    /**
     * Crea un vendedor que solo contiene la llave primaria (ver {@link Cliente#referencia(Integer)}).
     *
     * @param idVendedor llave primaria, o {@code null}
     * @return referencia con solo el id, o {@code null}
     */
    public static Vendedor referencia(Integer idVendedor) {
        if (idVendedor == null) {
            return null;
        }
        Vendedor vendedor = new Vendedor();
        vendedor.setIdVendedor(idVendedor);
        return vendedor;
    }
}
