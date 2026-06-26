package com.huly.backend.domain.port;

/**
 * Puerto para subir archivos a un almacenamiento de objetos (bucket).
 * La implementación se resuelve por adaptador (ej. Supabase Storage vía S3).
 */
public interface FileStoragePort {

    /**
     * Sube el contenido al bucket y devuelve la URL pública del objeto.
     *
     * @param content     bytes del archivo a subir
     * @param objectKey   ruta/clave del objeto dentro del bucket (ej. "goals/abc.png")
     * @param contentType MIME type del contenido (ej. "image/png")
     * @return URL pública permanente del objeto subido
     */
    String upload(byte[] content, String objectKey, String contentType);
}
