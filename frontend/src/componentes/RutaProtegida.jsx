/**
 * Protege las rutas que requieren sesión (y opcionalmente un rol interno).
 *
 * Proyecto: Homecenter Envíos
 * Autores : Juan Luis Torres Leal y Angela María Constain Escobar
 *
 * Es el equivalente en React de AutenticacionFiltro.java. OJO: esta
 * protección solo mejora la experiencia de uso; la seguridad real está en el
 * servidor, que responde 401/403 si alguien intenta saltársela.
 */
import { Navigate, useLocation } from 'react-router-dom';
import { useSesion } from '../contexto/SesionContexto.jsx';
import Cargando from './Cargando.jsx';

/**
 * @param {{ children: React.ReactNode, soloPersonalInterno?: boolean }} props
 */
export default function RutaProtegida({ children, soloPersonalInterno = false }) {
  const { usuario, cargando } = useSesion();
  const ubicacion = useLocation();

  if (cargando) {
    return <Cargando texto="Verificando sesión…" />;
  }
  if (!usuario) {
    // Se recuerda la ruta solicitada para volver a ella después del inicio de sesión.
    return <Navigate to="/login" replace state={{ desde: ubicacion.pathname }} />;
  }
  if (soloPersonalInterno && !usuario.personalInterno) {
    return <Navigate to="/" replace state={{ mensaje: 'Su rol no tiene permiso para esa sección.', tipo: 'error' }} />;
  }
  return children;
}
