module app{

    // Definir tipos de secuencia
    sequence<string> StringSeq;

    interface Service{
        void print();
        StringSeq consultarBD(string sqlQuery, StringSeq params);
    }
}