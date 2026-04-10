-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: prueba_db
-- ------------------------------------------------------
-- Server version	5.5.5-10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `alergeno`
--

DROP TABLE IF EXISTS `alergeno`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `alergeno` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(80) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alergeno`
--

LOCK TABLES `alergeno` WRITE;
/*!40000 ALTER TABLE `alergeno` DISABLE KEYS */;
INSERT INTO `alergeno` VALUES (6,'Colorantes artificiales'),(7,'Conservantes'),(4,'Frutos secos'),(1,'Gluten'),(3,'Huevo'),(2,'Lactosa'),(5,'Soja');
/*!40000 ALTER TABLE `alergeno` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auditoria`
--

DROP TABLE IF EXISTS `auditoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auditoria` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tabla_afectada` varchar(100) NOT NULL,
  `registro_id` int(11) NOT NULL,
  `accion` enum('INSERT','UPDATE','DELETE') NOT NULL,
  `usuario_id` int(11) DEFAULT NULL,
  `valores_anteriores` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `valores_nuevos` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `ip_origen` varchar(45) DEFAULT NULL,
  `fecha` datetime NOT NULL DEFAULT current_timestamp(),
  `id_correlacion` varchar(64) DEFAULT NULL,
  `modulo` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_aud_usu` (`usuario_id`),
  KEY `idx_audit_tabla_reg` (`tabla_afectada`,`registro_id`),
  KEY `idx_audit_fecha` (`fecha`),
  CONSTRAINT `fk_aud_usu` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE SET NULL,
  CONSTRAINT `auditoria_chk_1` CHECK (json_valid(`valores_anteriores`)),
  CONSTRAINT `auditoria_chk_2` CHECK (json_valid(`valores_nuevos`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auditoria`
--

LOCK TABLES `auditoria` WRITE;
/*!40000 ALTER TABLE `auditoria` DISABLE KEYS */;
/*!40000 ALTER TABLE `auditoria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carrito`
--

DROP TABLE IF EXISTS `carrito`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carrito` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `usuario_id` int(11) NOT NULL,
  `promocion_id` int(11) DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT current_timestamp(),
  `fecha_actualizacion` datetime DEFAULT NULL ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `fk_car_usu` (`usuario_id`),
  KEY `fk_car_prom` (`promocion_id`),
  CONSTRAINT `fk_car_prom` FOREIGN KEY (`promocion_id`) REFERENCES `promocion` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_car_usu` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carrito`
--

LOCK TABLES `carrito` WRITE;
/*!40000 ALTER TABLE `carrito` DISABLE KEYS */;
INSERT INTO `carrito` VALUES (1,1,NULL,'2026-04-02 13:00:18',NULL),(2,2,NULL,'2026-04-02 13:04:50',NULL),(3,4,NULL,'2026-04-03 17:54:52',NULL),(4,6,NULL,'2026-04-08 00:50:09','2026-04-08 03:27:22'),(5,10,NULL,'2026-04-08 13:25:07','2026-04-08 13:25:11');
/*!40000 ALTER TABLE `carrito` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carrito_detalle`
--

DROP TABLE IF EXISTS `carrito_detalle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carrito_detalle` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `carrito_id` int(11) NOT NULL,
  `variante_id` int(11) NOT NULL,
  `cantidad` int(11) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `fk_cd_car` (`carrito_id`),
  KEY `fk_cd_var` (`variante_id`),
  CONSTRAINT `fk_cd_car` FOREIGN KEY (`carrito_id`) REFERENCES `carrito` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cd_var` FOREIGN KEY (`variante_id`) REFERENCES `producto_variante` (`id`),
  CONSTRAINT `carrito_detalle_chk_1` CHECK (`cantidad` > 0)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carrito_detalle`
--

LOCK TABLES `carrito_detalle` WRITE;
/*!40000 ALTER TABLE `carrito_detalle` DISABLE KEYS */;
/*!40000 ALTER TABLE `carrito_detalle` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categoria`
--

DROP TABLE IF EXISTS `categoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categoria` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `slug` varchar(120) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categoria`
--

LOCK TABLES `categoria` WRITE;
/*!40000 ALTER TABLE `categoria` DISABLE KEYS */;
INSERT INTO `categoria` VALUES (1,'Tortas Enteras','tortas-enteras',1),(2,'Porciones','porciones',1),(3,'Bocaditos','bocaditos',1),(4,'Postres Especiales','postres-especiales',1);
/*!40000 ALTER TABLE `categoria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_mensaje`
--

DROP TABLE IF EXISTS `chat_mensaje`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_mensaje` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pedido_id` int(11) NOT NULL,
  `usuario_id` int(11) NOT NULL,
  `mensaje` text NOT NULL,
  `leido` tinyint(1) NOT NULL DEFAULT 0,
  `fecha` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `fk_chat_usu` (`usuario_id`),
  KEY `idx_chat_pedido_fecha` (`pedido_id`,`fecha`),
  CONSTRAINT `fk_chat_ped` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`),
  CONSTRAINT `fk_chat_usu` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_mensaje`
--

LOCK TABLES `chat_mensaje` WRITE;
/*!40000 ALTER TABLE `chat_mensaje` DISABLE KEYS */;
/*!40000 ALTER TABLE `chat_mensaje` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_pedido`
--

DROP TABLE IF EXISTS `detalle_pedido`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_pedido` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pedido_id` int(11) NOT NULL,
  `variante_id` int(11) NOT NULL,
  `nombre_snapshot` varchar(200) NOT NULL,
  `precio_unitario_snapshot` decimal(10,2) NOT NULL,
  `cantidad` int(11) NOT NULL,
  `subtotal_linea` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_dp_pedido` (`pedido_id`),
  KEY `idx_dp_variante` (`variante_id`),
  CONSTRAINT `fk_dp_ped` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`),
  CONSTRAINT `fk_dp_var` FOREIGN KEY (`variante_id`) REFERENCES `producto_variante` (`id`),
  CONSTRAINT `detalle_pedido_chk_1` CHECK (`cantidad` > 0)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_pedido`
--

LOCK TABLES `detalle_pedido` WRITE;
/*!40000 ALTER TABLE `detalle_pedido` DISABLE KEYS */;
INSERT INTO `detalle_pedido` VALUES (1,1,1,'Selva Negra Clasica - Entera 20cm',95.00,1,95.00),(2,1,5,'Porcion Tres Leches - Porcion Individual',12.00,1,12.00),(3,2,3,'Cheesecake de Maracuya - Entera 18cm',88.00,1,88.00),(4,3,7,'Brownie Clasico - Caja x6',38.00,1,38.00),(5,3,6,'Porcion Red Velvet - Porcion Individual',13.50,1,13.50),(6,4,7,'Brownie Clasico - Caja x6',38.00,1,38.00),(7,5,7,'Brownie Clasico - Caja x6',38.00,2,76.00),(8,5,8,'Tarta de Frutas - Mediana',72.00,1,72.00);
/*!40000 ALTER TABLE `detalle_pedido` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `direccion`
--

DROP TABLE IF EXISTS `direccion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `direccion` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `usuario_id` int(11) NOT NULL,
  `zona_id` int(11) DEFAULT NULL,
  `etiqueta` varchar(80) DEFAULT NULL,
  `direccion_completa` text NOT NULL,
  `referencia` text DEFAULT NULL,
  `destinatario_nombre` varchar(150) DEFAULT NULL,
  `destinatario_telefono` varchar(25) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  `es_principal` tinyint(1) NOT NULL DEFAULT 0,
  `latitud` decimal(10,8) DEFAULT NULL,
  `longitud` decimal(11,8) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_dir_usu` (`usuario_id`),
  KEY `fk_dir_zona` (`zona_id`),
  CONSTRAINT `fk_dir_usu` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_dir_zona` FOREIGN KEY (`zona_id`) REFERENCES `zona_envio` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `direccion`
--

LOCK TABLES `direccion` WRITE;
/*!40000 ALTER TABLE `direccion` DISABLE KEYS */;
INSERT INTO `direccion` VALUES (1,3,1,'Casa','Av. Pardo 123, Miraflores','Edificio verde, depto 402','Cliente Prueba','987654321',1,1,-12.12120000,-77.02970000),(2,3,3,'Oficina','Jr. Empresarial 890, Surco','Torre A, piso 5','Cliente Prueba','987654321',1,0,-12.13700000,-76.99130000),(3,10,3,'Casa de Rous','Av los girasoles','caseta','AV','9041231231',1,1,NULL,NULL);
/*!40000 ALTER TABLE `direccion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estado_pedido`
--

DROP TABLE IF EXISTS `estado_pedido`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estado_pedido` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(80) NOT NULL,
  `orden` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`),
  UNIQUE KEY `orden` (`orden`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estado_pedido`
--

LOCK TABLES `estado_pedido` WRITE;
/*!40000 ALTER TABLE `estado_pedido` DISABLE KEYS */;
INSERT INTO `estado_pedido` VALUES (1,'Pendiente de pago',1),(2,'Pago confirmado',2),(3,'En preparacion',3),(4,'Listo para recoger',4),(5,'En ruta',5),(6,'Entregado',6),(7,'Cancelado',7),(8,'Rechazado',8);
/*!40000 ALTER TABLE `estado_pedido` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estado_pedido_historial`
--

DROP TABLE IF EXISTS `estado_pedido_historial`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estado_pedido_historial` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pedido_id` int(11) NOT NULL,
  `estado_id` int(11) NOT NULL,
  `usuario_id` int(11) DEFAULT NULL,
  `observacion` text DEFAULT NULL,
  `fecha` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `fk_eph_est` (`estado_id`),
  KEY `fk_eph_usu` (`usuario_id`),
  KEY `idx_eph_pedido` (`pedido_id`,`fecha`),
  CONSTRAINT `fk_eph_est` FOREIGN KEY (`estado_id`) REFERENCES `estado_pedido` (`id`),
  CONSTRAINT `fk_eph_ped` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`),
  CONSTRAINT `fk_eph_usu` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estado_pedido_historial`
--

LOCK TABLES `estado_pedido_historial` WRITE;
/*!40000 ALTER TABLE `estado_pedido_historial` DISABLE KEYS */;
INSERT INTO `estado_pedido_historial` VALUES (1,1,1,3,'Pedido creado','2026-04-05 03:20:40'),(2,1,2,2,'Pago confirmado por admin','2026-04-05 03:50:40'),(3,1,6,2,'Pedido entregado','2026-04-06 03:20:40'),(4,2,1,3,'Pedido creado','2026-04-06 03:20:40'),(5,2,2,2,'Pago confirmado','2026-04-06 03:40:40'),(6,2,5,2,'Pedido en ruta','2026-04-07 03:20:40'),(7,3,1,3,'Pedido creado','2026-04-07 03:20:40'),(8,3,2,2,'Pago confirmado','2026-04-07 03:35:40'),(9,2,6,2,'Cambio de estado desde tablero: Entregado','2026-04-08 03:21:36'),(10,3,3,2,'Cambio de estado desde tablero: En preparacion','2026-04-08 03:21:42'),(13,3,4,2,'Cambio de estado desde tablero: Listo para recoger','2026-04-08 03:21:48'),(18,3,5,2,'Cambio de estado desde tablero: En ruta','2026-04-08 03:21:49'),(19,4,1,NULL,'Pedido creado — pendiente de pago','2026-04-08 03:27:41'),(20,4,2,NULL,'Pago aprobado — ref: culqi_ok_006b058f-f6d','2026-04-08 03:27:41'),(21,5,1,NULL,'Pedido creado — pendiente de pago','2026-04-08 14:33:59'),(22,5,2,NULL,'Pago aprobado — ref: culqi_ok_f48b8378-37f','2026-04-08 14:33:59');
/*!40000 ALTER TABLE `estado_pedido_historial` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_inventario_por_estado` AFTER INSERT ON `estado_pedido_historial` FOR EACH ROW BEGIN
    DECLARE v_estado_orden          INT DEFAULT 0;
    DECLARE v_estado_anterior_orden INT DEFAULT 0;

    SELECT orden INTO v_estado_orden
    FROM estado_pedido WHERE id = NEW.estado_id;

    SELECT ep.orden INTO v_estado_anterior_orden
    FROM estado_pedido_historial eph
    JOIN estado_pedido ep ON ep.id = eph.estado_id
    WHERE eph.pedido_id = NEW.pedido_id
      AND eph.id != NEW.id
    ORDER BY eph.fecha DESC
    LIMIT 1;

    IF v_estado_orden = 3 AND v_estado_anterior_orden < 3 THEN
        UPDATE producto_variante pv
        JOIN   detalle_pedido dp ON dp.variante_id = pv.id
        SET    pv.stock_disponible = pv.stock_disponible - dp.cantidad
        WHERE  dp.pedido_id = NEW.pedido_id;

        INSERT INTO inventario_movimiento
            (variante_id, tipo, cantidad, stock_resultante, motivo, pedido_id, usuario_id)
        SELECT dp.variante_id, 'SALIDA', dp.cantidad, pv.stock_disponible,
               CONCAT('Pedido #', NEW.pedido_id, ' - En preparacion'),
               NEW.pedido_id, NEW.usuario_id
        FROM detalle_pedido dp
        JOIN producto_variante pv ON pv.id = dp.variante_id
        WHERE dp.pedido_id = NEW.pedido_id;
    END IF;

    IF v_estado_orden = 7 AND v_estado_anterior_orden >= 3 AND v_estado_anterior_orden <= 6 THEN
        UPDATE producto_variante pv
        JOIN   detalle_pedido dp ON dp.variante_id = pv.id
        SET    pv.stock_disponible = pv.stock_disponible + dp.cantidad
        WHERE  dp.pedido_id = NEW.pedido_id;

        INSERT INTO inventario_movimiento
            (variante_id, tipo, cantidad, stock_resultante, motivo, pedido_id, usuario_id)
        SELECT dp.variante_id, 'ENTRADA', dp.cantidad, pv.stock_disponible,
               CONCAT('Cancelacion pedido #', NEW.pedido_id),
               NEW.pedido_id, NEW.usuario_id
        FROM detalle_pedido dp
        JOIN producto_variante pv ON pv.id = dp.variante_id
        WHERE dp.pedido_id = NEW.pedido_id;
    END IF;

    IF v_estado_orden IN (7, 8) THEN
        UPDATE franja_horaria f
        JOIN   pedido p ON p.franja_horaria_id = f.id
        SET    f.cupos_ocupados = GREATEST(0, f.cupos_ocupados - 1)
        WHERE  p.id = NEW.pedido_id;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `franja_horaria`
--

DROP TABLE IF EXISTS `franja_horaria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `franja_horaria` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `hora_inicio` time NOT NULL,
  `hora_fin` time NOT NULL,
  `cupos_totales` int(11) NOT NULL,
  `cupos_ocupados` int(11) NOT NULL DEFAULT 0,
  `tipo` enum('DELIVERY','RECOJO','AMBOS') NOT NULL DEFAULT 'AMBOS',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_franja` (`fecha`,`hora_inicio`,`hora_fin`),
  KEY `idx_franja_fecha_tipo` (`fecha`,`tipo`),
  CONSTRAINT `chk_cupos` CHECK (`cupos_ocupados` <= `cupos_totales`),
  CONSTRAINT `chk_horas` CHECK (`hora_fin` > `hora_inicio`),
  CONSTRAINT `franja_horaria_chk_1` CHECK (`cupos_totales` > 0),
  CONSTRAINT `franja_horaria_chk_2` CHECK (`cupos_ocupados` >= 0)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `franja_horaria`
--

LOCK TABLES `franja_horaria` WRITE;
/*!40000 ALTER TABLE `franja_horaria` DISABLE KEYS */;
INSERT INTO `franja_horaria` VALUES (1,'2026-04-09','09:00:00','11:00:00',12,3,'AMBOS'),(2,'2026-04-09','13:00:00','15:00:00',12,2,'AMBOS'),(3,'2026-04-09','18:00:00','20:00:00',12,0,'AMBOS'),(4,'2026-04-10','09:00:00','11:00:00',12,0,'AMBOS'),(5,'2026-04-10','13:00:00','15:00:00',12,0,'AMBOS'),(6,'2026-04-10','18:00:00','20:00:00',12,0,'AMBOS'),(7,'2026-04-11','09:00:00','11:00:00',12,0,'AMBOS'),(8,'2026-04-11','13:00:00','15:00:00',12,0,'AMBOS'),(9,'2026-04-11','18:00:00','20:00:00',12,0,'AMBOS');
/*!40000 ALTER TABLE `franja_horaria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventario_movimiento`
--

DROP TABLE IF EXISTS `inventario_movimiento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventario_movimiento` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `variante_id` int(11) NOT NULL,
  `tipo` enum('ENTRADA','SALIDA','AJUSTE') NOT NULL,
  `cantidad` int(11) NOT NULL,
  `stock_resultante` int(11) NOT NULL,
  `motivo` varchar(150) DEFAULT NULL,
  `pedido_id` int(11) DEFAULT NULL,
  `usuario_id` int(11) DEFAULT NULL,
  `fecha` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_inv_variante_fecha` (`variante_id`,`fecha`),
  CONSTRAINT `fk_inv_var` FOREIGN KEY (`variante_id`) REFERENCES `producto_variante` (`id`),
  CONSTRAINT `inventario_movimiento_chk_1` CHECK (`cantidad` > 0)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventario_movimiento`
--

LOCK TABLES `inventario_movimiento` WRITE;
/*!40000 ALTER TABLE `inventario_movimiento` DISABLE KEYS */;
INSERT INTO `inventario_movimiento` VALUES (1,1,'SALIDA',1,5,'Venta PED-DEMO-1001',1,2,'2026-04-05 03:20:40'),(2,3,'SALIDA',1,3,'Venta PED-DEMO-1002',2,2,'2026-04-06 03:20:40'),(3,7,'SALIDA',1,4,'Pedido #3 - En preparacion',3,2,'2026-04-08 03:21:42'),(4,6,'SALIDA',1,7,'Pedido #3 - En preparacion',3,2,'2026-04-08 03:21:42');
/*!40000 ALTER TABLE `inventario_movimiento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metodo_pago`
--

DROP TABLE IF EXISTS `metodo_pago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `metodo_pago` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(80) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metodo_pago`
--

LOCK TABLES `metodo_pago` WRITE;
/*!40000 ALTER TABLE `metodo_pago` DISABLE KEYS */;
INSERT INTO `metodo_pago` VALUES (1,'Tarjeta de credito/debito',1),(2,'Yape',1),(3,'Plin',1),(4,'Efectivo contra entrega',1),(5,'Transferencia bancaria',1);
/*!40000 ALTER TABLE `metodo_pago` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notificacion`
--

DROP TABLE IF EXISTS `notificacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notificacion` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `usuario_id` int(11) NOT NULL,
  `pedido_id` int(11) DEFAULT NULL,
  `canal` enum('EMAIL','SMS','WHATSAPP','PUSH') NOT NULL,
  `asunto` varchar(200) DEFAULT NULL,
  `mensaje` text NOT NULL,
  `leida` tinyint(1) NOT NULL DEFAULT 0,
  `intentos` int(11) NOT NULL DEFAULT 0,
  `proximo_intento` datetime DEFAULT NULL,
  `fecha_envio` datetime NOT NULL DEFAULT current_timestamp(),
  `estado_envio` enum('PENDIENTE','ENVIADA','ERROR','CANCELADA') NOT NULL DEFAULT 'PENDIENTE',
  `destino_canal` varchar(160) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_not_ped` (`pedido_id`),
  KEY `idx_notif_usuario_leida` (`usuario_id`,`leida`),
  CONSTRAINT `fk_not_ped` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_not_usu` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notificacion`
--

LOCK TABLES `notificacion` WRITE;
/*!40000 ALTER TABLE `notificacion` DISABLE KEYS */;
INSERT INTO `notificacion` VALUES (1,3,1,'EMAIL','Pedido entregado','Tu pedido PED-DEMO-1001 fue entregado con exito.',0,1,NULL,'2026-04-06 03:20:40','ENVIADA','cliente@gmail.com'),(2,3,2,'EMAIL','Pedido en ruta','Tu pedido PED-DEMO-1002 va en camino.',0,1,NULL,'2026-04-07 03:20:40','ENVIADA','cliente@gmail.com'),(3,3,2,'EMAIL','Estado de pedido actualizado','Tu pedido PED-DEMO-1002 cambió a estado: Entregado',0,1,NULL,'2026-04-08 03:21:36','ENVIADA','cliente@gmail.com'),(4,3,3,'EMAIL','Estado de pedido actualizado','Tu pedido PED-DEMO-1003 cambió a estado: En preparacion',0,1,NULL,'2026-04-08 03:21:43','ENVIADA','cliente@gmail.com'),(5,3,3,'EMAIL','Estado de pedido actualizado','Tu pedido PED-DEMO-1003 cambió a estado: Listo para recoger',0,1,NULL,'2026-04-08 03:21:48','ENVIADA','cliente@gmail.com'),(8,3,3,'EMAIL','Estado de pedido actualizado','Tu pedido PED-DEMO-1003 cambió a estado: En ruta',0,1,NULL,'2026-04-08 03:21:49','ENVIADA','cliente@gmail.com'),(12,6,4,'EMAIL','Pedido creado','Tu pedido PED-000004 fue creado y está pendiente de pago.',0,1,NULL,'2026-04-08 03:27:41','ENVIADA','kevin123@gmail.com'),(13,6,4,'EMAIL','Pedido confirmado','Tu pedido PED-000004 fue confirmado exitosamente.',0,1,NULL,'2026-04-08 03:27:41','ENVIADA','kevin123@gmail.com'),(14,10,5,'EMAIL','Pedido creado','Tu pedido PED-000005 fue creado y está pendiente de pago.',0,1,NULL,'2026-04-08 14:33:59','ENVIADA','rous@gmail.com'),(15,10,5,'EMAIL','Pedido confirmado','Tu pedido PED-000005 fue confirmado exitosamente.',0,1,NULL,'2026-04-08 14:33:59','ENVIADA','rous@gmail.com');
/*!40000 ALTER TABLE `notificacion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pago`
--

DROP TABLE IF EXISTS `pago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pago` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pedido_id` int(11) NOT NULL,
  `metodo_pago_id` int(11) NOT NULL,
  `monto` decimal(10,2) NOT NULL,
  `estado` enum('PENDIENTE','APROBADO','RECHAZADO','REEMBOLSADO') NOT NULL DEFAULT 'PENDIENTE',
  `referencia_externa` varchar(200) DEFAULT NULL,
  `intentos` int(11) NOT NULL DEFAULT 0,
  `proximo_intento` datetime DEFAULT NULL,
  `fecha` datetime NOT NULL DEFAULT current_timestamp(),
  `moneda` char(3) NOT NULL DEFAULT 'PEN',
  `id_transaccion_externa` varchar(120) DEFAULT NULL,
  `fecha_aprobacion` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_pago_id_transaccion_externa` (`id_transaccion_externa`),
  KEY `fk_pag_met` (`metodo_pago_id`),
  KEY `idx_pago_pedido` (`pedido_id`),
  KEY `idx_pago_estado_fecha` (`estado`,`fecha`),
  CONSTRAINT `fk_pag_met` FOREIGN KEY (`metodo_pago_id`) REFERENCES `metodo_pago` (`id`),
  CONSTRAINT `fk_pag_ped` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`),
  CONSTRAINT `pago_chk_1` CHECK (`monto` > 0)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pago`
--

LOCK TABLES `pago` WRITE;
/*!40000 ALTER TABLE `pago` DISABLE KEYS */;
INSERT INTO `pago` VALUES (1,1,1,123.94,'APROBADO','Culqi demo PED-DEMO-1001',1,NULL,'2026-04-05 03:20:40','PEN','TRX-DEMO-1001','2026-04-05 03:30:40'),(2,2,2,99.18,'APROBADO','Yape demo PED-DEMO-1002',1,NULL,'2026-04-06 03:20:40','PEN','TRX-DEMO-1002','2026-04-06 03:28:40'),(3,3,4,61.36,'PENDIENTE','Efectivo demo PED-DEMO-1003',0,NULL,'2026-04-07 03:20:40','PEN','TRX-DEMO-1003',NULL),(4,4,2,44.84,'APROBADO','culqi_ok_006b058f-f6d',1,NULL,'2026-04-08 03:27:41','PEN','culqi_ok_006b058f-f6d','2026-04-08 03:27:41'),(5,5,2,174.64,'APROBADO','culqi_ok_f48b8378-37f',1,NULL,'2026-04-08 14:33:59','PEN','culqi_ok_f48b8378-37f','2026-04-08 14:33:59');
/*!40000 ALTER TABLE `pago` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedido`
--

DROP TABLE IF EXISTS `pedido`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedido` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `usuario_id` int(11) NOT NULL,
  `direccion_id` int(11) DEFAULT NULL,
  `franja_horaria_id` int(11) DEFAULT NULL,
  `promocion_id` int(11) DEFAULT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  `descuento` decimal(10,2) NOT NULL DEFAULT 0.00,
  `costo_envio` decimal(10,2) NOT NULL DEFAULT 0.00,
  `impuestos` decimal(10,2) NOT NULL DEFAULT 0.00,
  `total` decimal(10,2) NOT NULL,
  `es_recojo_tienda` tinyint(1) NOT NULL DEFAULT 0,
  `fecha_creacion` datetime NOT NULL DEFAULT current_timestamp(),
  `codigo_pedido` varchar(30) DEFAULT NULL,
  `estado_actual_id` int(11) DEFAULT NULL,
  `fecha_actualizacion` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_pedido_codigo_pedido` (`codigo_pedido`),
  KEY `fk_ped_dir` (`direccion_id`),
  KEY `fk_ped_fra` (`franja_horaria_id`),
  KEY `fk_ped_prom` (`promocion_id`),
  KEY `idx_pedido_usuario` (`usuario_id`),
  KEY `idx_pedido_fecha` (`fecha_creacion`),
  KEY `idx_pedido_fecha_total` (`fecha_creacion`,`total`),
  KEY `fk_ped_estado_actual` (`estado_actual_id`),
  CONSTRAINT `fk_ped_dir` FOREIGN KEY (`direccion_id`) REFERENCES `direccion` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_ped_estado_actual` FOREIGN KEY (`estado_actual_id`) REFERENCES `estado_pedido` (`id`),
  CONSTRAINT `fk_ped_fra` FOREIGN KEY (`franja_horaria_id`) REFERENCES `franja_horaria` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_ped_prom` FOREIGN KEY (`promocion_id`) REFERENCES `promocion` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_ped_usu` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `pedido_chk_1` CHECK (`subtotal` >= 0),
  CONSTRAINT `pedido_chk_2` CHECK (`descuento` >= 0),
  CONSTRAINT `pedido_chk_3` CHECK (`costo_envio` >= 0),
  CONSTRAINT `pedido_chk_4` CHECK (`impuestos` >= 0),
  CONSTRAINT `pedido_chk_5` CHECK (`total` >= 0)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedido`
--

LOCK TABLES `pedido` WRITE;
/*!40000 ALTER TABLE `pedido` DISABLE KEYS */;
INSERT INTO `pedido` VALUES (1,3,1,1,1,108.00,12.00,8.50,19.44,123.94,0,'2026-04-05 03:20:40','PED-DEMO-1001',6,'2026-04-06 03:20:40'),(2,3,1,1,NULL,76.00,0.00,9.50,13.68,99.18,0,'2026-04-06 03:20:40','PED-DEMO-1002',6,'2026-04-08 03:21:36'),(3,3,1,1,NULL,52.00,0.00,0.00,9.36,61.36,1,'2026-04-07 03:20:40','PED-DEMO-1003',5,'2026-04-08 03:21:49'),(4,6,NULL,2,NULL,38.00,0.00,0.00,6.84,44.84,1,'2026-04-08 03:27:41','PED-000004',2,'2026-04-08 03:27:41'),(5,10,NULL,2,NULL,148.00,0.00,0.00,26.64,174.64,1,'2026-04-08 14:33:59','PED-000005',2,'2026-04-08 14:33:59');
/*!40000 ALTER TABLE `pedido` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_reservar_cupo_franja` BEFORE INSERT ON `pedido` FOR EACH ROW BEGIN
    DECLARE v_cupo_ok TINYINT(1);

    IF NEW.franja_horaria_id IS NOT NULL THEN
        SET v_cupo_ok = fn_verificar_cupo(
            NEW.franja_horaria_id,
            IF(NEW.es_recojo_tienda, 'RECOJO', 'DELIVERY')
        );

        IF v_cupo_ok = 0 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'La franja horaria seleccionada no tiene cupos disponibles.';
        END IF;

        UPDATE franja_horaria
        SET    cupos_ocupados = cupos_ocupados + 1
        WHERE  id = NEW.franja_horaria_id
          AND  cupos_ocupados < cupos_totales;

        IF ROW_COUNT() = 0 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cupo agotado al intentar reservar. Por favor elija otra franja.';
        END IF;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `personalizacion`
--

DROP TABLE IF EXISTS `personalizacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `personalizacion` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `detalle_pedido_id` int(11) NOT NULL,
  `sabor_bizcocho` varchar(100) DEFAULT NULL,
  `tipo_relleno` varchar(100) DEFAULT NULL,
  `color_decorado` varchar(100) DEFAULT NULL,
  `texto_pastel` varchar(200) DEFAULT NULL,
  `notas_cliente` text DEFAULT NULL,
  `imagen_referencia_url` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `detalle_pedido_id` (`detalle_pedido_id`),
  CONSTRAINT `fk_per_dp` FOREIGN KEY (`detalle_pedido_id`) REFERENCES `detalle_pedido` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `personalizacion`
--

LOCK TABLES `personalizacion` WRITE;
/*!40000 ALTER TABLE `personalizacion` DISABLE KEYS */;
/*!40000 ALTER TABLE `personalizacion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `producto`
--

DROP TABLE IF EXISTS `producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producto` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `categoria_id` int(11) NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  `fecha_creacion` datetime NOT NULL DEFAULT current_timestamp(),
  `fecha_actualizacion` datetime DEFAULT NULL,
  `slug` varchar(160) DEFAULT NULL,
  `imagen_url` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_producto_slug` (`slug`),
  KEY `idx_producto_categoria` (`categoria_id`),
  KEY `idx_producto_nombre` (`nombre`),
  CONSTRAINT `fk_prod_cat` FOREIGN KEY (`categoria_id`) REFERENCES `categoria` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `producto`
--

LOCK TABLES `producto` WRITE;
/*!40000 ALTER TABLE `producto` DISABLE KEYS */;
INSERT INTO `producto` VALUES (1,1,'Selva Negra Clasica','Torta de chocolate con crema y cerezas.',1,'2026-04-08 03:20:40',NULL,'selva-negra-clasica','https://images.unsplash.com/photo-1578985545062-69928b1d9587?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'),(2,1,'Cheesecake de Maracuya','Cheesecake cremoso con coulis de maracuya.',1,'2026-04-08 03:20:40',NULL,'cheesecake-maracuya','https://images.unsplash.com/photo-1533134242443-d4fd215305ad?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'),(3,2,'Porcion Tres Leches','Bizcocho humedo con mezcla de tres leches.',1,'2026-04-08 03:20:40','2026-04-08 13:18:44','porcion-tres-leches','https://images.unsplash.com/photo-1542826438-bd32f43d626f?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'),(4,2,'Porcion Red Velvet','Porcion individual de red velvet con frosting.',1,'2026-04-08 03:20:40','2026-04-08 13:18:43','porcion-red-velvet','https://images.unsplash.com/photo-1586788224331-9a74aa9a7810?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'),(5,3,'Brownie Clasico','Brownie artesanal de cacao intenso.',1,'2026-04-08 03:20:40','2026-04-08 13:21:59','brownie-clasico','https://images.unsplash.com/photo-1606313564200-e75d5e30476c?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'),(6,4,'Tarta de Frutas','Base crocante con crema pastelera y frutas frescas.',1,'2026-04-08 03:20:40','2026-04-08 13:22:01','tarta-de-frutas','https://images.unsplash.com/photo-1565958011703-44f9829ba187?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80');
/*!40000 ALTER TABLE `producto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `producto_alergeno`
--

DROP TABLE IF EXISTS `producto_alergeno`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producto_alergeno` (
  `producto_id` int(11) NOT NULL,
  `alergeno_id` int(11) NOT NULL,
  PRIMARY KEY (`producto_id`,`alergeno_id`),
  KEY `fk_pa_aler` (`alergeno_id`),
  CONSTRAINT `fk_pa_aler` FOREIGN KEY (`alergeno_id`) REFERENCES `alergeno` (`id`),
  CONSTRAINT `fk_pa_prod` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `producto_alergeno`
--

LOCK TABLES `producto_alergeno` WRITE;
/*!40000 ALTER TABLE `producto_alergeno` DISABLE KEYS */;
INSERT INTO `producto_alergeno` VALUES (1,1),(1,2),(1,3),(2,1),(2,2),(2,3),(3,1),(3,2),(3,3),(4,1),(4,2),(4,3),(5,1),(5,2),(5,3),(6,1),(6,2),(6,3);
/*!40000 ALTER TABLE `producto_alergeno` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `producto_promocion`
--

DROP TABLE IF EXISTS `producto_promocion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producto_promocion` (
  `variante_id` int(11) NOT NULL,
  `promocion_id` int(11) NOT NULL,
  PRIMARY KEY (`variante_id`,`promocion_id`),
  KEY `fk_pp_prom` (`promocion_id`),
  CONSTRAINT `fk_pp_prom` FOREIGN KEY (`promocion_id`) REFERENCES `promocion` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pp_var` FOREIGN KEY (`variante_id`) REFERENCES `producto_variante` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `producto_promocion`
--

LOCK TABLES `producto_promocion` WRITE;
/*!40000 ALTER TABLE `producto_promocion` DISABLE KEYS */;
INSERT INTO `producto_promocion` VALUES (1,1),(3,1),(8,1);
/*!40000 ALTER TABLE `producto_promocion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `producto_variante`
--

DROP TABLE IF EXISTS `producto_variante`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producto_variante` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `producto_id` int(11) NOT NULL,
  `nombre_variante` varchar(100) NOT NULL,
  `precio` decimal(10,2) NOT NULL,
  `costo` decimal(10,2) DEFAULT NULL,
  `peso_gramos` int(11) DEFAULT NULL,
  `tiempo_prep_min` int(11) NOT NULL DEFAULT 60,
  `stock_disponible` int(11) NOT NULL DEFAULT 0,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  `codigo_sku` varchar(60) DEFAULT NULL,
  `codigo_barras` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_producto_variante_codigo_sku` (`codigo_sku`),
  KEY `idx_variante_producto` (`producto_id`),
  KEY `idx_variante_sku` (`producto_id`,`activo`,`stock_disponible`),
  CONSTRAINT `fk_var_prod` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `producto_variante_chk_1` CHECK (`precio` >= 0),
  CONSTRAINT `producto_variante_chk_2` CHECK (`stock_disponible` >= 0)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `producto_variante`
--

LOCK TABLES `producto_variante` WRITE;
/*!40000 ALTER TABLE `producto_variante` DISABLE KEYS */;
INSERT INTO `producto_variante` VALUES (1,1,'Entera 20cm',95.00,48.00,1800,180,6,1,'SKU-SN-ENT-01','7750000000011'),(2,1,'Porcion',14.50,7.20,180,20,0,1,'SKU-SN-POR-01','7750000000012'),(3,2,'Entera 18cm',88.00,43.00,1500,160,4,1,'SKU-CHM-ENT-01','7750000000021'),(4,2,'Mini',16.00,8.00,190,25,2,1,'SKU-CHM-MIN-01','7750000000022'),(5,3,'Porcion Individual',12.00,5.50,170,18,10,1,'SKU-3L-POR-01','7750000000031'),(6,4,'Porcion Individual',13.50,6.20,170,18,7,1,'SKU-RV-POR-01','7750000000041'),(7,5,'Caja x6',38.00,19.00,600,45,1,1,'SKU-BR-X6-01','7750000000051'),(8,6,'Mediana',72.00,36.00,1200,140,2,1,'SKU-TF-MED-01','7750000000061');
/*!40000 ALTER TABLE `producto_variante` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `promocion`
--

DROP TABLE IF EXISTS `promocion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `promocion` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(150) NOT NULL,
  `tipo_descuento` enum('PORCENTAJE','MONTO_FIJO','2X1','ENVIO_GRATIS') NOT NULL,
  `valor_descuento` decimal(8,2) NOT NULL DEFAULT 0.00,
  `monto_minimo` decimal(10,2) NOT NULL DEFAULT 0.00,
  `aplica_a` enum('PRODUCTO','CARRITO') NOT NULL DEFAULT 'PRODUCTO',
  `fecha_inicio` datetime DEFAULT NULL,
  `fecha_fin` datetime DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  `codigo_cupon` varchar(40) DEFAULT NULL,
  `limite_usos_total` int(11) DEFAULT NULL,
  `limite_usos_por_usuario` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_promocion_codigo_cupon` (`codigo_cupon`),
  CONSTRAINT `chk_promo_fechas` CHECK (`fecha_fin` is null or `fecha_fin` > `fecha_inicio`),
  CONSTRAINT `promocion_chk_1` CHECK (`valor_descuento` >= 0)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promocion`
--

LOCK TABLES `promocion` WRITE;
/*!40000 ALTER TABLE `promocion` DISABLE KEYS */;
INSERT INTO `promocion` VALUES (1,'Campana Otono 2026','PORCENTAJE',12.00,80.00,'CARRITO','2026-04-03 03:20:40','2026-05-03 03:20:40',1,'OTONO12',500,2),(2,'Envio Cero Lima','ENVIO_GRATIS',0.00,60.00,'CARRITO','2026-04-06 03:20:40','2026-04-28 03:20:40',1,'ENVIO0LIM',300,3);
/*!40000 ALTER TABLE `promocion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `puntos_fidelidad`
--

DROP TABLE IF EXISTS `puntos_fidelidad`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `puntos_fidelidad` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `usuario_id` int(11) NOT NULL,
  `pedido_id` int(11) DEFAULT NULL,
  `puntos` int(11) NOT NULL,
  `tipo` enum('GANADO','CANJEADO','AJUSTE') NOT NULL,
  `descripcion` varchar(200) DEFAULT NULL,
  `fecha` datetime NOT NULL DEFAULT current_timestamp(),
  `saldo_resultante` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_pts_ped` (`pedido_id`),
  KEY `idx_puntos_usuario` (`usuario_id`,`fecha`),
  CONSTRAINT `fk_pts_ped` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_pts_usu` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `puntos_fidelidad`
--

LOCK TABLES `puntos_fidelidad` WRITE;
/*!40000 ALTER TABLE `puntos_fidelidad` DISABLE KEYS */;
/*!40000 ALTER TABLE `puntos_fidelidad` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reclamo`
--

DROP TABLE IF EXISTS `reclamo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reclamo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pedido_id` int(11) NOT NULL,
  `usuario_id` int(11) NOT NULL,
  `tipo` enum('REEMBOLSO','REPOSICION','QUEJA') NOT NULL,
  `descripcion` text NOT NULL,
  `estado` enum('ABIERTO','EN_REVISION','RESUELTO','CERRADO') NOT NULL DEFAULT 'ABIERTO',
  `monto_reembolso` decimal(10,2) NOT NULL DEFAULT 0.00,
  `fecha_creacion` datetime NOT NULL DEFAULT current_timestamp(),
  `fecha_resolucion` datetime DEFAULT NULL,
  `fecha_actualizacion` datetime DEFAULT NULL,
  `prioridad` enum('BAJA','MEDIA','ALTA') NOT NULL DEFAULT 'MEDIA',
  `detalle_resolucion` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_rec_ped` (`pedido_id`),
  KEY `fk_rec_usu` (`usuario_id`),
  CONSTRAINT `fk_rec_ped` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`),
  CONSTRAINT `fk_rec_usu` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reclamo`
--

LOCK TABLES `reclamo` WRITE;
/*!40000 ALTER TABLE `reclamo` DISABLE KEYS */;
/*!40000 ALTER TABLE `reclamo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reporte_venta_diaria`
--

DROP TABLE IF EXISTS `reporte_venta_diaria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reporte_venta_diaria` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `total_pedidos` int(11) NOT NULL DEFAULT 0,
  `pedidos_entregados` int(11) NOT NULL DEFAULT 0,
  `ingresos_brutos` decimal(12,2) NOT NULL DEFAULT 0.00,
  `total_descuentos` decimal(12,2) NOT NULL DEFAULT 0.00,
  `ingresos_netos` decimal(12,2) NOT NULL DEFAULT 0.00,
  `ticket_promedio` decimal(10,2) NOT NULL DEFAULT 0.00,
  `metodo_pago_top` varchar(80) DEFAULT NULL,
  `producto_top` varchar(200) DEFAULT NULL,
  `fecha_actualizacion` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `fecha` (`fecha`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reporte_venta_diaria`
--

LOCK TABLES `reporte_venta_diaria` WRITE;
/*!40000 ALTER TABLE `reporte_venta_diaria` DISABLE KEYS */;
INSERT INTO `reporte_venta_diaria` VALUES (1,'2026-04-07',1,0,61.36,0.00,61.36,61.36,'Tarjeta de credito/debito','Selva Negra Clasica','2026-04-08 03:20:40'),(2,'2026-04-05',1,1,123.94,12.00,111.94,123.94,'Tarjeta de credito/debito','Selva Negra Clasica','2026-04-08 03:20:40'),(3,'2026-04-06',1,0,99.18,0.00,99.18,99.18,'Tarjeta de credito/debito','Selva Negra Clasica','2026-04-08 03:20:40');
/*!40000 ALTER TABLE `reporte_venta_diaria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol`
--

DROP TABLE IF EXISTS `rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(80) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol`
--

LOCK TABLES `rol` WRITE;
/*!40000 ALTER TABLE `rol` DISABLE KEYS */;
INSERT INTO `rol` VALUES (1,'ADMIN'),(3,'CLIENTE'),(2,'VENDEDOR');
/*!40000 ALTER TABLE `rol` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sesion`
--

DROP TABLE IF EXISTS `sesion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sesion` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `usuario_id` int(11) NOT NULL,
  `token_hash` varchar(255) NOT NULL,
  `ip_origen` varchar(45) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  `fecha_inicio` datetime NOT NULL DEFAULT current_timestamp(),
  `fecha_expiracion` datetime NOT NULL,
  `agente_usuario` varchar(255) DEFAULT NULL,
  `fecha_revocacion` datetime DEFAULT NULL,
  `motivo_revocacion` varchar(120) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token_hash` (`token_hash`),
  KEY `fk_ses_usu` (`usuario_id`),
  KEY `idx_sesion_expiracion` (`fecha_expiracion`,`activo`),
  CONSTRAINT `fk_ses_usu` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_expiracion` CHECK (`fecha_expiracion` > `fecha_inicio`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sesion`
--

LOCK TABLES `sesion` WRITE;
/*!40000 ALTER TABLE `sesion` DISABLE KEYS */;
INSERT INTO `sesion` VALUES (1,1,'2f87a90d6fd94daa76dc37a84431c644059e85098799023d239f2bc1e9098c68',NULL,0,'2026-04-02 13:00:11','2026-04-03 13:00:11',NULL,NULL,NULL),(2,2,'df2ec4cfd283376055c34350eb5af66390d07b08225e44bb1dbf521b6eb2c4bd','0:0:0:0:0:0:0:1',0,'2026-04-02 13:04:40','2026-04-03 13:04:40',NULL,NULL,NULL),(3,2,'45df1e0df96c653d7caf07607caa304971e25a7936fb10f5355f9e941407c694','0:0:0:0:0:0:0:1',0,'2026-04-02 13:21:39','2026-04-03 13:21:39',NULL,NULL,NULL),(4,2,'0879b167f5db6573b9db1fa9ff986a67d18449194aa989773834864a3f2e4b66','0:0:0:0:0:0:0:1',1,'2026-04-02 13:23:39','2026-04-03 13:23:39',NULL,NULL,NULL),(5,4,'6f903ce181f006e9a80fa166f9a93d70678505ed39e810648a7e845c4e82b9ac',NULL,1,'2026-04-03 17:54:46','2026-04-04 17:54:46',NULL,NULL,NULL),(6,5,'89e9f932610865aa2deb10fdf481970d354f0b6c60a2398dbf5db81aac653044',NULL,0,'2026-04-08 00:10:13','2026-04-09 00:10:13',NULL,NULL,NULL),(7,5,'c9d53f8f0addf76008346ddeb77baad04bb735acb04f86215423140eb5e06d1a','0:0:0:0:0:0:0:1',0,'2026-04-08 00:10:31','2026-04-09 00:10:31',NULL,NULL,NULL),(8,2,'c701380c47dd7fe90ca4a8f985b170afb55167760c2354a66365340e8713aa56','0:0:0:0:0:0:0:1',1,'2026-04-08 00:12:05','2026-04-09 00:12:05',NULL,NULL,NULL),(9,6,'05bc3f6a93f2e476ffd0581969612de517ef9d75140c3bbc69f6bff86b86658c',NULL,0,'2026-04-08 00:48:51','2026-04-09 00:48:51',NULL,NULL,NULL),(10,2,'e3c10b070fe0c5abc234e0ef3b15f66dcdc534deae274d08cc025550693b0bb1','0:0:0:0:0:0:0:1',0,'2026-04-08 01:38:27','2026-04-09 01:38:27',NULL,NULL,NULL),(11,2,'ccbf9254f493564613908ccbd4a4b406cf27c9189aadb07c1f54ba1150e834a2','0:0:0:0:0:0:0:1',0,'2026-04-08 01:51:57','2026-04-09 01:51:57',NULL,NULL,NULL),(12,6,'ca7f5abf0559493ea954ab2a8071d6a7efd6b4143fe5fa5b00c62ead886c4758','0:0:0:0:0:0:0:1',0,'2026-04-08 02:43:02','2026-04-09 02:43:02',NULL,NULL,NULL),(13,2,'46c7c1c269b4d4b5de0304007a2cfde26dfd992afa92b661a5658d06fa8ee483','0:0:0:0:0:0:0:1',0,'2026-04-08 02:48:42','2026-04-09 02:48:42',NULL,NULL,NULL),(14,6,'4b5d232d5951a2054b3032cd9d18c4b19c24aee73bb5ca549c7b7bde49307c3f','0:0:0:0:0:0:0:1',0,'2026-04-08 02:51:41','2026-04-09 02:51:41',NULL,NULL,NULL),(15,6,'85c8d65c2cb8024d9640da9bc7c4a8b84b3ae7138b2a7714e33c96ee760eda45','0:0:0:0:0:0:0:1',0,'2026-04-08 02:51:51','2026-04-09 02:51:51',NULL,NULL,NULL),(16,2,'2750f8719a202eafb2e999802fccb2777cc15ff58d2e0a3b752c497ad2c83363','0:0:0:0:0:0:0:1',0,'2026-04-08 02:52:31','2026-04-09 02:52:31',NULL,NULL,NULL),(17,6,'022a482e6a5baa849932ee650ec18a35b401a6dc3b54b47b7d139d8df5a65a48','0:0:0:0:0:0:0:1',0,'2026-04-08 02:53:19','2026-04-09 02:53:19',NULL,NULL,NULL),(18,6,'37eaa685d438cf8f17285bcefcdc7a30859f43bc846e6ffedeeac3c80b448a1a','0:0:0:0:0:0:0:1',0,'2026-04-08 02:55:00','2026-04-08 03:33:20',NULL,'2026-04-08 03:03:20','LOGOUT'),(19,7,'35c0b6e4f4689a498d828d142f1742f240927a2150229409dedeacf8a02dc8b7',NULL,1,'2026-04-08 03:02:27','2026-04-09 03:02:27',NULL,NULL,NULL),(21,8,'9c857bcd20c9e9613e04ec7ba0afb8c21fde4673ee6b7fd3a8b8fb2b9ebafca3',NULL,1,'2026-04-08 03:02:33','2026-04-09 03:02:33',NULL,NULL,NULL),(22,2,'2cd7c204996d24c2d5010c27203dc60bb44a98f6c97ade22f688758da0f7872d','0:0:0:0:0:0:0:1',0,'2026-04-08 03:05:19','2026-04-08 03:41:43','Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36','2026-04-08 03:11:43','LOGOUT'),(23,6,'0594e154b8e5c995ed74cf7dd381ef2a899189eb6474af80e79906ec528172bf','0:0:0:0:0:0:0:1',0,'2026-04-08 03:11:48','2026-04-08 03:41:51','Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36','2026-04-08 03:11:51','LOGOUT'),(24,2,'b328d9ced124cdadec10aa88939e03612c6b38ed1e230e2fea48c3148f43bd9c','0:0:0:0:0:0:0:1',0,'2026-04-08 03:11:56','2026-04-08 03:57:13','Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36','2026-04-08 03:27:13','LOGOUT'),(25,2,'7bcad7800b1784cd86a2698ac46ab785fcae2033e04245ca3bf55a46fe8f6908','127.0.0.1',1,'2026-04-08 03:26:35','2026-04-08 03:56:36','codex-debug',NULL,NULL),(26,6,'158b6bd99f25c8199c63daab64038b97742c51003eb56093a4b66b297960ae4e','0:0:0:0:0:0:0:1',0,'2026-04-08 03:27:17','2026-04-08 03:57:43','Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36','2026-04-08 03:27:43','LOGOUT'),(27,6,'36779087ee23c46e5976be2f0a354b4071f2c8f4064d1770b40621acb1786dd2','0:0:0:0:0:0:0:1',0,'2026-04-08 03:27:50','2026-04-08 03:58:02','Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36','2026-04-08 03:28:02','LOGOUT'),(28,2,'31dc7907b6a59214274f860f9101f0b8310a5289694b96a55b5c1eece493b47c','0:0:0:0:0:0:0:1',1,'2026-04-08 03:28:06','2026-04-08 03:59:40','Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36',NULL,NULL),(29,2,'9678812fd821397ac7e148b861234e0ad1adbeb2bf9ff5ca0ab7cf9f8f19b849','0:0:0:0:0:0:0:1',0,'2026-04-08 03:39:25','2026-04-08 04:09:25','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36','2026-04-08 13:03:59','EXPIRADA_INACTIVIDAD'),(30,2,'dd1fa3f2138453ba1a486a3e2034897aee4a877cd3d3ba0fcb51ce9b59077ffb','0:0:0:0:0:0:0:1',0,'2026-04-08 13:05:36','2026-04-08 13:35:36','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36','2026-04-08 13:23:43','LOGOUT'),(31,10,'acb2c43b653d06353c6015596fcbf8400fbc770db0748b072915dfd45c45e769',NULL,0,'2026-04-08 13:24:59','2026-04-08 14:22:31',NULL,'2026-04-08 13:52:38','LOGOUT'),(32,10,'b90fa80276891097bcea0b6d92229710e6a1a92159604c7ec446420864e413c4','0:0:0:0:0:0:0:1',0,'2026-04-08 13:53:02','2026-04-08 14:23:02','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36','2026-04-08 13:53:31','LOGOUT'),(33,2,'96fb891f8b81406cd29cf1218838db9af4fa0f69d0e3e048e6e4ae4c709bea5b','0:0:0:0:0:0:0:1',0,'2026-04-08 13:53:37','2026-04-08 14:23:37','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36','2026-04-08 14:32:30','EXPIRADA_INACTIVIDAD'),(34,2,'a1e0ed032129624b9e5e7d385f4e9c8f922e3aaec77e770a25d14805f83e05a1','0:0:0:0:0:0:0:1',0,'2026-04-08 14:32:42','2026-04-08 15:02:42','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36','2026-04-08 14:33:14','LOGOUT'),(35,10,'e6daed048d21d13db24ea13d2f1d1a1101504f9612cb5eb48a951fd778333ed8','0:0:0:0:0:0:0:1',0,'2026-04-08 14:33:22','2026-04-08 15:03:22','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36','2026-04-08 14:35:04','LOGOUT'),(36,2,'06b1786eec6edda231782af9c6ec77c1183771d144dccdc48f82a7c72c4aea10','0:0:0:0:0:0:0:1',1,'2026-04-08 14:52:12','2026-04-08 15:22:12','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36',NULL,NULL);
/*!40000 ALTER TABLE `sesion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(150) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password_hash` varchar(255) DEFAULT NULL,
  `telefono` varchar(25) DEFAULT NULL,
  `proveedor_oauth` varchar(40) DEFAULT NULL,
  `oauth_id` varchar(200) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  `fecha_creacion` datetime NOT NULL DEFAULT current_timestamp(),
  `fecha_actualizacion` datetime DEFAULT NULL,
  `fecha_desactivacion` datetime DEFAULT NULL,
  `fecha_verificacion_email` datetime DEFAULT NULL,
  `intentos_fallidos_login` int(11) NOT NULL DEFAULT 0,
  `bloqueado_hasta` datetime DEFAULT NULL,
  `reset_token` varchar(36) DEFAULT NULL,
  `reset_token_expiration` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_usuario_email` (`email`),
  CONSTRAINT `chk_auth` CHECK (`password_hash` is not null or `proveedor_oauth` is not null)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'Felix Tintaya','felixtintaya@gmail.com','$2a$10$i5b/gtsLw.OZXnyA3swACevD1wuVGIuyG9kilrfq8efSKE92X2USK',NULL,NULL,NULL,1,'2026-04-02 13:00:11',NULL,NULL,NULL,0,NULL,NULL,NULL),(2,'Administrador','admin@casachantilly.pe','$2a$10$zIL7lAtje1Kki9scobwhK.nPlZ6F/Z.1E3ycT4nh7R9.ZOrwQ7CEG',NULL,NULL,NULL,1,'2026-04-02 13:04:07','2026-04-08 14:52:12',NULL,NULL,0,NULL,NULL,NULL),(3,'Cliente Prueba','cliente@gmail.com','$2a$10$eGXGrNpE3RzsH/o3XrTy5eAmoGp020c3Z0TQ1plchA.oojAdppCp2',NULL,NULL,NULL,1,'2026-04-02 13:04:07',NULL,NULL,NULL,0,NULL,NULL,NULL),(4,'Alonso Sifuentes','alonso@gmail.com','$2a$10$FvKANy1nJ48EVghEW2.rIu.rssMb2TlJNDoFRA8ZreFnBzefBqL2G',NULL,NULL,NULL,1,'2026-04-03 17:54:46',NULL,NULL,NULL,0,NULL,NULL,NULL),(5,'Kevin Sankef','kevin@gmail.com','$2a$10$MQ98vpsB92YIUJiqmMOaUeliJcM4ur9yFKhOzlYgBcHYdqWvz3WWS',NULL,NULL,NULL,1,'2026-04-08 00:10:13',NULL,NULL,NULL,0,NULL,NULL,NULL),(6,'kevin','kevin123@gmail.com','$2a$10$k7uBBzQTzp4hhk2ue6pKeO.g1XOWHp7piAo5duTVARRSxEF7RyTBq',NULL,NULL,NULL,1,'2026-04-08 00:48:51','2026-04-08 03:27:50',NULL,NULL,0,NULL,NULL,NULL),(7,'Diag User','diag1775635346@mail.com','$2a$10$sGEIZHZRtIBbySf8qoZwy.nr6zCZwV6x/gOXuGSqYjyRLyz8YgEPK',NULL,NULL,NULL,1,'2026-04-08 03:02:27',NULL,NULL,NULL,0,NULL,NULL,NULL),(8,'Diag Two','diag2_1775635352@mail.com','$2a$10$R0nJsKJGw.Xynd9PUOhtBu8tLJmkHdQ.zZV6yyxZ3I4E2gfMd4lT2',NULL,NULL,NULL,1,'2026-04-08 03:02:33',NULL,NULL,NULL,0,NULL,NULL,NULL),(9,'Aquiles','aquiles@gmail.com','$2a$10$cY6unTWRM.VAqZMjvKFztuyzPdLyS/1buPhXTnSeWcdAC49ZhIqb6',NULL,NULL,NULL,1,'2026-04-08 13:06:36','2026-04-08 13:06:36',NULL,NULL,0,NULL,NULL,NULL),(10,'RousMery','rous@gmail.com','$2a$10$dT4KSlGqHfUaq.i/JsyBReKHpWJybBk4nCFT/FzHE3s1qLl8mSOZe',NULL,NULL,NULL,1,'2026-04-08 13:24:59','2026-04-08 14:33:22',NULL,NULL,0,NULL,NULL,NULL);
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario_rol`
--

DROP TABLE IF EXISTS `usuario_rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_rol` (
  `usuario_id` int(11) NOT NULL,
  `rol_id` int(11) NOT NULL,
  PRIMARY KEY (`usuario_id`,`rol_id`),
  KEY `fk_ur_rol` (`rol_id`),
  CONSTRAINT `fk_ur_rol` FOREIGN KEY (`rol_id`) REFERENCES `rol` (`id`),
  CONSTRAINT `fk_ur_usu` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario_rol`
--

LOCK TABLES `usuario_rol` WRITE;
/*!40000 ALTER TABLE `usuario_rol` DISABLE KEYS */;
INSERT INTO `usuario_rol` VALUES (1,3),(2,1),(3,3),(4,3),(5,3),(6,3),(7,3),(8,3),(9,2),(10,3);
/*!40000 ALTER TABLE `usuario_rol` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `vw_horas_pico`
--

DROP TABLE IF EXISTS `vw_horas_pico`;
/*!50001 DROP VIEW IF EXISTS `vw_horas_pico`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_horas_pico` AS SELECT 
 1 AS `hora`,
 1 AS `num_pedidos`,
 1 AS `ticket_promedio`,
 1 AS `ranking`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_metodos_pago_stats`
--

DROP TABLE IF EXISTS `vw_metodos_pago_stats`;
/*!50001 DROP VIEW IF EXISTS `vw_metodos_pago_stats`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_metodos_pago_stats` AS SELECT 
 1 AS `metodo`,
 1 AS `total_transacciones`,
 1 AS `monto_total`,
 1 AS `monto_promedio`,
 1 AS `porcentaje_uso`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_productos_top`
--

DROP TABLE IF EXISTS `vw_productos_top`;
/*!50001 DROP VIEW IF EXISTS `vw_productos_top`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_productos_top` AS SELECT 
 1 AS `variante_id`,
 1 AS `producto`,
 1 AS `variante`,
 1 AS `categoria`,
 1 AS `unidades_vendidas`,
 1 AS `ingresos_generados`,
 1 AS `num_pedidos`,
 1 AS `precio_promedio_venta`,
 1 AS `ranking_unidades`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_ventas_diarias`
--

DROP TABLE IF EXISTS `vw_ventas_diarias`;
/*!50001 DROP VIEW IF EXISTS `vw_ventas_diarias`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_ventas_diarias` AS SELECT 
 1 AS `fecha`,
 1 AS `total_pedidos`,
 1 AS `pedidos_entregados`,
 1 AS `ingresos_brutos`,
 1 AS `total_descuentos`,
 1 AS `ingresos_netos`,
 1 AS `ticket_promedio`,
 1 AS `ingresos_delivery`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_ventas_mensual`
--

DROP TABLE IF EXISTS `vw_ventas_mensual`;
/*!50001 DROP VIEW IF EXISTS `vw_ventas_mensual`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_ventas_mensual` AS SELECT 
 1 AS `anio`,
 1 AS `mes`,
 1 AS `periodo`,
 1 AS `pedidos`,
 1 AS `ingresos`,
 1 AS `descuentos`,
 1 AS `clientes_unicos`,
 1 AS `ticket_promedio`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `zona_envio`
--

DROP TABLE IF EXISTS `zona_envio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `zona_envio` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre_distrito` varchar(120) NOT NULL,
  `costo_delivery` decimal(8,2) NOT NULL DEFAULT 0.00,
  `tiempo_estimado_min` int(11) NOT NULL DEFAULT 60,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  CONSTRAINT `zona_envio_chk_1` CHECK (`costo_delivery` >= 0)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `zona_envio`
--

LOCK TABLES `zona_envio` WRITE;
/*!40000 ALTER TABLE `zona_envio` DISABLE KEYS */;
INSERT INTO `zona_envio` VALUES (1,'Miraflores',8.50,45,1),(2,'San Isidro',9.50,50,1),(3,'Surco',11.00,60,1),(4,'La Molina',12.50,70,1);
/*!40000 ALTER TABLE `zona_envio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'prueba_db'
--
/*!50106 SET @save_time_zone= @@TIME_ZONE */ ;
/*!50106 DROP EVENT IF EXISTS `evt_actualizar_reporte_diario` */;
DELIMITER ;;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;;
/*!50003 SET character_set_client  = utf8mb4 */ ;;
/*!50003 SET character_set_results = utf8mb4 */ ;;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO' */ ;;
/*!50003 SET @saved_time_zone      = @@time_zone */ ;;
/*!50003 SET time_zone             = 'SYSTEM' */ ;;
/*!50106 CREATE*/ /*!50117 DEFINER=`root`@`localhost`*/ /*!50106 EVENT `evt_actualizar_reporte_diario` ON SCHEDULE EVERY 1 DAY STARTS '2026-04-03 01:00:00' ON COMPLETION NOT PRESERVE ENABLE DO BEGIN
    REPLACE INTO reporte_venta_diaria
        (fecha, total_pedidos, pedidos_entregados, ingresos_brutos,
         total_descuentos, ingresos_netos, ticket_promedio, metodo_pago_top, producto_top)
    SELECT
        DATE(NOW() - INTERVAL 1 DAY),
        COUNT(DISTINCT p.id),
        COUNT(DISTINCT CASE WHEN ep.orden = 6 THEN p.id END),
        COALESCE(SUM(p.total), 0),
        COALESCE(SUM(p.descuento), 0),
        COALESCE(SUM(p.total - p.descuento), 0),
        COALESCE(AVG(p.total), 0),
        (SELECT mp2.nombre
         FROM pago pg2
         JOIN metodo_pago mp2 ON mp2.id = pg2.metodo_pago_id
         WHERE DATE(pg2.fecha) = DATE(NOW() - INTERVAL 1 DAY)
           AND pg2.estado = 'APROBADO'
         GROUP BY mp2.nombre
         ORDER BY COUNT(*) DESC LIMIT 1),
        (SELECT CONCAT(pr2.nombre, ' - ', pv2.nombre_variante)
         FROM detalle_pedido dp2
         JOIN pedido pe2            ON pe2.id = dp2.pedido_id
         JOIN producto_variante pv2 ON pv2.id = dp2.variante_id
         JOIN producto pr2          ON pr2.id = pv2.producto_id
         WHERE DATE(pe2.fecha_creacion) = DATE(NOW() - INTERVAL 1 DAY)
         GROUP BY dp2.variante_id
         ORDER BY SUM(dp2.cantidad) DESC LIMIT 1)
    FROM pedido p
    LEFT JOIN estado_pedido_historial eph ON eph.pedido_id = p.id
    LEFT JOIN estado_pedido ep            ON ep.id = eph.estado_id AND ep.orden = 6
    WHERE DATE(p.fecha_creacion) = DATE(NOW() - INTERVAL 1 DAY);
END */ ;;
/*!50003 SET time_zone             = @saved_time_zone */ ;;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;;
/*!50003 SET character_set_client  = @saved_cs_client */ ;;
/*!50003 SET character_set_results = @saved_cs_results */ ;;
/*!50003 SET collation_connection  = @saved_col_connection */ ;;
DELIMITER ;
/*!50106 SET TIME_ZONE= @save_time_zone */ ;

--
-- Dumping routines for database 'prueba_db'
--
/*!50003 DROP FUNCTION IF EXISTS `fn_verificar_cupo` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_verificar_cupo`(p_franja_id   INT,
    p_tipo_pedido VARCHAR(10)
) RETURNS tinyint(1)
    READS SQL DATA
BEGIN
    DECLARE v_cupos_totales  INT DEFAULT 0;
    DECLARE v_cupos_ocupados INT DEFAULT 0;
    DECLARE v_tipo_franja    VARCHAR(20) DEFAULT NULL;

    SELECT cupos_totales, cupos_ocupados, tipo
    INTO   v_cupos_totales, v_cupos_ocupados, v_tipo_franja
    FROM   franja_horaria
    WHERE  id = p_franja_id;

    IF v_tipo_franja IS NULL THEN
        RETURN 0;
    END IF;

    IF v_tipo_franja != 'AMBOS' AND v_tipo_franja != p_tipo_pedido THEN
        RETURN 0;
    END IF;

    IF v_cupos_ocupados >= v_cupos_totales THEN
        RETURN 0;
    END IF;

    RETURN 1;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_asignar_puntos` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_asignar_puntos`(IN p_pedido_id INT)
BEGIN
    DECLARE v_usuario_id   INT;
    DECLARE v_total        DECIMAL(10,2);
    DECLARE v_puntos       INT;
    DECLARE v_tasa         DECIMAL(5,2) DEFAULT 10.00;
    DECLARE v_ya_procesado INT DEFAULT 0;

    SELECT COUNT(*) INTO v_ya_procesado
    FROM puntos_fidelidad
    WHERE pedido_id = p_pedido_id AND tipo = 'GANADO';

    IF v_ya_procesado > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Los puntos para este pedido ya fueron asignados.';
    END IF;

    SELECT usuario_id, total
    INTO   v_usuario_id, v_total
    FROM   pedido
    WHERE  id = p_pedido_id;

    IF v_usuario_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Pedido no encontrado.';
    END IF;

    SET v_puntos = FLOOR(v_total / v_tasa);

    IF v_puntos > 0 THEN
        INSERT INTO puntos_fidelidad (usuario_id, pedido_id, puntos, tipo, descripcion)
        VALUES (
            v_usuario_id, p_pedido_id, v_puntos, 'GANADO',
            CONCAT('Compra #', p_pedido_id, ' - S/.', v_total, ' = ', v_puntos, ' pts')
        );
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_canjear_puntos` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_canjear_puntos`(
    IN p_usuario_id INT,
    IN p_puntos     INT,
    IN p_pedido_id  INT
)
BEGIN
    DECLARE v_saldo INT DEFAULT 0;

    IF p_puntos <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'La cantidad de puntos a canjear debe ser mayor a cero.';
    END IF;

    SELECT COALESCE(SUM(puntos), 0) INTO v_saldo
    FROM puntos_fidelidad
    WHERE usuario_id = p_usuario_id;

    IF v_saldo < p_puntos THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Saldo de puntos insuficiente para el canje solicitado.';
    END IF;

    INSERT INTO puntos_fidelidad (usuario_id, pedido_id, puntos, tipo, descripcion)
    VALUES (
        p_usuario_id, p_pedido_id, -p_puntos, 'CANJEADO',
        CONCAT('Canje de ', p_puntos, ' pts aplicado al pedido #', p_pedido_id)
    );
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_confirmar_pedido` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_confirmar_pedido`(
    IN  p_pedido_id  INT,
    IN  p_usuario_id INT,
    OUT p_resultado  VARCHAR(200)
)
BEGIN
    DECLARE v_stock_insuf INT     DEFAULT 0;
    DECLARE v_continuar   BOOLEAN DEFAULT TRUE;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_resultado = 'ERROR: Fallo al confirmar el pedido. Transaccion revertida.';
    END;

    START TRANSACTION;

    SELECT COUNT(*) INTO v_stock_insuf
    FROM detalle_pedido dp
    JOIN producto_variante pv ON pv.id = dp.variante_id
    WHERE dp.pedido_id = p_pedido_id
      AND pv.stock_disponible < dp.cantidad;

    IF v_stock_insuf > 0 THEN
        ROLLBACK;
        SET p_resultado = 'RECHAZADO: Stock insuficiente para uno o mas productos del pedido.';
        SET v_continuar = FALSE;
    END IF;

    IF v_continuar THEN
        INSERT INTO estado_pedido_historial (pedido_id, estado_id, usuario_id, observacion)
        SELECT p_pedido_id, id, p_usuario_id, 'Pedido validado y confirmado por administracion'
        FROM estado_pedido WHERE orden = 2;

        CALL sp_asignar_puntos(p_pedido_id);

        COMMIT;
        SET p_resultado = CONCAT('OK: Pedido #', p_pedido_id, ' confirmado exitosamente.');
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Final view structure for view `vw_horas_pico`
--

/*!50001 DROP VIEW IF EXISTS `vw_horas_pico`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_horas_pico` AS select hour(`p`.`fecha_creacion`) AS `hora`,count(0) AS `num_pedidos`,round(avg(`p`.`total`),2) AS `ticket_promedio`,rank() over ( order by count(0) desc) AS `ranking` from `pedido` `p` group by hour(`p`.`fecha_creacion`) order by count(0) desc */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_metodos_pago_stats`
--

/*!50001 DROP VIEW IF EXISTS `vw_metodos_pago_stats`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_metodos_pago_stats` AS select `mp`.`nombre` AS `metodo`,count(`pg`.`id`) AS `total_transacciones`,sum(`pg`.`monto`) AS `monto_total`,avg(`pg`.`monto`) AS `monto_promedio`,round(count(`pg`.`id`) * 100.0 / sum(count(`pg`.`id`)) over (),2) AS `porcentaje_uso` from (`pago` `pg` join `metodo_pago` `mp` on(`mp`.`id` = `pg`.`metodo_pago_id`)) where `pg`.`estado` = 'APROBADO' group by `mp`.`id`,`mp`.`nombre` order by count(`pg`.`id`) desc */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_productos_top`
--

/*!50001 DROP VIEW IF EXISTS `vw_productos_top`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_productos_top` AS select `pv`.`id` AS `variante_id`,`p`.`nombre` AS `producto`,`pv`.`nombre_variante` AS `variante`,`cat`.`nombre` AS `categoria`,sum(`dp`.`cantidad`) AS `unidades_vendidas`,sum(`dp`.`subtotal_linea`) AS `ingresos_generados`,count(distinct `dp`.`pedido_id`) AS `num_pedidos`,avg(`dp`.`precio_unitario_snapshot`) AS `precio_promedio_venta`,rank() over ( order by sum(`dp`.`cantidad`) desc) AS `ranking_unidades` from (((`detalle_pedido` `dp` join `producto_variante` `pv` on(`pv`.`id` = `dp`.`variante_id`)) join `producto` `p` on(`p`.`id` = `pv`.`producto_id`)) join `categoria` `cat` on(`cat`.`id` = `p`.`categoria_id`)) group by `pv`.`id`,`p`.`nombre`,`pv`.`nombre_variante`,`cat`.`nombre` order by sum(`dp`.`cantidad`) desc */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_ventas_diarias`
--

/*!50001 DROP VIEW IF EXISTS `vw_ventas_diarias`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_ventas_diarias` AS select cast(`p`.`fecha_creacion` as date) AS `fecha`,count(distinct `p`.`id`) AS `total_pedidos`,count(distinct case when `eph`.`estado_id` = (select `estado_pedido`.`id` from `estado_pedido` where `estado_pedido`.`orden` = 6) then `p`.`id` end) AS `pedidos_entregados`,sum(`p`.`total`) AS `ingresos_brutos`,sum(`p`.`descuento`) AS `total_descuentos`,sum(`p`.`total` - `p`.`descuento`) AS `ingresos_netos`,avg(`p`.`total`) AS `ticket_promedio`,sum(`p`.`costo_envio`) AS `ingresos_delivery` from (`pedido` `p` left join `estado_pedido_historial` `eph` on(`eph`.`pedido_id` = `p`.`id` and `eph`.`estado_id` = (select `estado_pedido`.`id` from `estado_pedido` where `estado_pedido`.`orden` = 6))) group by cast(`p`.`fecha_creacion` as date) order by cast(`p`.`fecha_creacion` as date) desc */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_ventas_mensual`
--

/*!50001 DROP VIEW IF EXISTS `vw_ventas_mensual`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_ventas_mensual` AS select year(`p`.`fecha_creacion`) AS `anio`,month(`p`.`fecha_creacion`) AS `mes`,date_format(`p`.`fecha_creacion`,'%Y-%m') AS `periodo`,count(distinct `p`.`id`) AS `pedidos`,sum(`p`.`total`) AS `ingresos`,sum(`p`.`descuento`) AS `descuentos`,count(distinct `p`.`usuario_id`) AS `clientes_unicos`,avg(`p`.`total`) AS `ticket_promedio` from `pedido` `p` group by year(`p`.`fecha_creacion`),month(`p`.`fecha_creacion`),date_format(`p`.`fecha_creacion`,'%Y-%m') order by year(`p`.`fecha_creacion`) desc,month(`p`.`fecha_creacion`) desc */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-08 15:07:47
