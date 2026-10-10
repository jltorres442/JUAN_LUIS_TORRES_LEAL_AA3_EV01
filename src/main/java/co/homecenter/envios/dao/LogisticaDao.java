package co.homecenter.envios.dao;

import co.homecenter.envios.modelo.Logistica;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

/**
 * Acceso a datos de la entidad {@link Logistica} con Hibernate.
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
public class LogisticaDao extends BaseDao<Logistica> {

    private static final String HQL_LISTAR =
            "from Logistica l "
            + "where l.nombre like :patron escape '!' or l.email like :patron escape '!' "
            + "or l.numeroIdentificacion like :patron escape '!' or l.telefono like :patron escape '!' "
            + "order by l.nombre";

    public LogisticaDao() {
        super(Logistica.class);
    }

    @Override
    public List<Logistica> listar(String filtro) throws DaoException {
        return consultarLista(HQL_LISTAR, Map.of("patron", patronBusqueda(filtro)));
    }

    @Override
    protected void prepararParaGuardar(Session sesion, Logistica logistica, Logistica existente) {
        conservarContrasena(logistica, existente);
    }
}
