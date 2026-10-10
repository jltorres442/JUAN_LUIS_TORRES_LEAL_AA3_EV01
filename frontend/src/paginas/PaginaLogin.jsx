/**
 * Pantalla de inicio de sesión (versión React de login.jsp).
 *
 * Proyecto: Homecenter Envíos
 * Autores : Juan Luis Torres Leal y Angela María Constain Escobar
 *
 * Es un "formulario controlado": el valor de cada campo vive en el estado de
 * React (useState) y cada tecla actualiza ese estado mediante onChange.
 */
import { useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useSesion } from '../contexto/SesionContexto.jsx';
import Alerta from '../componentes/Alerta.jsx';
import Cargando from '../componentes/Cargando.jsx';

export default function PaginaLogin() {
  const { usuario, cargando, errorConexion, iniciarSesion } = useSesion();
  const navegar = useNavigate();
  const ubicacion = useLocation();

  const [email, setEmail] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [contrasenaVisible, setContrasenaVisible] = useState(false);
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  if (cargando) {
    return <Cargando texto="Conectando con el servidor…" />;
  }
  // Si ya hay sesión no tiene sentido mostrar el formulario.
  if (usuario) {
    return <Navigate to="/" replace />;
  }

  /**
   * Envía las credenciales a /api/sesion.
   * preventDefault() evita que el navegador recargue la página (comportamiento normal de <form>).
   */
  const manejarEnvio = async (evento) => {
    evento.preventDefault();
    if (!email.trim() || !contrasena) {
      setError('Ingrese su correo electrónico y su contraseña.');
      return;
    }
    setEnviando(true);
    setError('');
    try {
      await iniciarSesion(email, contrasena);
      // Vuelve a la página que el usuario intentaba abrir, o al inicio.
      navegar(ubicacion.state?.desde ?? '/', { replace: true });
    } catch (excepcion) {
      setError(excepcion.message);
      setContrasena('');
    } finally {
      setEnviando(false);
    }
  };

  return (
    <main className="pagina-acceso">
      <div className="tarjeta-acceso">
        <div className="logo" aria-hidden="true">HC</div>
        <h1>Iniciar Sesión</h1>
        <p className="subtitulo">Sistema de realización y seguimiento de pedidos</p>

        <Alerta tipo={ubicacion.state?.tipo ?? 'info'}>{ubicacion.state?.mensaje}</Alerta>
        <Alerta tipo="error">{error || errorConexion}</Alerta>

        <form onSubmit={manejarEnvio} noValidate>
          <div className="campo">
            <label htmlFor="email">Correo electrónico</label>
            <input id="email" type="email" autoComplete="username" autoFocus required
                   placeholder="nombre@homecenter.com"
                   value={email} onChange={(evento) => setEmail(evento.target.value)} />
          </div>

          <div className="campo">
            <label htmlFor="contrasena">Contraseña</label>
            <div className="contrasena-envoltura">
              <input id="contrasena" type={contrasenaVisible ? 'text' : 'password'} required
                     autoComplete="current-password" placeholder="••••••••"
                     value={contrasena} onChange={(evento) => setContrasena(evento.target.value)} />
              <button type="button" className="mostrar-contrasena"
                      onClick={() => setContrasenaVisible((visible) => !visible)}>
                {contrasenaVisible ? 'OCULTAR' : 'MOSTRAR'}
              </button>
            </div>
          </div>

          <button type="submit" className="boton boton--primario boton--bloque" disabled={enviando}>
            {enviando ? 'INGRESANDO…' : 'INGRESAR'}
          </button>
        </form>

        <div className="enlaces-acceso">
          {/* El registro de clientes se hace en la versión JSP (/registro). */}
          ¿No tiene cuenta? <a href="../registro">Regístrese aquí</a>
        </div>

        <p className="nota-acceso">
          Versión <strong>React</strong> · Portal para Clientes, Vendedores, Logística y Transportistas.<br />
          <a href="../login">Usar la versión JSP</a>
        </p>
      </div>
    </main>
  );
}
