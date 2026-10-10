package co.homecenter.envios.dao;

import co.homecenter.envios.modelo.Cliente;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

/**
 * Acceso a datos de la entidad {@link Cliente} con Hibernate.
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
public class ClienteDao extends BaseDao<Cliente> {

    /**
     * Consulta HQL: se escribe sobre la entidad {@code Cliente} y sus atributos
     * Java ({@code numeroIdentificacion}), no sobre la tabla y sus columnas.
     * Busca por nombre, correo, identificación o teléfono.
     */
    private static final String HQL_LISTAR =
            "from Cliente c "
            + "where c.nombre like :patron escape '!' or c.email like :patron escape '!' "
            + "or c.numeroIdentificacion like :patron escape '!' or c.telefono like :patron escape '!' "
            + "order by c.nombre";

    public ClienteDao() {
        super(Cliente.class);
    }

    @Override
    public List<Cliente> listar(String filtro) throws DaoException {
        return consultarLista(HQL_LISTAR, Map.of("patron", patronBusqueda(filtro)));
    }

    /** Si al editar no se escribió una contraseña nueva, se conserva la actual. */
    @Override
    protected void prepararParaGuardar(Session sesion, Cliente cliente, Cliente existente) {
        conservarContrasena(cliente, existente);
    }
}
