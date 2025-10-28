package dao;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import oracle.jdbc.pool.OracleDataSource;

public class Conexion {
    public Conexion() {
    }
    public Connection getConexion() throws SQLException,Exception{
    	String url="jdbc:oracle:thin:CINEMATCH21/CINEMATCH21@80.28.158.14:1521:oradai";
		//String url="jdbc:oracle:thin:VS2DAWBIBLIOTECA30/VS2DAWBIBLIOTECA30@80.28.158.14:1521:oradai";
        Connection con;
        OracleDataSource ods;
        try{

        	ods=new OracleDataSource();
            ods.setURL(url);
            con=ods.getConnection();
            DatabaseMetaData meta = con.getMetaData();
            System.out.println("JDBC driver version is " + meta.getDriverVersion());
            System.out.println("Data Source definido y conexion establecida");
        }
        catch(SQLException sqle){
        	System.out.println(sqle.toString());
            throw sqle;
        }
        catch(Exception e){
        	System.out.println(e.toString());
            throw e;
        }
        return con;
    }
}

