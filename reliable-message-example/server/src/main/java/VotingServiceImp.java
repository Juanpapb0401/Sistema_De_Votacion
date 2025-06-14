import java.util.List;
import java.util.Map;

import com.zeroc.Ice.Current;

import app.Service;

public class VotingServiceImp implements Service {

    @Override
    public void print(Current current) {
        System.out.println("Servicio de votación funcionando correctamente!");
    }

    @Override
    public String[] consultarBD(String sqlQuery, String[] params, Current current) {
        try {
            System.out.println("Consultando BD: " + sqlQuery);
            
            // Usar los métodos estáticos existentes del Server
            List<Map<String, Object>> results;
            if (params != null && params.length > 0) {
                results = Server.getInfo(sqlQuery, (Object[]) params);
            } else {
                results = Server.getInfo(sqlQuery);
            }
            
            // Convertir resultados a String array (simplificado)
            if (results.isEmpty()) {
                return new String[0];
            }
            
            // Convertir el primer resultado a formato string
            Map<String, Object> firstResult = results.get(0);
            String[] resultArray = new String[firstResult.size() * 2]; // key-value pairs
            int index = 0;
            
            for (Map.Entry<String, Object> entry : firstResult.entrySet()) {
                resultArray[index++] = entry.getKey();
                resultArray[index++] = entry.getValue() != null ? entry.getValue().toString() : "";
            }
            
            return resultArray;
            
        } catch (Exception e) {
            System.err.println("Error en consultarBD: " + e.getMessage());
            e.printStackTrace();
            return new String[]{"ERROR", e.getMessage()};
        }
    }
} 