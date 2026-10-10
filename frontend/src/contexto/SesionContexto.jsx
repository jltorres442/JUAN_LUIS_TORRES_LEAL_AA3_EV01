/**
 * Contexto de sesión: comparte el usuario autenticado con toda la aplicación.
 *
 * Proyecto: Homecenter Envíos
 * Autores : Juan Luis Torres Leal y Angela María Constain Escobar
 *
 * ¿POR QUÉ UN CONTEXTO?
 * Muchos componentes necesitan saber quién inició sesión (el menú, la barra
 * superior, las rutas protegidas). En lugar de pasar el usuario de componente
 * en componente ("prop drilling"), React Context lo publica una sola vez y
 * cualquier componente lo lee con el hook useSesion().
 */
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { api, configurarSesionExpirada, establecerTokenCsrf } from '../api/clienteApi.js';

/** Contexto con valor inicial nulo; se llena en <ProveedorSesion>. */
const SesionContexto = createContext(null);

/**
 * Componente que envuelve la aplicación y administra el estado de la sesión.
 *
 * @param {{ children: React.ReactNode }} props
 */
export function ProveedorSesion({ children }) {
  /** Usuario autenticado, o null si no hay sesión. */
  const [usuario, setUsuario] = useState(null);
  /** Listas de estados (pedido, envío, incidencia) enviadas por el servidor. */
  const [estados, setEstados] = useState({});
  /** true mientras se consulta la sesión por primera vez. */
  const [cargando, setCargando] = useState(true);
  /** Error de conexión con el backend, si lo hay. */
  const [errorConexion, setErrorConexion] = useState(null);

  /**
   * Aplica la respuesta de /api/sesion al estado de React.
   * useCallback conserva la misma función entre renderizados.
   */
  const aplicarEstadoSesion = useCallback((estadoSesion) => {
    establecerTokenCsrf(estadoSesion.tokenCsrf);
    setUsuario(estadoSesion.autenticado ? estadoSesion.usuario : null);
    setEstados(estadoSesion.estados ?? {});
  }, []);

  /** Vuelve a consultar la sesión al servidor (obtiene un token CSRF fresco). */
  const refrescarSesion = useCallback(async () => {
    try {
      aplicarEstadoSesion(await api.consultarSesion());
      setErrorConexion(null);
    } catch (error) {
      setErrorConexion(error.message || 'No fue posible conectar con el servidor.');
    } finally {
      setCargando(false);
    }
  }, [aplicarEstadoSesion]);

  // useEffect con [] se ejecuta una sola vez, cuando el componente aparece en pantalla.
  useEffect(() => {
    refrescarSesion();
    // Si cualquier petición responde 401, la sesión expiró: se limpia el usuario.
    configurarSesionExpirada(() => {
      setUsuario(null);
      refrescarSesion();
    });
  }, [refrescarSesion]);

  /**
   * Inicia sesión con correo y contraseña.
   * @returns {Promise<void>} lanza ErrorApi si las credenciales son incorrectas
   */
  const iniciarSesion = useCallback(
    async (email, contrasena) => {
      aplicarEstadoSesion(await api.iniciarSesion(email, contrasena));
    },
    [aplicarEstadoSesion],
  );

  /** Cierra la sesión en el servidor y en la interfaz. */
  const cerrarSesion = useCallback(async () => {
    try {
      aplicarEstadoSesion(await api.cerrarSesion());
    } catch {
      // Aunque falle la red, en la interfaz se considera cerrada la sesión.
      setUsuario(null);
    }
  }, [aplicarEstadoSesion]);

  // useMemo evita crear un objeto nuevo en cada renderizado (y re-renderizar a todos los consumidores).
  const valor = useMemo(
    () => ({ usuario, estados, cargando, errorConexion, iniciarSesion, cerrarSesion, refrescarSesion }),
    [usuario, estados, cargando, errorConexion, iniciarSesion, cerrarSesion, refrescarSesion],
  );

  return <SesionContexto.Provider value={valor}>{children}</SesionContexto.Provider>;
}

/**
 * Hook para leer la sesión desde cualquier componente.
 *
 * @example const { usuario, cerrarSesion } = useSesion();
 */
export function useSesion() {
  const contexto = useContext(SesionContexto);
  if (!contexto) {
    throw new Error('useSesion() debe usarse dentro de <ProveedorSesion>.');
  }
  return contexto;
}
