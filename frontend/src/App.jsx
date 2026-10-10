/**
 * Componente raíz: define las rutas (pantallas) de la aplicación.
 *
 * Proyecto: Homecenter Envíos
 * Autores : Juan Luis Torres Leal y Angela María Constain Escobar
 *
 * MAPA DE RUTAS (después del # en la URL)
 *   /login                 Inicio de sesión
 *   /                      Panel de inicio            (requiere sesión)
 *   /:modulo               Listado: consultar/eliminar (personal interno)
 *   /:modulo/nuevo         Formulario: insertar        (personal interno)
 *   /:modulo/:id/editar    Formulario: actualizar      (personal interno)
 *
 * Se usa HashRouter (URLs como /app/#/pedidos) porque Tomcat solo conoce el
 * archivo /app/index.html: todo lo que va después del # lo resuelve React en
 * el navegador, así que recargar la página nunca produce un 404.
 */
import { HashRouter, Route, Routes } from 'react-router-dom';
import { ProveedorSesion } from './contexto/SesionContexto.jsx';
import DisenoAplicacion from './componentes/DisenoAplicacion.jsx';
import RutaProtegida from './componentes/RutaProtegida.jsx';
import PaginaLogin from './paginas/PaginaLogin.jsx';
import PaginaInicio from './paginas/PaginaInicio.jsx';
import PaginaListado from './paginas/PaginaListado.jsx';
import PaginaFormulario from './paginas/PaginaFormulario.jsx';
import PaginaNoEncontrada from './paginas/PaginaNoEncontrada.jsx';

export default function App() {
  return (
    <ProveedorSesion>
      <HashRouter>
        <Routes>
          <Route path="/login" element={<PaginaLogin />} />

          {/* Rutas internas: comparten el diseño con menú lateral (rutas anidadas). */}
          <Route
            path="/"
            element={
              <RutaProtegida>
                <DisenoAplicacion />
              </RutaProtegida>
            }
          >
            <Route index element={<PaginaInicio />} />
            <Route
              path=":modulo"
              element={<RutaProtegida soloPersonalInterno><PaginaListado /></RutaProtegida>}
            />
            <Route
              path=":modulo/nuevo"
              element={<RutaProtegida soloPersonalInterno><PaginaFormulario /></RutaProtegida>}
            />
            <Route
              path=":modulo/:id/editar"
              element={<RutaProtegida soloPersonalInterno><PaginaFormulario /></RutaProtegida>}
            />
            <Route path="*" element={<PaginaNoEncontrada />} />
          </Route>
        </Routes>
      </HashRouter>
    </ProveedorSesion>
  );
}
