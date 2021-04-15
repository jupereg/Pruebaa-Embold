package sol;
	//Juan Pérez Garrido
	//Correccion:Ismael Cariñanos (comentarios en los que pone ISMA)
	import java.io.IOException;
	import java.io.InputStream;
	import java.sql.Connection;
	import java.sql.Date;
	import java.sql.DriverManager;
	import java.sql.PreparedStatement;
	import java.sql.ResultSet;
	import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
	import java.util.Calendar;
	import java.util.List;
	import java.util.Properties;

import model.Captacion;
import model.Empleado;
	import model.ExcepcionDeAplicacion;
	import model.Finca;
	import model.Propietario;
	import model.Sucursal;
	import pers.Persistencia;

	public class GestorBD implements Persistencia {

		//Constantes
		private static final String URL = getPropiedad("url");
		private static final String USR = getPropiedad("user");
		private static final String PWD = getPropiedad("password");
		//Métodos
		public static String getPropiedad(String clave) {
		String valor = null;
		try {
		    Properties props = new Properties();
		    InputStream prIS = GestorBD.class.getResourceAsStream("/conexion.properties");
		    props.load(prIS);
		    valor = props.getProperty(clave);
		} catch (IOException ex) { ex.printStackTrace();
		}
		return valor;
		}
		@Override
		public List<Finca> buscaFincas(double arg0, double arg1) throws ExcepcionDeAplicacion {
			// TODO Auto-generated method stub
			List<Finca> lista=new ArrayList<>();
			Connection con=null;
			Finca finca=null;
			try {
				//Vamos a usar PreparedStatement
				con=DriverManager.getConnection(URL, USR, PWD);
				String s1="SELECT * FROM finca WHERE alquiler>=? AND alquiler<=?";
				PreparedStatement ps=con.prepareStatement(s1);//consulta ya precompilada
				ps.setDouble(1, arg0);//primera componente ? de la consulta
				ps.setDouble(2, arg1);//segunda componente ? de la consulta
				ResultSet rs=ps.executeQuery();//se realiza y almacena la consulta
				
				while(rs.next()) {
					//nos ayudamos del método creado previamente
					lista.add(getFinca(con,rs.getString(1)));//No hace falta el this.
				}
				//cerramos conexiones
				rs.close();
				ps.close();
				
			}
			catch(SQLException e) {
				e.printStackTrace();
				throw new ExcepcionDeAplicacion(e);
			}
			finally {
					try {
						if(con!=null)
							con.close();
					}
					catch(SQLException e) {
						e.printStackTrace();
						throw new ExcepcionDeAplicacion(e);
					}
				}
			return lista;
		}

		@Override
		public boolean eliminarCliente(String arg0) throws ExcepcionDeAplicacion {
			// TODO Auto-generated method stub
			Connection con=null;
			boolean b=false;
			try {
				con=DriverManager.getConnection(URL, USR, PWD);
				con.setAutoCommit(false);
				String s1="Delete from cliente where id_cliente=?";
				String s2="Delete from visita where id_cliente=?";
				PreparedStatement ps1=con.prepareStatement(s1);
				PreparedStatement ps2=con.prepareStatement(s2);
				ps1.setString(1, arg0);
				ps2.setString(1, arg0);
				//Primero borrar de las visitas porque al revés no funcionaría
				ps2.executeUpdate();
				int cantidad=ps1.executeUpdate();//Con comprobar solo en la tabla cliente sabremos si se ha hecho algún cambio
				
				if(cantidad!=0)
					b=true;
				
				ps1.close();
				ps2.close();
				con.commit();
			}
			catch(SQLException e) {
				e.printStackTrace();
				try {
					if(con!=null)
						con.rollback();
				}
				catch(SQLException ex) {
					ex.printStackTrace();
					throw new ExcepcionDeAplicacion(e);
				}
				throw new ExcepcionDeAplicacion(e);
			}
			finally {
				try {
					if(con!=null)
						con.close();
				}
				catch(SQLException e) {
					e.printStackTrace();
					throw new ExcepcionDeAplicacion(e);
				}
			}
			return b;
		}

		@Override
		public Empleado getEmpleado(String arg0) throws ExcepcionDeAplicacion {
			// TODO Auto-generated method stub
			//Realizado con join
			Empleado empleado=null;
			Sucursal sucursal=null;
			Connection con=null;
			try {
				con=DriverManager.getConnection(URL, USR, PWD);
				String s1="SELECT * FROM empleado WHERE id_empleado=?";
				String s2="SELECT s.* FROM sucursal s JOIN empleado e ON s.id_sucursal=e.sucursal WHERE e.id_empleado=?";
				PreparedStatement ps1=con.prepareStatement(s1);
				PreparedStatement ps2=con.prepareStatement(s2);
				ps1.setString(1, arg0);
				ps2.setString(1, arg0);
				ResultSet rs1=ps1.executeQuery();
				ResultSet rs2=ps2.executeQuery();					
				
				if(rs2.next()) {
					sucursal=new Sucursal(rs2.getString(1),rs2.getString(2),rs2.getString(3),rs2.getString(4));
				}
				if(rs1.next()) {
					String sexo=rs1.getString(5);
					char sexoChar=(sexo==null)? null:sexo.charAt(0);
					//Character sexoChar=sexo.charAt(0);
					Double salario=rs1.getDouble(7);
					if(rs1.wasNull()) salario=null;
							
					Date d=rs1.getDate(6);
					Calendar cal=Calendar.getInstance();
					if(d!=null) 					
						cal.setTime(d);
					else
						cal=null;
					empleado=new Empleado(rs1.getString(1),rs1.getString(2),rs1.getString(3),rs1.getString(4),sexoChar,salario,cal,sucursal);
				}
				rs1.close();rs2.close();
				ps1.close();ps2.close();
			}
			catch(SQLException e) {
				e.printStackTrace();
				throw new ExcepcionDeAplicacion(e);
			}
			finally {
					try {
						if(con!=null)
							con.close();
					}
					catch(SQLException e) {
						e.printStackTrace();
						throw new ExcepcionDeAplicacion(e);
					}
				}
			return empleado;
		}

		@Override
		public Finca getFinca(String arg0) throws ExcepcionDeAplicacion {
			// TODO Auto-generated method stub
			//Lo realizaremos con un JOIN
			Finca finca=null;
			Propietario prop=null;
			Connection con=null;
			try {
				con=DriverManager.getConnection(URL, USR, PWD);
				String s1="SELECT * FROM finca WHERE id_finca=?";
				String s2="SELECT P.* FROM PROPIETARIO AS P JOIN FINCA AS F ON P.ID_PROPIETARIO=F.PROPIETARIO WHERE id_finca =?";	
				PreparedStatement pstm = con.prepareStatement(s1);
				PreparedStatement pstm2=con.prepareStatement(s2);
				pstm.setString(1, arg0);
				pstm2.setString(1, arg0);
				ResultSet rs1=pstm.executeQuery();
				ResultSet rs2=pstm2.executeQuery();	
				
				//Boolean ascB=(asc==null)? null: asc.equalsIgnoreCase("Si")
				
				if(rs2.next()) {
					prop=new Propietario(rs2.getString(1),rs2.getString(2),rs2.getString(3),rs2.getString(4),rs2.getString(5));
				}			
				if(rs1.next()) {
			         boolean hayAscensor;
			         String ascensor=rs1.getString(9);
			         hayAscensor=(ascensor==null)? null:ascensor.equalsIgnoreCase("si");
			         			          
			         Integer habitaciones = rs1.getInt(6); 
				  	 if (rs1.wasNull()) habitaciones=null;
			         Integer banios = rs1.getInt(7);
			  		 if (rs1.wasNull()) banios=null;
			  		 Double alquiler = rs1.getDouble(10); 
			  		 if (rs1.wasNull()) alquiler=null;
			  		 
			  		 finca=new Finca(rs1.getString(1),rs1.getString(2),rs1.getString(3),rs1.getString(4),rs1.getString(5),habitaciones,banios,rs1.getString(8),hayAscensor,prop,alquiler);
			  		
				}
				rs1.close();rs2.close();
				pstm.close();pstm2.close();
			}
			catch(SQLException e) {
				e.printStackTrace();
				throw new ExcepcionDeAplicacion(e);
			}
			finally {
					try {
						if(con!=null)
							con.close();
					}
					catch(SQLException e) {
						e.printStackTrace();
						throw new ExcepcionDeAplicacion(e);
					}
				}
			
			return finca;
		}
		
		
		@Override
		public int incrementarSueldo(float arg0) throws ExcepcionDeAplicacion {
			// TODO Auto-generated method stub
			long l1=System.currentTimeMillis();//tiempo inicial
			Connection con=null;
			int resultado=0;
			try {
				con=DriverManager.getConnection(URL, USR, PWD);
				String s1="UPDATE empleado SET salario=salario+(salario*?/100)";
				PreparedStatement ps=con.prepareStatement(s1);
				ps.setFloat(1,arg0);
				resultado=ps.executeUpdate();
				
				ps.close();
			}
			catch(SQLException e) {
				e.printStackTrace();
				throw new ExcepcionDeAplicacion(e);
			}
			finally {
				try {
					if(con!=null)
						con.close();
				}
				catch(SQLException e) {
					e.printStackTrace();
					throw new ExcepcionDeAplicacion(e);
				}
			}
			long l2=System.currentTimeMillis();//tiempo final
			System.out.println("Tiempo tardado incrementarSueldo(float)"+(l2-l1));
			return resultado;
		}
		
		//PRÁCTICA 5
		@Override
		public void aniadirEmpleado(Empleado arg0) throws ExcepcionDeAplicacion {
			// TODO Auto-generated method stub
			Connection con=null;
			try {
				con=DriverManager.getConnection(URL, USR, PWD);
				con.setAutoCommit(false);
				
				if(this.getEmpleado(arg0.getId())==null) { //si no está en la BD
					String sql="SELECT * FROM sucursal WHERE id_sucursal=?";					
//					PreparedStatement ps=con.prepareStatement(sql);
//					ps.setString(1, arg0.getSucursal().getId());
//					ResultSet rs=ps.executeQuery();
					if(!existe(con,sql,arg0.getSucursal().getId())) {//si no existe tampoco la sucursal
						String sql2="INSERT INTO sucursal VALUES(?,?,?,?)";
						PreparedStatement ps2=con.prepareStatement(sql2);
						ps2.setString(1, arg0.getSucursal().getId());	
						ps2.setString(2, arg0.getSucursal().getDireccion());		
						ps2.setString(3, arg0.getSucursal().getCiudad());		
						ps2.setString(4, arg0.getSucursal().getCP());	
						ps2.executeUpdate();
						
						ps2.close();						
					}
//					rs.close();
//					ps.close();					
					
					String sql3="INSERT INTO empleado VALUES(?,?,?,?,?,?,?,?)";
					PreparedStatement ps3=con.prepareStatement(sql3);
					ps3.setString(1, arg0.getId());
					ps3.setString(2, arg0.getNombre());
					ps3.setString(3, arg0.getApellidos());
					ps3.setString(4, arg0.getTrabajo());
					//comprobar nulos para tipos primitivos
					Character sexo=arg0.getSexo();//para acceder 2 veces mejor lo almacenamos en una variable
					if(sexo==null)
						ps3.setNull(5, java.sql.Types.BOOLEAN);
					else
						ps3.setString(5, Character.toString(sexo));
					
					Calendar fechaN=arg0.getFechaNacimiento();
					if(fechaN==null)
						ps3.setDate(6, null);
					else
						ps3.setDate(6, new java.sql.Date(fechaN.getTimeInMillis()));
					
					Double salario=arg0.getSalario();
					if(salario==null)
						ps3.setNull(7, Types.DOUBLE);
					else
						ps3.setDouble(7, salario);
					
					ps3.setString(8,arg0.getSucursal().getId() );
					
					ps3.executeUpdate();
					
					ps3.close();
					con.commit();//cierre de transacción
				}
			}
			catch(SQLException e) {
				e.printStackTrace();	
				try {
					if(con!=null)
						con.rollback();
				}
				catch(SQLException ex) {
					ex.printStackTrace();
					throw new ExcepcionDeAplicacion(ex);
				}
				throw new ExcepcionDeAplicacion(e);
			}
			finally {
				try {
					if(con!=null)
						con.close();
				}
				catch(SQLException e) {
					e.printStackTrace();	
					throw new ExcepcionDeAplicacion(e);
				}
			}
			
		}
		@Override
		public int eliminarEmpleados(List<String> arg0) throws ExcepcionDeAplicacion {
			// TODO Auto-generated method stub
			Connection con=null;
			int resultado=0;
			try {
				con=DriverManager.getConnection(URL, USR, PWD);
				con.setAutoCommit(false);
				String s1="DELETE FROM visita WHERE id_empleado=?";
				String s2="DELETE FROM captacion WHERE id_empleado=?";
				String s3="DELETE FROM empleado WHERE id_empleado=?";
				PreparedStatement ps1=con.prepareStatement(s1);
				PreparedStatement ps2=con.prepareStatement(s2);
				PreparedStatement ps3=con.prepareStatement(s3);
				for(int i=0;i<arg0.size();i++) {
					ps1.setString(1, arg0.get(i));
					ps1.executeUpdate();
					ps2.setString(1, arg0.get(i));
					ps2.executeUpdate();
					ps3.setString(1, arg0.get(i));
					
					resultado=resultado + ps3.executeUpdate();//vamos almacenando en resultado la cantidad de filas borradas de empleado
				}
				ps1.close();ps2.close();ps3.close();
				con.commit();
			}
			catch(SQLException e) {
				e.printStackTrace();
				try {
					if(con!=null)
						con.rollback();
				}
				catch(SQLException ex) {
					ex.printStackTrace();
					throw new ExcepcionDeAplicacion(ex);
				}
				throw new ExcepcionDeAplicacion(e);
			}
			finally {
				try {
					if(con!=null)
						con.close();
				}
				catch(SQLException e) {
					e.printStackTrace();
					throw new ExcepcionDeAplicacion(e);
				}
			}
			return resultado;
		}
		@Override
		public int incrementarSueldoUpdatableResultSet(float arg0) throws ExcepcionDeAplicacion {
			// TODO Auto-generated method stub
			long l1=System.currentTimeMillis();//tiempo inicial
			Connection con=null;
			int resultado=0;
			try {
				con=DriverManager.getConnection(URL, USR, PWD);
				con.setAutoCommit(false);
				Statement st=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				ResultSet rs=st.executeQuery("SELECT * FROM empleado");
				while(rs.next()) {
					double sueldo=rs.getDouble(7);
					if(rs.wasNull())
						rs.updateNull(7);
					else
						rs.updateDouble(7, sueldo+sueldo*arg0/100);
					
					rs.updateRow();
					resultado++;
				}
								
				rs.close();
				st.close();
				con.commit();
			}
			catch(SQLException e) {
				e.printStackTrace();
				try {
					if(con!=null)
						con.rollback();
				}
				catch(SQLException ex) {
					ex.printStackTrace();
					throw new ExcepcionDeAplicacion(ex);
				}
				throw new ExcepcionDeAplicacion(e);
			}
			finally {
				try {
					if(con!=null)
						con.close();
				}
				catch(SQLException e) {
					e.printStackTrace();
					throw new ExcepcionDeAplicacion(e);
				}
			}
			long l2=System.currentTimeMillis();//tiempo final
			System.out.println("Tiempo tardado incrementarSueldoUpdatableResultSet(float):"+(l2-l1));
			return resultado;
			
		}
		public boolean existe(Connection con,String sql,String valorId) throws ExcepcionDeAplicacion{
			boolean existe=false;
			try {
				PreparedStatement ps=con.prepareStatement(sql);  
				ps.setString(1, valorId);
				ResultSet rs=ps.executeQuery();
				
				existe=rs.next();
				
				rs.close();
				ps.close();				
			}
			catch(SQLException ex) {
				ex.printStackTrace();
				throw new ExcepcionDeAplicacion("Error al borrar el cliente",ex);
			}
			return existe;
		}
		
		//SOBRECARGADO GETFINCA
		public Finca getFinca(Connection con,String arg0) throws ExcepcionDeAplicacion {
			// TODO Auto-generated method stub
			//Lo realizaremos con un JOIN
			Finca finca=null;
			Propietario prop=null;
			try {
				String s1="SELECT * FROM finca WHERE id_finca=?";
				String s2="SELECT P.* FROM PROPIETARIO AS P JOIN FINCA AS F ON P.ID_PROPIETARIO=F.PROPIETARIO WHERE id_finca =?";	
				PreparedStatement pstm = con.prepareStatement(s1);
				PreparedStatement pstm2=con.prepareStatement(s2);
				pstm.setString(1, arg0);
				pstm2.setString(1, arg0);
				ResultSet rs1=pstm.executeQuery();
				ResultSet rs2=pstm2.executeQuery();	
				
				//Boolean ascB=(asc==null)? null: asc.equalsIgnoreCase("Si")
				
				if(rs2.next()) {
					prop=new Propietario(rs2.getString(1),rs2.getString(2),rs2.getString(3),rs2.getString(4),rs2.getString(5));
				}			
				if(rs1.next()) {
			         boolean hayAscensor;
			         String ascensor=rs1.getString(9);
			         hayAscensor=(ascensor==null)? null:ascensor.equalsIgnoreCase("si");
			         			          
			         Integer habitaciones = rs1.getInt(6); 
				  	 if (rs1.wasNull()) habitaciones=null;
			         Integer banios = rs1.getInt(7);
			  		 if (rs1.wasNull()) banios=null;
			  		 Double alquiler = rs1.getDouble(10); 
			  		 if (rs1.wasNull()) alquiler=null;
			  		 
			  		 finca=new Finca(rs1.getString(1),rs1.getString(2),rs1.getString(3),rs1.getString(4),rs1.getString(5),habitaciones,banios,rs1.getString(8),hayAscensor,prop,alquiler);
			  		
				}
				rs1.close();rs2.close();
				pstm.close();pstm2.close();
			}
			catch(SQLException e) {
				e.printStackTrace();
				throw new ExcepcionDeAplicacion(e);
			}
			
			
			return finca;
		}

		//Ejecutando los 2 métodos de incrementar, en incrementarSueldo(float)
		//se reduce notablemente el tiempo con respecto a incrementarSueldoUpdatableResultSet(float)
		//Para comprobarlo he creado una nueva clase principal con un método main
		
		//EJERCICIO OPCIONAL
		public void aniadirCaptacion(Finca finca,String idEmpleado)throws ExcepcionDeAplicacion{
			Captacion cap=null;
			Connection con=null;
			try {
				con=DriverManager.getConnection(URL, USR, PWD);
				String sql1="INSERT INTO finca VALUES(?,?,?,?,?,?,?,?,?,?,?)";
				String sql2="INSERT INTO captacion VALUES(?,?,?)";
				String sql3="INSERT INTO comision VALUES(?,?,?)";
				con.setAutoCommit(false);
				PreparedStatement ps1=con.prepareStatement(sql1);
				PreparedStatement ps2=con.prepareStatement(sql2);
				PreparedStatement ps3=con.prepareStatement(sql3);
				
				ps1.setString(1, finca.getId());
				ps1.setString(2, finca.getDireccion());
				ps1.setString(3, finca.getCiudad());
				ps1.setString(4, finca.getCP());
				ps1.setString(5, finca.getTipo());
				Integer habitaciones=finca.getHabitaciones();
				if(habitaciones==null)
					ps1.setNull(6, java.sql.Types.INTEGER);
				else
					ps1.setInt(6, habitaciones);
				Integer banios=finca.getBanios();
				if(banios==null)
					ps1.setNull(7, java.sql.Types.INTEGER);
				else
					ps1.setInt(7, banios);
				ps1.setString(8,finca.getCalefaccion());
				Boolean asc=finca.tieneAscensor();
				if(asc)
					ps1.setString(9, "si");
				else
					ps1.setString(9, "no");
				Double alquiler=finca.getAlquiler();
				if(alquiler==null)
					ps1.setNull(10, Types.DOUBLE);
				else
					ps1.setDouble(10, alquiler);
				
				String idPropietario=finca.getPropietario().getId();
				if(!existe(con,"select * from propietario where id_propietario=?",idPropietario))
					this.aniadirPropietario(con,finca.getPropietario());
				
				ps1.setString(11, idPropietario);
				
				
				ps2.setString(1,finca.getId());
				ps2.setString(2, idEmpleado);
				Date fecha=new java.sql.Date(Calendar.getInstance().getTimeInMillis());
				ps2.setDate(3, fecha);
				
				
				ps3.setString(1, idEmpleado);
				ps3.setDate(2,fecha);
				if(finca.getTipo().equalsIgnoreCase("unifamiliar"))
					ps3.setDouble(3, 5);
				if(finca.getTipo().equalsIgnoreCase("piso")&&finca.getHabitaciones()>3)
					ps3.setDouble(3, 5);
				if(finca.getTipo().equalsIgnoreCase("piso")&&finca.getHabitaciones()<=3)
					ps3.setDouble(3, 7);
				
				ps1.executeUpdate();ps2.executeUpdate();ps3.executeUpdate();
				ps1.close();ps2.close();ps3.close();
				con.commit();
			}
			catch(SQLException e) {
				e.printStackTrace();
				try {
					if(con!=null)
						con.rollback();
					
				}
				catch(SQLException ex) {
					ex.printStackTrace();
					throw new ExcepcionDeAplicacion(ex);
				}
				throw new ExcepcionDeAplicacion(e);
			}
			finally {
				try {
					if(con!=null)
						con.close();
				}
				catch(SQLException e) {
					e.printStackTrace();
					throw new ExcepcionDeAplicacion(e);
				}
			}
		}
		
		public void aniadirPropietario(Connection con,Propietario prop) throws ExcepcionDeAplicacion{
			try {
				String sql="INSERT into propietario values(?,?,?,?,?)";
				PreparedStatement ps=con.prepareStatement(sql);
				ps.setString(1, prop.getId());
				ps.setString(2, prop.getNombre());
				ps.setString(3, prop.getApellidos());
				ps.setString(4, prop.getDireccion());
				ps.setString(5, prop.getTelefono());
				ps.executeUpdate();
				ps.close();
			}
			catch(SQLException e) {
				e.printStackTrace();
				throw new ExcepcionDeAplicacion(e);
			}
			
		}
		
		public Propietario getPropietario(String idPropietario)throws ExcepcionDeAplicacion{
			Connection con=null;
			Propietario prop=null;
			try {
				con=DriverManager.getConnection(URL,USR, PWD);
				String sql="select * from propietario where id_propietario=?";
				PreparedStatement ps=con.prepareStatement(sql);
				ps.setString(1, idPropietario);
				ResultSet rs=ps.executeQuery();
				if(rs.next()) {
					prop=new Propietario(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5));
				}
				rs.close();
				ps.close();
			}
			catch(SQLException e) {
				e.printStackTrace();
				throw new ExcepcionDeAplicacion(e);
			}
			finally {
				try {
					if(con!=null)
						con.close();
				}
				catch(SQLException ex) {
					ex.printStackTrace();
					throw new ExcepcionDeAplicacion(ex);
				}
			}
			return prop;
		}
}

