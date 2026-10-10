/**
 * Estructura común de las pantallas internas: menú lateral azul, barra
 * superior con el usuario y área de contenido.
 * Reproduce en React el diseño de cabecera.jspf (mismas clases CSS).
 *
 * Proyecto: Homecenter Envíos
 * Autores : Juan Luis Torres Leal y Angela María Constain Escobar
 */
import { useEffect, useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useSesion } from '../contexto/SesionContexto.jsx';
import { MODULOS } from '../modulos/definiciones.jsx';

/** Secciones del menú en el orden en que se muestran. */
const SECCIONES = ['Operación', 'Personas'];

/** Construye la clase CSS de un enlace del menú; NavLink indica si está activo. */
const claseEnlace = ({ isActive }) => `menu-lateral__enlace ${isActive ? 'activo' : ''}`;

export default function DisenoAplicacion() {
  const { usuario, cerrarSesion } = useSesion();
  const navegar = useNavigate();
  const ubicacion = useLocation();
  /** Controla si el menú lateral está abierto en pantallas pequeñas. */
  const [menuAbierto, setMenuAbierto] = useState(false);

  // Al cambiar de pantalla se cierra el menú móvil.
  useEffect(() => setMenuAbierto(false), [ubicacion.pathname]);

  /** Cierra la sesión y vuelve al formulario de ingreso. */
  const salir = async () => {
    await cerrarSesion();
    navegar('/login', { replace: true, state: { mensaje: 'Cerró sesión correctamente.', tipo: 'exito' } });
  };

  return (
    <div className="aplicacion">
      {/* ===================== MENÚ LATERAL ===================== */}
      <aside className={`menu-lateral ${menuAbierto ? 'abierto' : ''}`} id="menuLateral">
        <div className="menu-lateral__marca">
          <div className="logo" aria-hidden="true">HC</div>
          <div>
            <strong>HOMECENTER</strong>
            <span>Envíos · React</span>
          </div>
        </div>

        <nav className="menu-lateral__navegacion" aria-label="Menú principal">
          <div className="menu-lateral__seccion">General</div>
          {/* "end" hace que Inicio solo quede activo en la ruta exacta "/" */}
          <NavLink to="/" end className={claseEnlace}>
            <span className="icono">⌂</span> Inicio
          </NavLink>

          {/* Los módulos de administración solo se muestran al personal interno. */}
          {usuario.personalInterno &&
            SECCIONES.map((seccion) => (
              <div key={seccion}>
                <div className="menu-lateral__seccion">{seccion}</div>
                {MODULOS.filter((modulo) => modulo.seccion === seccion).map((modulo) => (
                  <NavLink key={modulo.clave} to={`/${modulo.clave}`} className={claseEnlace}>
                    <span className="icono">{modulo.icono}</span> {modulo.titulo}
                  </NavLink>
                ))}
              </div>
            ))}
        </nav>

        <div className="menu-lateral__pie">
          Sistema de realización y seguimiento de pedidos<br />
          Juan Luis Torres Leal · Angela María Constain Escobar<br />
          SENA · ADSO 3186607<br />
          {/* Enlace a la versión JSP, que sigue disponible en el mismo servidor */}
          <a className="enlace-claro" href="../inicio">Ver versión JSP →</a>
        </div>
      </aside>

      {/* ===================== CONTENIDO ===================== */}
      <div className="principal">
        <header className="barra-superior">
          <h1 className="barra-superior__titulo">
            <button type="button" className="solo-movil" aria-controls="menuLateral"
                    aria-expanded={menuAbierto} aria-label="Abrir menú"
                    onClick={() => setMenuAbierto((abierto) => !abierto)}>☰</button>
            Homecenter Envíos
          </h1>
          <div className="barra-superior__usuario">
            <div className="avatar" aria-hidden="true">{usuario.iniciales}</div>
            <div className="datos">
              <strong>{usuario.nombre}</strong>
              <small>{usuario.rolEtiqueta}</small>
            </div>
            <button type="button" className="boton boton--contorno boton--pequeno" onClick={salir}>Salir</button>
          </div>
        </header>

        <main className="contenido">
          {/* <Outlet /> es el lugar donde React Router dibuja la página de la ruta actual. */}
          <Outlet />
        </main>
      </div>
    </div>
  );
}
