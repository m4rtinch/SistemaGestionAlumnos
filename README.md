**Guía de instalación y configuración de Sistema de Gestión de Alumnos**

1. Abrir una terminal y escribir: "git clone https://github.com/m4rtinch/SistemaGestionAlumnos.git"
2. En Intellij abrir el directorio del proyescto "SitemaGestionAlumnos"
3. Ir a "https://dev.mysql.com/downloads/connector/j/"
4. Seleccionar Ubuntu Linux, la versión instalada y descargar.
5. En una terminal escribir "sudo dpkg . i (nombre del archivo descargado de la página)" o instalarlo haciendo doble
   click desde la interfaz gráfica.
6. En Intellij, con el proyefcto abierto, ir a: 3 rayitas > Project Structure > Modules
7. Hacer click derecho en "mysql. connector. j. 9.7.0 " y Editar
8. Apretar el símbolo "+" y elegir el .jar en  "/usr/share/mysql. connector. j. 9.7.0.jar"
9. Eliminar si hay otra clase en el mismo modulo que interfiere con el símbolo ". ".
10. Apply y Ok
11. Levantar el servicio (si no lo esta) de mysql con "sudo systemctl start mariadb"
12. Ir a MySqlWorkbench y acceder a la conexión de LocalHost.
13. Ir a File > Run SQL Script y elegir el Script que esta en la raiz del proyecto de "gestion_alumnos.sql" y aceptar.
14. En Intellij, cambiar el USER por el usuario con el que accediste a localhost y el PASSWORD con la contraseña con la
    que accediste.
    (Aclaracion) Mañana vamos a tener que cambiar el URL y el Usuario por el de la BD del Martín (o sea su IP y un
    usuario que nos conceda el acceso)
15. En el módulo SistemaGestion apretar el símbolo de Play y probar las funciones y contrastar con la Base de datos.