package co.homecenter.envios.dao;

import co.homecenter.envios.modelo.Vendedor;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

/**
 * Acceso a datos de la entidad {@link Vendedor} con Hibernate.
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
public class VendedorDao extends BaseDao<Vendedor> {

    private static final String HQL_LISTAR =
            "from Vendedor v "
            + "where v.nombre like :patron escape '!' or v.email like :patron escape '!' "
            + "or v.numeroIdentificacion like :patron escape '!' or v.telefono like :patron escape '!' "
            + "order by v.nombre";

    public VendedorDao() {
        super(Vendedor.class);
    }

    @Override
    public List<Vendedor> listar(String filtro) throws DaoException {
        return consultarLista(HQL_LISTAR, Map.of("patron", patronBusqueda(filtro)));
    }

    @Override
    protected void prepararParaGuardar(Session sesion, Vendedor vendedor, Vendedor existente) {
        conservarContrasena(vendedor, existente);
    }
}
