package co.homecenter.envios.dao;

import co.homecenter.envios.configuracion.GestorHibernate;
import co.homecenter.envios.modelo.Cliente;
import co.homecenter.envios.modelo.Logistica;
import co.homecenter.envios.modelo.Rol;
import co.homecenter.envios.modelo.Transportista;
import co.homecenter.envios.modelo.Usuario;
import co.homecenter.envios.modelo.UsuarioSesion;
import co.homecenter.envios.modelo.Vendedor;
import co.homecenter.envios.util.SeguridadContrasena;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Valida las credenciales de inicio de sesión.
 *
 * <p>Como cada rol tiene su propia entidad (y su propia tabla), se busca el
 * correo en las cuatro entidades de usuario en orden. La consulta HQL es la
 * misma para todas porque comparten los atributos heredados de {@link Usuario}.</p>
 *
 * @author Juan Luis Torres Leal
 * @author Angela María Constain Escobar
 */
public class AutenticacionDao {

    /** Entidad de usuario asociada a cada rol, en el orden en que se buscan. */
    private static final Map<Class<? extends Usuario>, Rol> ENTIDADES_POR_ROL = new LinkedHashMap<>();

    static {
        ENTIDADES_POR_ROL.put(Cliente.class, Rol.CLIENTE);
        ENTIDADES_POR_ROL.put(Vendedor.class, Rol.VENDEDOR);
        ENTIDADES_POR_ROL.put(Logistica.class, Rol.LOGISTICA);
        ENTIDADES_POR_ROL.put(Transportista.class, Rol.TRANSPORTISTA);
    }

    /**
     * Verifica el correo y la contraseña de un usuario.
     *
     * @param email           correo digitado (se compara en minúsculas)
     * @param contrasenaPlana contraseña digitada
     * @return datos del usuario si las credenciales son válidas; vacío en caso contrario
     * @throws DaoException si falla la consulta
     */
    public Optional<UsuarioSesion> autenticar(String email, String contrasenaPlana) throws DaoException {
        String emailNormalizado = email.trim().toLowerCase();

        return BaseDao.enSesion("validar las credenciales", sesion -> {
            for (Map.Entry<Class<? extends Usuario>, Rol> entrada : ENTIDADES_POR_ROL.entrySet()) {
                Class<? extends Usuario> clase = entrada.getKey();
                // getSimpleName() da el nombre de la entidad: "Cliente", "Vendedor", etc.
                Usuario usuario = sesion
                        .createSelectionQuery("from " + clase.getSimpleName() + " u where u.email = :email", clase)
                        .setParameter("email", emailNormalizado)
                        .uniqueResult();

                if (usuario != null) {
                    // El correo existe en este rol: el resultado depende solo de la contraseña.
                    if (!SeguridadContrasena.verificar(contrasenaPlana, usuario.getContrasena())) {
                        return Optional.empty();
                    }
                    Integer id = (Integer) GestorHibernate.obtenerFabrica()
                            .getPersistenceUnitUtil().getIdentifier(usuario);
                    return Optional.of(new UsuarioSesion(id, usuario.getNombre(), usuario.getEmail(),
                            entrada.getValue()));
                }
            }
            return Optional.empty();
        });
    }
}
