# 🍰 La Casa del Chantilly - E-Commerce System (Happy Pack)

Este repositorio contiene el sistema completo (Frontend y Backend) modernizado para "La Casa del Chantilly". 

## 🚀 Requisitos Previos

1. **Java 17+** instalado (`java -version`).
2. **Maven** instalado (o usar el `mvnw` incluido).
3. **MySQL 8+** corriendo en el puerto 3306.
4. Entorno local en Windows, Mac o Linux.

---

## 🛠️ Instrucciones de Instalación Pasos a Paso

### 1. Base de Datos
1. Inicia tu servidor MySQL local (XAMPP, Workbench, o Docker).
2. Crea una base de datos vacía llamada `prueba_db`:
   ```sql
   CREATE DATABASE prueba_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

### 2. Configuración Backend
1. Renombra el archivo `.env.example` a `.env` si tu entorno lee archivos dot-env, o asegúrate de que el archivo `src/main/resources/application.properties` esté apuntando correctamente a tu MySQL local.
2. (Opcional Primera Vez) En `application.properties`, temporalmente cambia:
   ```properties
   spring.jpa.hibernate.ddl-auto=update
   ```
   Esto forzará a Hibernate a crear absolutamente todas las tablas mágicamente al correr el proyecto. ¡Luego de correrlo, devuélvelo a `none`!

### 3. Ejecución del Desarrollo
1. Abre tu terminal en la carpeta raíz del proyecto.
2. Ejecuta:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
3. El backend inicializará en `http://localhost:8081`. 

### 4. Poblado de Datos (Seeding)
Una vez el backend haya creado las tablas (si usaste `ddl-auto=update`), abre tu cliente MySQL e inyecta el catálogo corriendo secuencialmente los siguientes scripts de la carpeta `src/main/resources/sql/`:
1. `seed_demo_operativo.sql` (Crea clientes, roles, catálogo basico, promociones y variables de sistema).
2. `2026-04-08_agregar_campos_escalabilidad.sql`
3. `2026-04-08_agregar_imagenes_y_reset.sql` (Pinta las imágenes dinámicamente y da paso al login avanzado).

---

## 🖥️ Acceso a la Plataforma (Frontend Dinámico)

A diferencia de React/Angular, este proyecto funciona sirviendo HTML directamente desde los estáticos de Spring Boot, maximizado visualmente con *Bootstrap 5.3+* para full-responsive UX.

* **Vista Cliente:** [http://localhost:8081/](http://localhost:8081/)
* **Vista Administrador:** [http://localhost:8081/pages/admin/dashboard.html](http://localhost:8081/pages/admin/dashboard.html)

**Credenciales Globales de Acceso:**
* **Admin:** `admin@casachantilly.pe` / Contraseña: `password123`
* **Cliente:** `cliente@gmail.com` / Contraseña: `password123`

---

## 🐛 Solución de Problemas (Troubleshooting)

| Error | Solución |
| :--- | :--- |
| `Web server failed to start. Port 8081 was already in use.` | Otro servicio usa el 8081. Ve a `application.properties` y cambia a `server.port=8082`. |
| `Access denied for user 'root'@'localhost'` | Verifica que la contraseña del root en MySQL local esté en blanco (XAMPP). Si tienes clave, módificala en `application.properties`. |
| Las imágenes no cargan | Revisa tu conexión a internet, las imágenes se traen desde el CDN de Unsplash vía API de DB. |
