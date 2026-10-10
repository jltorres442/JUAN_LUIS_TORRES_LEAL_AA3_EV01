package co.homecenter.envios.dao;

import co.homecenter.envios.modelo.EnvioPedido;
import co.homecenter.envios.modelo.Transportista;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

/**
 * Acceso a datos de la entidad {@link Transportista} con Hibernate.
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
public class TransportistaDao extends BaseDao<Transportista> {

    /**
     * {@code left join fetch} trae el envío asignado en la misma consulta;
     * es "left" porque un transportista puede no tener envío (relación opcional).
     */
    private static final String HQL_LISTAR =
            "select t from Transportista t left join fetch t.envio "
            + "where t.nombre like :patron escape '!' or t.email like :patron escape '!' "
            + "or t.numeroIdentificacion like :patron escape '!' or t.licencia like :patron escape '!' "
            + "order by t.nombre";

    public TransportistaDao() {
        super(Transportista.class);
    }

    @Override
    public List<Transportista> listar(String filtro) throws DaoException {
        return consultarLista(HQL_LISTAR, Map.of("patron", patronBusqueda(filtro)));
    }

    /**
     * Conserva la contraseña si no se cambió y reemplaza la referencia del
     * envío (solo con id) por el envío real.
     */
    @Override
    protected void prepararParaGuardar(Session sesion, Transportista transportista, Transportista existente)
            throws DaoException {
        conservarContrasena(transportista, existente);
        transportista.setEnvio(resolverReferencia(sesion, EnvioPedido.class, transportista.getIdEnvio(),
                "El envío seleccionado ya no existe."));
    }
}
