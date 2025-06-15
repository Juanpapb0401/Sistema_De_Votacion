module app{

    // Definir tipos de secuencia
    sequence<string> StringSeq;

    interface Service{
        void print();
        StringSeq consultarBD(string sqlQuery, StringSeq params);
    }

    interface VoteStation {
        int vote(string document, int candidateId);
    }

    interface QueryStation {
        string query(string document);
    }
}