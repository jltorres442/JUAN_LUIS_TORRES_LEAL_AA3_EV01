/**
 * Formulario genérico de un módulo: INSERCIÓN (ruta /modulo/nuevo) y
 * ACTUALIZACIÓN (ruta /modulo/:id/editar).
 *
 * Proyecto: Homecenter Envíos
 * Autores : Juan Luis Torres Leal y Angela María Constain Escobar
 *
 * FLUJO
 *  1. Al abrir, consulta GET /api/{modulo}?accion=nuevo|editar: recibe el
 *     registro (vacío o con datos) y los catálogos de las listas desplegables.
 *  2. Cada cambio del usuario actualiza el estado "valores".
 *  3. Al guardar, envía POST con accion=guardar|actualizar.
 *     - 201/200: vuelve al listado con un mensaje de éxito.
 *     - 422: muestra el error de cada campo devuelto por el servidor.
 *     - 409: muestra el conflicto (p. ej. correo duplicado) y conserva lo digitado.
 */
import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/clienteApi.js';
import { useSesion } from '../contexto/SesionContexto.jsx';
import { buscarModulo } from '../modulos/definiciones.jsx';
import Alerta from '../componentes/Alerta.jsx';
import Cargando from '../componentes/Cargando.jsx';
import CampoFormulario from '../componentes/CampoFormulario.jsx';
import PaginaNoEncontrada from './PaginaNoEncontrada.jsx';
import { aValorFechaHoraLocal } from '../utilidades/formato.js';

/**
 * Convierte el registro recibido de la API en los valores iniciales del formulario.
 *
 * @param {object} modulo   definición del módulo
 * @param {object} registro registro JSON
 * @returns {Object<string, any>} valores por nombre de campo
 */
function valoresIniciales(modulo, registro) {
  const valores = {};
  modulo.campos.forEach((campo) => {
    const valor = registro?.[campo.nombre];
    if (campo.tipo === 'password') {
      valores[campo.nombre] = ''; // Las contraseñas nunca se precargan.
    } else if (campo.tipo === 'checkbox') {
      valores[campo.nombre] = valor ?? campo.valorInicial ?? false;
    } else if (campo.tipo === 'datetime-local') {
      valores[campo.nombre] = aValorFechaHoraLocal(valor);
    } else {
      valores[campo.nombre] = valor ?? '';
    }
  });
  return valores;
}

/**
 * Prepara los valores para enviarlos como formulario: una casilla marcada se
 * envía como "on" (igual que un <input type="checkbox"> de HTML) y una
 * desmarcada no se envía.
 */
function prepararEnvio(modulo, valores) {
  const datos = {};
  modulo.campos.forEach((campo) => {
    const valor = valores[campo.nombre];
    if (campo.tipo === 'checkbox') {
      if (valor) datos[campo.nombre] = 'on';
    } else {
      datos[campo.nombre] = valor;
    }
  });
  return datos;
}

export default function PaginaFormulario() {
  const { modulo: claveModulo, id } = useParams();
  const modulo = buscarModulo(claveModulo);
  const esNuevo = !id;
  const navegar = useNavigate();
  const { estados } = useSesion();

  const [valores, setValores] = useState(null);
  const [catalogos, setCatalogos] = useState({});
  const [errores, setErrores] = useState({});
  const [mensajeError, setMensajeError] = useState('');
  const [guardando, setGuardando] = useState(false);

  // 1. Carga inicial del formulario.
  useEffect(() => {
    if (!modulo) return;
    let activo = true;
    setValores(null);
    api.consultarFormulario(modulo.clave, id)
      .then((respuesta) => {
        if (!activo) return;
        setCatalogos(respuesta.catalogos ?? {});
        setValores(valoresIniciales(modulo, respuesta.registro));
      })
      .catch((excepcion) => {
        if (!activo) return;
        // Si el registro no existe se vuelve al listado con el mensaje del servidor.
        navegar(`/${modulo.clave}`, { replace: true, state: { tipo: 'error', mensaje: excepcion.message } });
      });
    return () => {
      activo = false;
    };
  }, [modulo, id, navegar]);

  if (!modulo) {
    return <PaginaNoEncontrada />;
  }

  /**
   * 2. Actualiza un campo. Se usa la forma funcional de setValores para partir
   * siempre del estado más reciente; además se borra el error de ese campo.
   */
  const cambiarValor = (nombre, valor) => {
    setValores((anteriores) => ({ ...anteriores, [nombre]: valor }));
    setErrores((anteriores) => ({ ...anteriores, [nombre]: undefined }));
  };

  /** 3. Envía el formulario (INSERT o UPDATE en el servidor). */
  const guardar = async (evento) => {
    evento.preventDefault();
    setGuardando(true);
    setMensajeError('');
    try {
      const respuesta = await api.guardar(modulo.clave, prepararEnvio(modulo, valores), id);
      navegar(`/${modulo.clave}`, { state: { tipo: 'exito', mensaje: respuesta.mensaje } });
    } catch (excepcion) {
      setErrores(excepcion.errores ?? {});
      setMensajeError(excepcion.message);
      // Por seguridad se limpian las contraseñas después de un error.
      setValores((anteriores) => ({ ...anteriores, contrasena: '', confirmarContrasena: '' }));
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } finally {
      setGuardando(false);
    }
  };

  return (
    <>
      <section className="banda-seccion">
        <div>
          <h2>{esNuevo ? `Registrar ${modulo.singular}` : `Actualizar ${modulo.singular} #${id}`}</h2>
          <p>Los campos marcados con <strong>*</strong> son obligatorios.</p>
        </div>
      </section>

      <Alerta tipo="error">{mensajeError}</Alerta>

      {!valores ? (
        <Cargando />
      ) : (
        <section className="tarjeta">
          {/* noValidate: la validación la hace el servidor y sus mensajes se muestran por campo. */}
          <form onSubmit={guardar} noValidate>
            <div className="formulario-rejilla">
              {modulo.campos.map((campo) => (
                <CampoFormulario
                  key={campo.nombre}
                  campo={campo}
                  valor={valores[campo.nombre]}
                  alCambiar={cambiarValor}
                  error={errores[campo.nombre]}
                  esNuevo={esNuevo}
                  catalogos={catalogos}
                  estados={estados}
                />
              ))}
            </div>

            <div className="acciones-formulario">
              <Link className="boton boton--contorno" to={`/${modulo.clave}`}>Cancelar</Link>
              <button type="submit" className="boton boton--primario" disabled={guardando}>
                {guardando ? 'GUARDANDO…' : esNuevo ? 'GUARDAR' : 'ACTUALIZAR'}
              </button>
            </div>
          </form>
        </section>
      )}
    </>
  );
}
