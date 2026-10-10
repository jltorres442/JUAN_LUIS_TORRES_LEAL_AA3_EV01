/**
 * Listado genérico de un módulo: CONSULTA (con buscador) y ELIMINACIÓN.
 * Sirve para las 9 tablas; lo que cambia entre ellas viene de definiciones.jsx.
 *
 * Proyecto: Homecenter Envíos
 * Autores : Juan Luis Torres Leal y Angela María Constain Escobar
 */
import { useCallback, useEffect, useState } from 'react';
import { Link, useLocation, useParams, useSearchParams } from 'react-router-dom';
import { api } from '../api/clienteApi.js';
import { buscarModulo } from '../modulos/definiciones.jsx';
import Alerta from '../componentes/Alerta.jsx';
import Cargando from '../componentes/Cargando.jsx';
import EtiquetaEstado from '../componentes/EtiquetaEstado.jsx';
import PaginaNoEncontrada from './PaginaNoEncontrada.jsx';
import { formatearFechaHora, formatearMoneda } from '../utilidades/formato.js';

/**
 * Dibuja el contenido de una celda según el tipo de columna.
 *
 * @param {object} columna  definición de la columna
 * @param {object} registro fila del JSON
 * @returns {React.ReactNode}
 */
function dibujarCelda(columna, registro) {
  if (columna.render) {
    return columna.render(registro);
  }
  const valor = registro[columna.campo];
  switch (columna.tipo) {
    case 'id':
      return <strong>{columna.prefijo ?? '#'}{valor}</strong>;
    case 'fecha':
      return valor ? String(valor).slice(0, 10) : '—';
    case 'fechaHora':
      return formatearFechaHora(valor);
    case 'moneda':
      return formatearMoneda(valor);
    case 'estado':
      return <EtiquetaEstado estado={valor} />;
    case 'booleano': {
      const [textoSi, textoNo] = columna.textos ?? ['SÍ', 'NO'];
      return <span className={`etiqueta etiqueta--${valor ? 'si' : 'no'}`}>{valor ? textoSi : textoNo}</span>;
    }
    default: {
      // Texto con un dato secundario opcional debajo (p. ej. correo y teléfono).
      const secundario = columna.secundarioTexto ? columna.secundarioTexto(registro) : registro[columna.secundario];
      return (
        <>
          {columna.clase === 'negrita' ? <strong>{valor}</strong> : valor}
          {secundario && <span className="secundario">{secundario}</span>}
        </>
      );
    }
  }
}

/** Clase CSS de la celda (alineación de números y fechas). */
function claseCelda(columna) {
  if (columna.tipo === 'moneda') return 'numero';
  if (columna.tipo === 'fecha' || columna.tipo === 'fechaHora') return 'fecha';
  return undefined;
}

export default function PaginaListado() {
  // useParams lee la parte variable de la URL: /#/pedidos → { modulo: 'pedidos' }
  const { modulo: claveModulo } = useParams();
  const modulo = buscarModulo(claveModulo);
  const ubicacion = useLocation();

  // El filtro vive en la URL (?q=...) para que el botón "Atrás" lo conserve.
  const [parametrosUrl, setParametrosUrl] = useSearchParams();
  const filtro = parametrosUrl.get('q') ?? '';
  const [textoBusqueda, setTextoBusqueda] = useState(filtro);

  const [registros, setRegistros] = useState(null);
  const [mensaje, setMensaje] = useState(ubicacion.state ?? null);

  /** CONSULTA: pide los registros al servidor (GET /api/{modulo}?q=...). */
  const cargar = useCallback(async () => {
    if (!modulo) return;
    setRegistros(null);
    try {
      const respuesta = await api.listar(modulo.clave, filtro);
      setRegistros(respuesta.registros);
    } catch (excepcion) {
      setRegistros([]);
      setMensaje({ tipo: 'error', mensaje: excepcion.message });
    }
  }, [modulo, filtro]);

  // Se vuelve a consultar cada vez que cambia el módulo o el filtro.
  useEffect(() => {
    cargar();
  }, [cargar]);

  // Al cambiar de módulo desde el menú se reinicia el buscador y el mensaje.
  useEffect(() => {
    setTextoBusqueda(filtro);
  }, [claveModulo, filtro]);

  // Cada navegación tiene una "key" distinta: el mensaje que trae el formulario
  // (p. ej. "se registró correctamente") se muestra, y se borra al navegar a otra parte.
  useEffect(() => {
    setMensaje(ubicacion.state ?? null);
  }, [ubicacion.key]); // eslint-disable-line react-hooks/exhaustive-deps

  if (!modulo) {
    return <PaginaNoEncontrada />;
  }

  /** Aplica la búsqueda escribiendo ?q= en la URL. */
  const buscar = (evento) => {
    evento.preventDefault();
    setParametrosUrl(textoBusqueda.trim() ? { q: textoBusqueda.trim() } : {});
  };

  /** ELIMINACIÓN: pide confirmación y envía POST /api/{modulo} (accion=eliminar). */
  const eliminar = async (registro) => {
    const id = registro[modulo.campoId];
    if (!window.confirm(`¿Seguro que desea eliminar ${modulo.describir(registro)}? Esta acción no se puede deshacer.`)) {
      return;
    }
    try {
      const respuesta = await api.eliminar(modulo.clave, id);
      setMensaje({ tipo: 'exito', mensaje: respuesta.mensaje });
      cargar();
    } catch (excepcion) {
      setMensaje({ tipo: 'error', mensaje: excepcion.message });
    }
  };

  return (
    <>
      <section className="banda-seccion">
        <div>
          <h2>{modulo.titulo}</h2>
          <p>{modulo.descripcion}</p>
        </div>
        <Link className="boton boton--banda" to={`/${modulo.clave}/nuevo`}>+ Nuevo {modulo.singular}</Link>
      </section>

      <Alerta tipo={mensaje?.tipo}>{mensaje?.mensaje}</Alerta>

      <form className="barra-herramientas" role="search" onSubmit={buscar}>
        <div className="buscador">
          <span aria-hidden="true">🔍</span>
          <input type="search" aria-label="Buscar" placeholder={modulo.ayudaBusqueda}
                 value={textoBusqueda} onChange={(evento) => setTextoBusqueda(evento.target.value)} />
          <button type="submit" className="boton boton--azul boton--pequeno">Buscar</button>
        </div>
        <span className="texto-secundario">
          {registros ? `${registros.length} registro(s)` : ''}
          {filtro && <> · <Link to={`/${modulo.clave}`}>Limpiar filtro</Link></>}
        </span>
      </form>

      {!registros ? (
        <Cargando />
      ) : (
        <div className="tabla-contenedor">
          <table className="tabla">
            <thead>
              <tr>
                {modulo.columnas.map((columna) => (
                  <th key={columna.campo} className={claseCelda(columna)}>{columna.titulo}</th>
                ))}
                <th className="acciones">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {registros.map((registro) => (
                <tr key={registro[modulo.campoId]}>
                  {modulo.columnas.map((columna) => (
                    <td key={columna.campo} className={claseCelda(columna)}>{dibujarCelda(columna, registro)}</td>
                  ))}
                  <td className="acciones">
                    <Link className="boton boton--contorno boton--pequeno"
                          to={`/${modulo.clave}/${registro[modulo.campoId]}/editar`}>Editar</Link>{' '}
                    <button type="button" className="boton boton--peligro boton--pequeno"
                            onClick={() => eliminar(registro)}>Eliminar</button>
                  </td>
                </tr>
              ))}
              {registros.length === 0 && (
                <tr>
                  <td colSpan={modulo.columnas.length + 1} className="tabla-vacia">
                    No se encontraron registros.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
