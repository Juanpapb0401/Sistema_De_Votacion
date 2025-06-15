package test;

import dispositivoPersonal.QueryStationImpl;


public class TestQueryStation {

    private static final String CEDULA_NO_EXISTE = "111111111";
    private static final String CEDULA_EXISTE    = "130204799";
    private static final String CEDULA_CACHE     = "711674049";

    public static void main(String[] args) {
        System.out.println("Iniciando pruebas de QueryStation...\n");
        QueryStationImpl qs = new QueryStationImpl();

        String r1 = qs.query(CEDULA_NO_EXISTE, null);
        System.out.println("Test 1 - Cedula inexistente");
        System.out.println("Respuesta: " + r1 + "\n");

        String r2 = qs.query(CEDULA_EXISTE, null);
        System.out.println("Test 2 - Cedula existente");
        System.out.println("Respuesta: " + r2 + "\n");

        if (!"NOT_FOUND".equals(r2) && !r2.startsWith("ERROR")) {
            String[] parts = r2.split(",");
            System.out.println("Campos recibidos: " + parts.length);
        }

        System.out.println("Test 3 - Cache (dos consultas a la misma cédula)\n");
        long t1 = System.currentTimeMillis();
        String cacheResp1 = qs.query(CEDULA_CACHE, null);
        long d1 = System.currentTimeMillis() - t1;

        long t2 = System.currentTimeMillis();
        String cacheResp2 = qs.query(CEDULA_CACHE, null);
        long d2 = System.currentTimeMillis() - t2;

        System.out.println("Primera consulta  -> " + d1 + " ms. Respuesta: " + cacheResp1);
        System.out.println("Segunda consulta  -> " + d2 + " ms. Respuesta: " + cacheResp2);

        System.out.println("Pruebas de QueryStation completadas.");
    }
} 