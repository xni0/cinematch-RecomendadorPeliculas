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
    // coger la nota y el género que le da el usuario, 
    // buscar la mejor película de ese género, y guardar la nota para esa película
    public String handleRegistrar(String alias, String genero, Double valoracion) {
    	// abrimos la conexion a la BBDD
        Conexion C = new Conexion();
        Connection con = null; // Creamos la variable 'con'
        try {
            con = C.getConexion(); // Obtenemos la conexión
            
            // crea los dos "traductores" de SQL, les pasa la conexion para que sepan
            // con que BBDD tienen que hablar
            PeliculaDAO peliculaDAO = new PeliculaDAO(con);
            UsuarioPeliculaDAO upDAO = new UsuarioPeliculaDAO(con);

            // llama el método findBestByGenero de peliculaDAO
            Pelicula mejorPelicula = peliculaDAO.findBestByGenero(genero);

            // comprueba si la BBDD ha encontrado la pelicula
            if (mejorPelicula != null) {
            	// crea una caja (objeto) vacía a la que llamamos up
                UsuarioPelicula up = new UsuarioPelicula();
                // rellena la "caja" up, con todos los datos
                up.setAlias(alias);
                up.setPelicula(mejorPelicula);
                up.setValoracion(valoracion); 
                
                // guarda los datos en la "caja" up
                upDAO.insert(up);
                
                // Devuelve el mensaje de éxito (el verde) al CineController
                return "Valoración registrada para: " + mejorPelicula.getTitulo();
            } else {
            	// No inserta nada. Solo devuelve el mensaje de aviso al CineController
                return "No hay películas de " + genero + " para registrar.";
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al registrar: " + e.getMessage();
        } finally {
        	
 
            if (con != null) {
                try {
                    con.close(); // ¡Cerramos la conexión, no el objeto 'C', para no dejarla colgada!
                } catch (Exception e) {
                    System.err.println("Error al cerrar la conexión en Registrar");
                    e.printStackTrace();
                }
            } 
        }
    }

    // RECOMENDAR
    // recibe un genero y devuelve el objeto Pelicula con la mejor nota
    public Pelicula handleRecomendar(String genero) {
    	// Crea el objeto que sabe como conectarse a la BBDD
        Conexion C = new Conexion();
        // try-with-resources --> obtiene la conexion y cuando se termine
        // incluso teniendo errores, se cierra la conexión automáticamente
        try (Connection con = C.getConexion()) { 
        	// crea el traductor de SQL PeliculaDAO y le da la conexion que se acaba de abrir
            PeliculaDAO peliculaDAO = new PeliculaDAO(con);
            // llama el método findBestByGenero de PeliculaDAO
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