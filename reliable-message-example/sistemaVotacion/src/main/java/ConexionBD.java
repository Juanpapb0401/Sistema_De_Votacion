package sistemaVotacion;

import java.util.List;
import java.util.Map;

public class ConexionBD {

    public List<Map<String, Object>> getInfoBD(String sqlQuery) {
        try {
            Class<?> serverClazz = Class.forName("Server");
            java.lang.reflect.Method method = serverClazz.getMethod("getInfo", String.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = (List<Map<String, Object>>) method.invoke(null, sqlQuery);
            return result;
        } catch (Exception e) {
            Throwable root = (e instanceof java.lang.reflect.InvocationTargetException)
                             ? ((java.lang.reflect.InvocationTargetException) e).getTargetException()
                             : e;
            root.printStackTrace();
            System.err.println("Error detallado: " + root.getMessage());
            throw new RuntimeException("No se pudo obtener la información del servidor: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getInfoBDWithParams(String sqlQuery, Object... params) {
        try {
            Class<?> serverClazz = Class.forName("Server");
            java.lang.reflect.Method method = serverClazz.getMethod("getInfo", String.class, Object[].class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = (List<Map<String, Object>>) method.invoke(null, sqlQuery, params);
            return result;
        } catch (Exception e) {
            Throwable root = (e instanceof java.lang.reflect.InvocationTargetException)
                             ? ((java.lang.reflect.InvocationTargetException) e).getTargetException()
                             : e;
            root.printStackTrace();
            System.err.println("Error detallado: " + root.getMessage());
            throw new RuntimeException("No se pudo obtener la información del servidor (con params): " + e.getMessage(), e);
        }
    }

    public void addVote(int candidateId, int userId) {
        try {
            Class<?> serverClazz = Class.forName("Server");
            java.lang.reflect.Method method = serverClazz.getMethod("addNewVotes", int.class, int.class);
            method.invoke(null, candidateId, userId);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo insertar el voto a través del servidor: " + e.getMessage(), e);
        }
    }
} 