package dispositivoPersonal;

import java.util.List;
import java.util.Map;

import com.zeroc.Ice.Current;

import app.QueryStation;


public class QueryStationImpl implements QueryStation {

    private static final String SQL = """
            SELECT mv.consecutive AS mesa, d.nombre AS departamento, m.nombre AS municipio,
                   pv.id AS puesto_id, pv.nombre AS puesto_nombre, pv.direccion AS direccion
            FROM ciudadano c
            JOIN mesa_votacion mv ON mv.id = c.mesa_id
            JOIN puesto_votacion pv ON pv.id = mv.puesto_id
            JOIN municipio m ON m.id = pv.municipio_id
            JOIN departamento d ON d.id = m.departamento_id
            WHERE c.documento = ?""";

    @Override
    public String query(String document, Current current) {
        ConexionBD conexion = new ConexionBD();
        try {
            List<Map<String, Object>> rows = conexion.getInfoBDWithParams(SQL, document);
            if (rows.isEmpty()) {
                return "NOT_FOUND";
            }
            Map<String, Object> r = rows.get(0);
            return String.join(",",
                    safe(r.get("departamento")),
                    safe(r.get("municipio")),
                    safe(r.get("puesto_id")),
                    safe(r.get("puesto_nombre")),
                    safe(r.get("direccion")),
                    safe(r.get("mesa")));
        } catch (Exception e) {
            return "ERROR:" + e.getMessage();
        }
    }

    private static String safe(Object o) {
        return o == null ? "" : o.toString();
    }
} 