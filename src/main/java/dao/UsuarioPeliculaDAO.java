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

// EL historial JSP y el panel "Últimas del alias" en home.jsp 
// necesitan mostrar el TÍTULO y el GÉNERO de la película
public class UsuarioPeliculaDAO {
    
    private Connection con;

    public UsuarioPeliculaDAO(Connection con) {
        this.con = con;
    }

    // Helper para "traducir" el resultado del JOIN a un objeto UsuarioPelicula
    private UsuarioPelicula mapFromResultSet(ResultSet rs) throws SQLException {
        // 1. Crea el objeto pelicula usando las columnas que vienen del JOIN (titulo, genero)
        Pelicula p = new Pelicula(
            rs.getInt("pelicula_id"),
            rs.getString("titulo"),
            rs.getString("genero"),
            rs.getDouble("puntuacion_media")
        );
        
        // 2. Crear el UsuarioPelicula --> CAJA PRINCIPAL
        UsuarioPelicula up = new UsuarioPelicula();
        // Rellena la caja con los datos básicos
        up.setId(rs.getLong("id")); 
        up.setAlias(rs.getString("alias"));
        up.setFechaRegistro(rs.getTimestamp("fecha_registro"));
        
        // 3. Comprobar si la valoración era NULL
        double valoracion = rs.getDouble("valoracion");
        if (rs.wasNull()) {
            up.setValoracion(null);
        } else {
            up.setValoracion(valoracion);
        }
        
        up.setPelicula(p); // Meter la peli dentro de la caja
        return up;
    }


    // Insertar un registro
    public void insert(UsuarioPelicula up) throws SQLException {
    	// NEXTVAL le dice a Oracle que coja el siguiente número de la secuencia
    	// así el ID es automatico
        String sql = "INSERT INTO usuario_pelicula (id, alias, pelicula_id, valoracion, fecha_registro) " +
                     "VALUES (seq_usuario_pelicula.NEXTVAL, ?, ?, ?, SYSTIMESTAMP)";
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, up.getAlias());
            // De pelicula solo guarda el ID
            ps.setInt(2, up.getPelicula().getId());
            
            if (up.getValoracion() != null) {
            	// si tiene la valoracion la guarda
                ps.setDouble(3, up.getValoracion());
            } else {
                ps.setNull(3, Types.DECIMAL); 
            }
            
            ps.executeUpdate();
        }
    }

    // Buscar historial paginado del usuario
    public List<UsuarioPelicula> findByAlias(String alias, int page, int pageSize) throws SQLException {
        List<UsuarioPelicula> historial = new ArrayList<>();

        // Esta es la consulta de paginación
        // 1. Primero hace JOIN y ordena todo el historial de usuario por fecha
        // 2. Coge la lista ordenada, le asigna un numero de fila (ROWNUM as rnum)
        // y se queda solo con los de arriba (WHERE ROWNUM <= ?). El ? aquí es el limiteSuperior
        // 3. Coge la lista y se queda solo con los de abajo (WHERE rnum > ?). El ? aquí es el limiteInferior
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

        // Calcula dónde "cortar" la lista basándose en el número de página (page) 
        // y el tamaño (pageSize)
        int limiteSuperior = (page + 1) * pageSize;
        int limiteInferior = page * pageSize;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
        	// pone el alias en la primera ? (consulta interna)
            ps.setString(1, alias);
            // Pone el límite superior en el segundo ?
            ps.setInt(2, limiteSuperior); 
            // Pone el límite inferior en el tercer ?
            ps.setInt(3, limiteInferior); 
            
            // recorre en un puntero los resultados y los traduce usando
            // mapFromResultSet
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    historial.add(mapFromResultSet(rs));
                }
            }
        }
        return historial;
    }

    // Contar el total de registros de un usuario (para la paginación)
    public int countByAlias(String alias) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuario_pelicula WHERE alias = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, alias);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                	// devuelve el primer y unico numero de la consulta
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
    // Para el panel lateral de home.jsp 
    // Busca los ultimos "N" registros de un usuario
    public List<UsuarioPelicula> findLastByAlias(String alias, int limit) throws SQLException {
        List<UsuarioPelicula> historial = new ArrayList<>();
        
        // 1. hace un JOIN y ordena por fecha
        // 2. luego hay una consulta externa que coge la lista ordenada
        // y se queda solo con los primeros (WHERE ROWNUM <= ?)
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
           // Rellena el ? con el limite
           ps.setInt(2, limit);
           try (ResultSet rs = ps.executeQuery()) {
        	   // recorre los resultados y los traduce
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
        	// dice que usuario borrar
            ps.setString(1, alias);
            // ejecuta el borrado y devuelve el número de filas que ha borrado
            return ps.executeUpdate(); 
        }
    }
}