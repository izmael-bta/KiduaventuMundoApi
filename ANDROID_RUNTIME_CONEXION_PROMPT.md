# Prompt Para Corregir Conexion Android En Runtime

```txt
Actúa como Android senior y trabaja SOLO en este proyecto Android abierto.

Objetivo:
- Corregir la conexión en runtime para que la app use mi backend local desde teléfono físico.
- NO cambiar arquitectura, capas, ni lógica de negocio.
- SOLO ajustar configuración de red/base URL y permisos.

Contexto confirmado:
- El backend responde OK en: http://192.168.0.13:8080/health
- Por tanto, el problema está en configuración Android (URL/runtime/permisos).

Tareas obligatorias:
1) Localiza TODAS las fuentes de base URL (Retrofit, ApiClient, Constants, BuildConfig, productFlavors, debug/release).
2) Unifica para que en runtime use exactamente:
   http://192.168.0.13:8080
3) Elimina o corrige cualquier valor viejo como:
   - http://10.0.2.2:8080
   - http://localhost:8080
   - cualquier URL mock o placeholder
4) Verifica UserApi/endpoints sin cambiar contratos:
   - POST /users
   - GET /users/{nickname}
   - POST /login
5) En AndroidManifest.xml asegurar:
   - <uses-permission android:name="android.permission.INTERNET" />
   - <application ... android:usesCleartextTraffic="true" ...>
6) No tocar UI, navegación, ViewModel, repositorios, ni arquitectura.

Validación requerida:
1) Agrega un log temporal en la creación de Retrofit para imprimir baseUrl efectiva en runtime.
2) Compila el proyecto y confirma que no rompe.
3) Entrega:
   - lista de archivos modificados
   - diff exacto
   - valor final de base URL usado en runtime
   - confirmación de que SOLO se cambiaron valores/config de conexión

Importante:
- Haz cambios mínimos y seguros.
- No refactors.
```
