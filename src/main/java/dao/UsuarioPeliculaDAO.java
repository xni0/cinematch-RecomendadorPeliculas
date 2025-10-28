package dao; 

import entidades.Pelicula;
import entidades.UsuarioPelicula;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types; 
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para 'usuario_pelicula' (paquete 'dao').
 * Este hace el JOIN.
 */
public class UsuarioPeliculaDAO {
    
    private Connection con;

    public UsuarioPeliculaDAO(Connection con) {
        this.con = con;
    }

    // Helper para "traducir" el resultado del JOIN a un objeto UsuarioPelicula
    private UsuarioPelicula mapFromResultSet(ResultSet rs) throws SQLException {
        // 1. Crear la Pelicula
        Pelicula p = new Pelicula(
            rs.getInt("pelicula_id"),
            rs.getString("titulo"),
            rs.getString("genero"),
            rs.getDouble("puntuacion_media")
        );
        
        // 2. Crear el UsuarioPelicula
        UsuarioPelicula up = new UsuarioPelicula();
        up.setId(rs.getLong("id")); // ¡long!
        up.setAlias(rs.getString("alias"));
        up.setFechaRegistro(rs.getTimestamp("fecha_registro")); // ¡Timestamp!
        
        // 3. Comprobar si la valoración era NULL
        double valoracion = rs.getDouble("valoracion");
        if (rs.wasNull()) {
            up.setValoracion(null);
        } else {
            up.setValoracion(valoracion);
        }
        
        up.setPelicula(p); // Meter la peli dentro
        return up;
    }


    // Insertar un registro
    public void insert(UsuarioPelicula up) throws SQLException {
        String sql = "INSERT INTO usuario_pelicula (id, alias, pelicula_id, valoracion, fecha_registro) " +
                     "VALUES (seq_usuario_pelicula.NEXTVAL, ?, ?, ?, SYSTIMESTAMP)";
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, up.getAlias());
            ps.setInt(2, up.getPelicula().getId());
            
            if (up.getValoracion() != null) {
                ps.setDouble(3, up.getValoracion());
            } else {
                ps.setNull(3, Types.DECIMAL); 
            }
            
            ps.executeUpdate();
        }
    }

    // Buscar historial paginado
    public List<UsuarioPelicula> findByAlias(String alias, int page, int pageSize) throws SQLException {
        List<UsuarioPelicula> historial = new ArrayList<>();

        // Esta es la query de paginación 
        String sql = "SELECT * FROM (" +
                     "  SELECT a.*, ROWNUM as rnum FROM (" +
                     "    SELECT up.*, p.titulo, p.genero, p.puntuacion_media " +
                     "    FROM usuario_pelicula up " +
                     "    JOIN pelicula p ON up.pelicula_id = p.id " +
                     "    WHERE up.alias = ? " +
                     "    ORDER BY up.fecha_registro DESC" +
                     "  ) a" +
                     "  WHERE ROWNUM <= ?" + 
                     ")" +
                     " WHERE rnum > ?"; 

        int limiteSuperior = (page + 1) * pageSize;
        int limiteInferior = page * pageSize;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, alias);
            ps.setInt(2, limiteSuperior); 
            ps.setInt(3, limiteInferior); 
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    historial.add(mapFromResultSet(rs));
                }
            }
        }
        return historial;
    }

    // Contar el total de registros (para la paginación)
    public int countByAlias(String alias) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuario_pelicula WHERE alias = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, alias);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
    // Para el panel lateral de home.jsp
    public List<UsuarioPelicula> findLastByAlias(String alias, int limit) throws SQLException {
        List<UsuarioPelicula> historial = new ArrayList<>();
        

        String sql = "SELECT * FROM (" +
                     "  SELECT up.*, p.titulo, p.genero, p.puntuacion_media " +
                     "  FROM usuario_pelicula up " +
                     "  JOIN pelicula p ON up.pelicula_id = p.id " +
                     "  WHERE up.alias = ? " +
                     "  ORDER BY up.fecha_registro DESC" +
                     ")" +
                     " WHERE ROWNUM <= ?";

       try (PreparedStatement ps = con.prepareStatement(sql)) {
           ps.setString(1, alias);
           ps.setInt(2, limit);
           try (ResultSet rs = ps.executeQuery()) {
               while (rs.next()) {
                   historial.add(mapFromResultSet(rs)); 
               }
           }
       }
       return historial;
   }

    // Borrar historial
    public int deleteByAlias(String alias) throws SQLException {
        String sql = "DELETE FROM usuario_pelicula WHERE alias = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, alias);
            return ps.executeUpdate(); 
        }
    }
}