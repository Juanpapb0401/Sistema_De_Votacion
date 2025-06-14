package dispositivoPersonal;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import app.ServicePrx;

public class ConsultarPuesto {

    private final Scanner scanner = new Scanner(System.in);
    private Communicator communicator;
    private ServicePrx servicePrx;

    public static void main(String[] args) {
        ConsultarPuesto consulta = new ConsultarPuesto();
        consulta.inicializarIceGrid();
        consulta.run();
        consulta.cerrarConexion();
    }
    
    private void inicializarIceGrid() {
        try {
            System.out.println("Conectando al sistema de votación a través de IceGrid...");
            
            // Inicializar comunicador con localizador IceGrid
            String[] initData = {
                "--Ice.Default.Locator=SistemaVotacion/Locator:default -h localhost -p 4061"
            };
            communicator = Util.initialize(initData);
            
            // Conectar al ProxyCache en lugar de directamente a los servidores
            // El ProxyCache actuará como intermediario con cache
            ObjectPrx serviceBase = communicator.stringToProxy("ProxyCache-1");
            servicePrx = ServicePrx.checkedCast(serviceBase);
            
            if (servicePrx == null) {
                throw new RuntimeException("No se pudo conectar al ProxyCache");
            }
            
            System.out.println("Conexión establecida exitosamente con ProxyCache a través de IceGrid");
            
        } catch (Exception e) {
            System.err.println("Error al conectar con IceGrid: " + e.getMessage());
            System.err.println("Asegúrese de que:");
            System.err.println("1. icegridregistry esté corriendo");
            System.err.println("2. icegridnode esté corriendo");
            System.err.println("3. La aplicación esté desplegada");
            throw new RuntimeException("No se pudo establecer conexión con el broker", e);
        }
    }
    
    private void cerrarConexion() {
        if (communicator != null) {
            communicator.destroy();
        }
    }
    
    // Método que reemplaza ConexionBD usando IceGrid
    private List<Map<String, java.lang.Object>> getInfoBDWithParams(String sqlQuery, java.lang.Object... params) {
        try {
            System.out.println("Ejecutando consulta a través de ProxyCache...");
            
            // Convertir parámetros a String array
            String[] stringParams = new String[params.length];
            for (int i = 0; i < params.length; i++) {
                stringParams[i] = params[i] != null ? params[i].toString() : "";
            }
            
            // Llamar al método consultarBD del ProxyCache (que puede usar cache o backend)
            String[] resultArray = servicePrx.consultarBD(sqlQuery, stringParams);
            
            // Convertir resultado de String array a List<Map<String, Object>>
            List<Map<String, java.lang.Object>> resultList = new java.util.ArrayList<>();
            
            if (resultArray.length >= 2 && !resultArray[0].equals("ERROR")) {
                Map<String, java.lang.Object> resultMap = new java.util.HashMap<>();
                
                // Los resultados vienen en pares key-value
                for (int i = 0; i < resultArray.length - 1; i += 2) {
                    String key = resultArray[i];
                    String value = resultArray[i + 1];
                    resultMap.put(key, value);
                }
                
                resultList.add(resultMap);
            }
            
            return resultList;
            
        } catch (Exception e) {
            System.err.println("Error al consultar base de datos a través de ProxyCache: " + e.getMessage());
            throw new RuntimeException("Error en consulta remota", e);
        }
    }

    private void run() {
        boolean continuar = true;
        while (continuar) {
            String cedula = pedirCedula();
            mostrarPuesto(cedula);
            System.out.print("\n¿Desea consultar otra cédula? (S/N): ");
            continuar = scanner.nextLine().trim().equalsIgnoreCase("S");
        }
        System.out.println("Gracias por usar el sistema de consulta.");
    }

    private String pedirCedula() {
        String cedula;
        while (true) {
            System.out.println("Bienvenido al sistema de consulta de puesto de votacion");
            System.out.println("--------------------------------");
            System.out.print("Ingrese su cédula: ");
            cedula = scanner.nextLine().trim();
            if (cedula.matches("\\d+")) break;
            System.out.println("La cédula debe ser numérica. Intente de nuevo.");
        }
        return cedula;
    }

    private void mostrarPuesto(String cedula) {
        String sql = """
            SELECT mv.consecutive AS mesa, d.nombre AS departamento, m.nombre AS municipio,
                   pv.id AS puesto_id, pv.nombre AS puesto_nombre, pv.direccion AS direccion
            FROM ciudadano c
            JOIN mesa_votacion mv ON mv.id = c.mesa_id
            JOIN puesto_votacion pv ON pv.id = mv.puesto_id
            JOIN municipio m ON m.id = pv.municipio_id
            JOIN departamento d ON d.id = m.departamento_id
            WHERE c.documento = ?""";
        try {
            List<Map<String, java.lang.Object>> rows = getInfoBDWithParams(sql, cedula);
            if (rows.isEmpty()) {
                System.out.println("No se encontró información para la cédula " + cedula);
            } else {
                Map<String, Object> r = rows.get(0);
                System.out.println("\n----- Información de votación -----");
                System.out.println("Departamento : " + r.get("departamento"));
                System.out.println("Municipio    : " + r.get("municipio"));
                System.out.println("Puesto (ID)  : " + r.get("puesto_id") + " - " + r.get("puesto_nombre"));
                System.out.println("Dirección    : " + r.get("direccion"));
                System.out.println("Mesa         : " + r.get("mesa"));
            }
        } catch (Exception e) {
            System.err.println("Error al obtener información: " + e.getMessage());
        }
    }
} 