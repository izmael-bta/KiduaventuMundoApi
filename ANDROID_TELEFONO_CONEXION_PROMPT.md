# Prompt Para Android En Telefono Fisico

```txt
Actúa como Android developer senior. Trabaja SOLO en este proyecto Android abierto.

Objetivo:
- Que la app funcione en teléfono físico y se conecte al backend Ktor en mi PC.
- NO cambiar arquitectura ni lógica, solo configuración de conexión.

Reglas:
1) No refactorizar.
2) No mover ni renombrar clases.
3) No tocar ViewModels/repositorios/UI salvo valores de red.
4) Mantener endpoints y contratos actuales.

Cambios obligatorios:
1) Buscar dónde se define la base URL (Retrofit/ApiClient/constants/BuildConfig) y reemplazar:
   - de: http://10.0.2.2:8080
   - a:  http://192.168.1.50:8080
   (usar esta IP como ejemplo; dejarla en un solo lugar fácil de cambiar)
2) Verificar que endpoints se mantengan exactamente:
   - POST /users
   - GET /users/{nickname}
   - POST /login
3) En AndroidManifest.xml asegurar:
   - <uses-permission android:name="android.permission.INTERNET" />
4) En <application> asegurar:
   - android:usesCleartextTraffic="true"
5) No cambiar nada más.

Validación rápida:
- Confirmar que compilación no se rompe.
- Mostrar archivos modificados y diff exacto.

Entrega:
- Lista de archivos cambiados.
- Qué línea quedó con la nueva base URL.
- Confirmación de que solo se tocaron valores de conexión/permisos.
```
