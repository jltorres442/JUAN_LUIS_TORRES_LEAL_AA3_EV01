package co.homecenter.envios.configuracion;

import co.homecenter.envios.util.CatalogoEstados;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Escucha el arranque y la detención de la aplicación en Tomcat.
 *
 * <ul>
 *   <li><strong>Al arrancar:</strong> inicializa Hibernate y publica en el
 *       ámbito de aplicación ({@code applicationScope}) las listas de estados,
 *       de modo que cualquier JSP pueda usarlas con EL, p. ej. {@code ${estadosPedido}}.</li>
 *   <li><strong>Al detenerse:</strong> cierra Hibernate y su pool de conexiones.</li>
 * </ul>
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
@WebListener
public class InicializadorAplicacion implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        ServletContext contexto = evento.getServletContext();
        contexto.setAttribute("estadosPedido", CatalogoEstados.ESTADOS_PEDIDO);
        contexto.setAttribute("estadosEnvio", CatalogoEstados.ESTADOS_ENVIO);
        contexto.setAttribute("estadosIncidencia", CatalogoEstados.ESTADOS_INCIDENCIA);

        try {
            GestorHibernate.iniciar();
            contexto.log("Homecenter Envíos: Hibernate iniciado correctamente.");
        } catch (RuntimeException excepcion) {
            // La aplicación sigue desplegada; GestorHibernate reintentará en la primera consulta.
            contexto.log("Homecenter Envíos: no fue posible iniciar Hibernate.", excepcion);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        GestorHibernate.cerrar();
        evento.getServletContext().log("Homecenter Envíos: Hibernate cerrado.");
    }
}
