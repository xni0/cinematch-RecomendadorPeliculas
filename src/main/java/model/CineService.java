package model;

import dao.Conexion;
import dao.PeliculaDAO;
import dao.UsuarioPeliculaDAO;
import entidades.Pelicula;
import entidades.UsuarioPelicula;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de negocio (paquete 'model').
 * El Controller solo habla con esta clase.
 * Abre y cierra la conexión en cada método.
 */
public class CineService {

    // Lista de géneros (fija)
    public List<String> getGeneros() {
        return List.of("ACCIÓN", "COMEDIA", "DRAMA", "CIENCIA FICCIÓN", "TERROR");
    }

    // --- Métodos para cada operación ---

    // REGISTRAR
    public String handleRegistrar(String alias, String genero, Double valoracion) {
        Conexion C = new Conexion();
        Connection con = null; // Creamos la variable 'con'
        try {
            con = C.getConexion(); // Obtenemos la conexión
            
            PeliculaDAO peliculaDAO = new PeliculaDAO(con);
            UsuarioPeliculaDAO upDAO = new UsuarioPeliculaDAO(con);

            Pelicula mejorPelicula = peliculaDAO.findBestByGenero(genero);

            if (mejorPelicula != null) {
                UsuarioPelicula up = new UsuarioPelicula();
                up.setAlias(alias);
                up.setPelicula(mejorPelicula);
                up.setValoracion(valoracion); 
                
                upDAO.insert(up);
                
                return "Valoración registrada para: " + mejorPelicula.getTitulo();
            } else {
                return "No hay películas de " + genero + " para registrar.";
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al registrar: " + e.getMessage();
        } finally {
            // --- ¡AQUÍ ESTÁ EL ARREGLO! ---
            // No es C.close(), es con.close()
            // Y hay que meterlo en su propio try/catch
            if (con != null) {
                try {
                    con.close(); // ¡Cerramos la conexión, no el objeto 'C'!
                } catch (Exception e) {
                    System.err.println("Error al cerrar la conexión en Registrar");
                    e.printStackTrace();
                }
            } 
        }
    }

    // RECOMENDAR
    public Pelicula handleRecomendar(String genero) {
        Conexion C = new Conexion();
        try (Connection con = C.getConexion()) { 
            PeliculaDAO peliculaDAO = new PeliculaDAO(con);
            return peliculaDAO.findBestByGenero(genero); 
        } catch (Exception e) {
            e.printStackTrace();
            return null; 
        }
    }

    // CONSULTAR HISTORIAL
    public List<UsuarioPelicula> handleConsultarHistorial(String alias, int page, int pageSize) {
        Conexion C = new Conexion();
        try (Connection con = C.getConexion()) {
            UsuarioPeliculaDAO upDAO = new UsuarioPeliculaDAO(con);
            return upDAO.findByAlias(alias, page, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>(); 
        }
    }
    
    // Método extra para saber el TOTAL de registros (para la paginación)
    public int getTotalHistorial(String alias) {
        Conexion C = new Conexion();
        try (Connection con = C.getConexion()) {
            UsuarioPeliculaDAO upDAO = new UsuarioPeliculaDAO(con);
            return upDAO.countByAlias(alias);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // BORRAR HISTORIAL
    public String handleBorrarHistorial(String alias) {
        Conexion C = new Conexion();
        try (Connection con = C.getConexion()) {
            UsuarioPeliculaDAO upDAO = new UsuarioPeliculaDAO(con);
            int filasBorradas = upDAO.deleteByAlias(alias);
            return "Historial borrado. Se eliminaron " + filasBorradas + " registros.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al borrar el historial.";
        }
    }

    // TOP PELICULAS
    public List<Pelicula> handleTopPeliculas(int limit) {
        Conexion C = new Conexion();
        try (Connection con = C.getConexion()) {
            PeliculaDAO peliculaDAO = new PeliculaDAO(con);
            return peliculaDAO.findTopRated(limit);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>(); 
        }
    }
    
    // Para el panel de 'home.jsp'
    public List<UsuarioPelicula> getUltimas(String alias, int limit) {
        Conexion C = new Conexion();
        try (Connection con = C.getConexion()) {
            UsuarioPeliculaDAO upDAO = new UsuarioPeliculaDAO(con);
            return upDAO.findLastByAlias(alias, limit);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}