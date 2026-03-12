# Prompt Para Ajustar Conexion Android (Sin Cambiar Arquitectura)

```txt
Actúa como Android developer senior. Trabaja SOLO en este proyecto Android abierto.

Objetivo:
- NO cambiar arquitectura, capas, ni estructura.
- SOLO reemplazar/ajustar valores de configuración para que la app apunte al backend local.

Reglas estrictas:
1) No refactorizar.
2) No renombrar clases.
3) No mover archivos.
4) No cambiar contratos de modelos ni endpoints.
5) Solo editar valores de URL/permisos/red necesarios.

Cambios a realizar:
1) Buscar dónde se define la base URL (Retrofit/ApiClient/BuildConfig/constants) y reemplazarla por:
   http://10.0.2.2:8080
2) Verificar que los endpoints usados sigan exactamente:
   - POST /users
   - GET /users/{nickname}
   - POST /login
3) En AndroidManifest.xml agregar (si falta):
   <uses-permission android:name="android.permission.INTERNET" />
4) En el tag <application> agregar (si falta):
   android:usesCleartextTraffic="true"
   (solo para desarrollo local con HTTP).
5) No tocar lógica de negocio, ViewModel, repositorios, navegación, UI, ni arquitectura.

Entregable:
- Lista exacta de archivos modificados.
- Diff/resumen línea por línea de cada cambio.
- Confirmación final de que solo se cambiaron valores de conexión y permisos.
```
