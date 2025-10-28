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

    public PeliculaDAO(Connection con) {
        this.con = con;
    }

    private Pelicula mapPelicula(ResultSet rs) throws SQLException {
        return new Pelicula(
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

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, genero);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Usamos el helper para crear el objeto 
                    pelicula = mapPelicula(rs); 
                }
            }
        }
        return pelicula; // Devuelve la peli (o null si no había)
    }

    // Devuelve el Top N de películas
    public List<Pelicula> findTopRated(int limit) throws SQLException {
        List<Pelicula> peliculas = new ArrayList<>();
        

        String sql = "SELECT * FROM (" +
                     "  SELECT * FROM pelicula " +
                     "  ORDER BY puntuacion_media DESC" +
                     ")" +
                     " WHERE ROWNUM <= ?"; 

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    peliculas.add(mapPelicula(rs));
                }
            }
        }
        return peliculas;
    }
}