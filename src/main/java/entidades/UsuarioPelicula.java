package entidades;


import java.sql.Timestamp; // ¡Usamos Timestamp, lo que pide la BD!


public class UsuarioPelicula {

    private long id; 
    private String alias;
    // Aquí guardamos la peli entera (el DAO hará el JOIN)
    private Pelicula pelicula; 
    // Con 'D' mayúscula para que pueda ser 'null'
    private Double valoracion; 
    private Timestamp fechaRegistro; 


    public UsuarioPelicula() {
    }


    public long getId() {
        return id;
    }


    public void setId(long id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Pelicula getPelicula() {
        return pelicula;
    }

    public void setPelicula(Pelicula pelicula) {
        this.pelicula = pelicula;
    }

    public Double getValoracion() {
        return valoracion;
    }

    public void setValoracion(Double valoracion) {
        this.valoracion = valoracion;
    }


    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }


    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}