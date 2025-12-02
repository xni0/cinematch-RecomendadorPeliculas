package dao; 

import entidades.Pelicula; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class PeliculaDAO {

    private Connection con;

    // constructor de la clase
    public PeliculaDAO(Connection con) {
        this.con = con;
    }

    // su unica función es traducir/mapear 
    private Pelicula mapPelicula(ResultSet rs) throws SQLException {
        return new Pelicula(
        	// recibe el ResultSet y lo convierte en un objeto Java Pelicula, va rellenando
            rs.getInt("id"),
            rs.getString("titulo"),
            rs.getString("genero"),
            rs.getDouble("puntuacion_media")
        );
    }

    // Devuelve la mejor peli de un género
    public Pelicula findBestByGenero(String genero) throws SQLException {
        Pelicula pelicula = null;
        
        String sql = "SELECT * FROM (" +
                     "  SELECT * FROM pelicula " +
                     "  WHERE genero = ? " +
                     "  ORDER BY puntuacion_media DESC" +
                     ")" +
                     " WHERE ROWNUM = 1"; // ROWNUM = 1 coge la primera fila

        // PreparedStatement prepara, ejecuta y envía órdenes SQL
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, genero);
            // Recoge los datos que le devuelve el SELECT guardándolos en un ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Usamos el helper para crear el objeto 
                    pelicula = mapPelicula(rs); 
                }
            }
        }
        return pelicula; // Devuelve la peli (o null si no había)
    }

    // Devuelve el Top N de películas con mejor puntuación de toda la BBDD
    // Para la pgina de Top Peliculas
    public List<Pelicula> findTopRated(int limit) throws SQLException {
        List<Pelicula> peliculas = new ArrayList<>();
        

        String sql = "SELECT * FROM (" +
                     "  SELECT * FROM pelicula " +
                     "  ORDER BY puntuacion_media DESC" +
                     ")" +
                     " WHERE ROWNUM <= ?"; 
        // coge solo las películas que estén en la fila ordenada entre el puesto 1 y el puesto ?

        try (PreparedStatement ps = con.prepareStatement(sql)) {
        	// rellena los huecos, de 1 a limit
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
            	// Recorre una por una las filas como si fuera un puntero
                while (rs.next()) {
                	// traduce cada fila de SQL a un objeto Pelicula usando 
                	// el helper mapPelicula
                    peliculas.add(mapPelicula(rs));
                }
            }
        }
        return peliculas;
    }
}