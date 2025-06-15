package test;

import sistemaVotacion.ConexionBD;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación falsa de ConexionBD para pruebas
 */
public class FakeConexionBD extends ConexionBD {
    
    // Base de datos simulada con documentos y sus mesas asignadas
    private static final Map<String, Integer> CIUDADANOS = new HashMap<>();
    
    static {
        // Documentos en mesa 1
        CIUDADANOS.put("711674049", 1);  // Fanny Aguilar
        CIUDADANOS.put("111111111", 1);
        CIUDADANOS.put("222222222", 1);
        
        // Documentos en mesa 2
        CIUDADANOS.put("130204799", 2);  // Nidia Tejera
        CIUDADANOS.put("333333333", 2);
        
        // Documentos en mesa 3
        CIUDADANOS.put("527760767", 3);  // Clementina Oliveras
        CIUDADANOS.put("444444444", 3);
    }
    
    // Registro de votos simulado (documentos que ya votaron)
    private static final List<String> VOTOS_REGISTRADOS = new ArrayList<>();
    
    @Override
    public List<Map<String, Object>> getInfoBDWithParams(String sqlQuery, Object... params) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        if (params.length > 0 && params[0] != null) {
            String documento = params[0].toString();
            
            // Si el documento está en nuestra "base de datos"
            if (CIUDADANOS.containsKey(documento)) {
                Map<String, Object> row = new HashMap<>();
                row.put("mesa_id", CIUDADANOS.get(documento));
                result.add(row);
            }
        }
        
        return result;
    }
    
    @Override
    public void addVote(int candidateId, int userId) {
        // Simulamos añadir un voto
        String documento = String.valueOf(userId);
        if (!VOTOS_REGISTRADOS.contains(documento)) {
            VOTOS_REGISTRADOS.add(documento);
        }
    }
    
    // Método para simular si un documento ya votó
    public static boolean haVotado(String documento) {
        return VOTOS_REGISTRADOS.contains(documento);
    }
    
    // Método para registrar un voto manualmente (para pruebas)
    public static void registrarVoto(String documento) {
        if (!VOTOS_REGISTRADOS.contains(documento)) {
            VOTOS_REGISTRADOS.add(documento);
        }
    }
    
    // Método para limpiar todos los votos (para reiniciar pruebas)
    public static void limpiarVotos() {
        VOTOS_REGISTRADOS.clear();
    }
    
    // Método para agregar un ciudadano a la base de datos simulada
    public static void agregarCiudadano(String documento, int mesaId) {
        CIUDADANOS.put(documento, mesaId);
    }
} 