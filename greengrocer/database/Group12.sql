CREATE DATABASE  IF NOT EXISTS `greengrocer` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `greengrocer`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: greengrocer
-- ------------------------------------------------------
-- Server version	8.0.44

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
-- Table structure for table `carrier_ratings`
--

DROP TABLE IF EXISTS `carrier_ratings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carrier_ratings` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `carrier_id` int NOT NULL,
  `customer_id` int NOT NULL,
  `rating` int NOT NULL,
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `order_id` (`order_id`),
  CONSTRAINT `carrier_ratings_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `carrier_ratings_chk_1` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carrier_ratings`
--

LOCK TABLES `carrier_ratings` WRITE;
/*!40000 ALTER TABLE `carrier_ratings` DISABLE KEYS */;
/*!40000 ALTER TABLE `carrier_ratings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupons`
--

DROP TABLE IF EXISTS `coupons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupons` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `discount_percentage` decimal(5,2) NOT NULL,
  `min_purchase_amount` decimal(10,2) DEFAULT '0.00',
  `max_discount_amount` decimal(10,2) DEFAULT NULL,
  `valid_until` timestamp NULL DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupons`
--

LOCK TABLES `coupons` WRITE;
/*!40000 ALTER TABLE `coupons` DISABLE KEYS */;
INSERT INTO `coupons` VALUES (1,'HOSGELDIN',10.00,100.00,NULL,'2026-12-31 20:59:59',1),(2,'YAZ2026',15.00,200.00,NULL,'2026-08-30 20:59:59',1),(3,'ESKI2025',5.00,50.00,NULL,'2025-12-30 20:59:59',0);
/*!40000 ALTER TABLE `coupons` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loyalty_settings`
--

DROP TABLE IF EXISTS `loyalty_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loyalty_settings` (
  `id` int NOT NULL AUTO_INCREMENT,
  `min_orders` int DEFAULT '5',
  `discount_percentage` decimal(5,2) DEFAULT '5.00',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `is_active` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loyalty_settings`
--

LOCK TABLES `loyalty_settings` WRITE;
/*!40000 ALTER TABLE `loyalty_settings` DISABLE KEYS */;
INSERT INTO `loyalty_settings` VALUES (1,-5,5.00,NULL,1);
/*!40000 ALTER TABLE `loyalty_settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages` (
  `id` int NOT NULL AUTO_INCREMENT,
  `sender_id` int NOT NULL,
  `receiver_id` int DEFAULT NULL,
  `subject` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_read` tinyint(1) DEFAULT '0',
  `reply_to` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `sender_id` (`sender_id`),
  CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
INSERT INTO `messages` VALUES (1,4,3,'Sipariş Gecikmesi','Merhaba, 1 numaralı siparişim gecikti.',0,NULL,'2026-01-01 12:00:00'),(2,6,3,'Teşekkürler','Ürünler çok taze geldi.',0,NULL,'2026-01-01 13:20:00');
/*!40000 ALTER TABLE `messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `product_id` int NOT NULL,
  `product_name` varchar(100) NOT NULL,
  `quantity` decimal(10,2) NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `total_price` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `order_id` (`order_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (1,1,1,'Domates',2.00,25.00,50.00),(2,1,13,'Elma',4.00,25.00,100.00),(3,2,3,'Biber',2.00,30.00,60.00),(4,2,2,'Salatalık',1.00,20.00,20.00),(5,3,5,'Patates',3.00,15.00,45.00),(6,3,13,'Elma',5.00,25.00,125.00);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `carrier_id` int DEFAULT NULL,
  `order_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `delivery_time` timestamp NULL DEFAULT NULL,
  `requested_delivery_date` timestamp NOT NULL,
  `delivery_address` text,
  `delivery_neighborhood` varchar(50) DEFAULT NULL,
  `total_cost` decimal(10,2) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  `vat_amount` decimal(10,2) NOT NULL,
  `discount_amount` decimal(10,2) DEFAULT '0.00',
  `status` enum('pending','assigned','in_delivery','completed','cancelled') DEFAULT 'pending',
  `priority_level` int DEFAULT '1',
  `invoice` longtext,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `payment_method` enum('CASH_ON_DELIVERY','ONLINE_PAYMENT') DEFAULT 'CASH_ON_DELIVERY',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `carrier_id` (`carrier_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`carrier_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,4,2,'2026-01-01 07:00:00','2026-01-01 11:30:00','2026-01-01 11:00:00','Teşvikiye Cad. No:10','Şişli',177.00,150.00,27.00,0.00,'completed',1,NULL,'2026-01-01 19:35:15','CASH_ON_DELIVERY'),(2,4,2,'2026-01-01 08:00:00',NULL,'2026-01-02 07:00:00','Valikonağı Cad. No:5','Şişli',94.40,80.00,14.40,0.00,'assigned',1,NULL,'2026-01-01 19:35:15','ONLINE_PAYMENT'),(3,6,NULL,'2026-01-01 09:30:00',NULL,'2026-01-02 13:00:00','Bağdat Cad. No:10','Kadıköy',250.00,211.86,38.14,0.00,'pending',1,NULL,'2026-01-01 19:35:15','ONLINE_PAYMENT');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `type` enum('vegetable','fruit') NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `stock` decimal(10,2) NOT NULL DEFAULT '0.00',
  `threshold` decimal(10,2) NOT NULL DEFAULT '5.00',
  `image` longblob,
  `image_type` varchar(50) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_threshold` (`stock`,`threshold`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'Domates','vegetable',25.00,97.50,10.00,NULL,NULL,1,'2026-01-01 19:35:15'),(2,'Salatalık','vegetable',20.00,80.00,10.00,NULL,NULL,1,'2026-01-01 19:35:15'),(3,'Biber','vegetable',30.00,50.00,5.00,NULL,NULL,1,'2026-01-01 19:35:15'),(4,'Patlıcan','vegetable',28.00,60.00,8.00,NULL,NULL,1,'2026-01-01 19:35:15'),(5,'Patates','vegetable',15.00,200.00,20.00,NULL,NULL,1,'2026-01-01 19:35:15'),(6,'Soğan','vegetable',12.00,150.00,20.00,NULL,NULL,1,'2026-01-01 19:35:15'),(7,'Havuç','vegetable',18.00,70.00,10.00,NULL,NULL,1,'2026-01-01 19:35:15'),(8,'Ispanak','vegetable',22.00,40.00,5.00,NULL,NULL,1,'2026-01-01 19:35:15'),(9,'Marul','vegetable',15.00,45.00,5.00,NULL,NULL,1,'2026-01-01 19:35:15'),(10,'Brokoli','vegetable',35.00,30.00,5.00,NULL,NULL,1,'2026-01-01 19:35:15'),(11,'Kabak','vegetable',20.00,50.00,8.00,NULL,NULL,1,'2026-01-01 19:35:15'),(12,'Sarımsak','vegetable',80.00,20.00,2.00,NULL,NULL,1,'2026-01-01 19:35:15'),(13,'Elma','fruit',25.00,100.00,10.00,NULL,NULL,1,'2026-01-01 19:35:15'),(14,'Armut','fruit',30.00,80.00,8.00,NULL,NULL,1,'2026-01-01 19:35:15'),(15,'Muz','fruit',45.00,120.00,15.00,NULL,NULL,1,'2026-01-01 19:35:15'),(16,'Çilek','fruit',60.00,30.00,5.00,NULL,NULL,1,'2026-01-01 19:35:15'),(17,'Kiraz','fruit',70.00,25.00,5.00,NULL,NULL,1,'2026-01-01 19:35:15'),(18,'Karpuz','fruit',10.00,200.00,20.00,NULL,NULL,1,'2026-01-01 19:35:15'),(19,'Kavun','fruit',15.00,150.00,15.00,NULL,NULL,1,'2026-01-01 19:35:15'),(20,'Üzüm','fruit',35.00,60.00,8.00,NULL,NULL,1,'2026-01-01 19:35:15'),(21,'Portakal','fruit',20.00,100.00,15.00,NULL,NULL,1,'2026-01-01 19:35:15'),(22,'Mandalina','fruit',22.00,90.00,15.00,NULL,NULL,1,'2026-01-01 19:35:15'),(23,'Şeftali','fruit',40.00,50.00,10.00,NULL,NULL,1,'2026-01-01 19:35:15'),(24,'Erik','fruit',50.00,40.00,5.00,NULL,NULL,1,'2026-01-01 19:35:15');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_coupons`
--

DROP TABLE IF EXISTS `user_coupons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_coupons` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `coupon_id` int NOT NULL,
  `used` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `coupon_id` (`coupon_id`),
  CONSTRAINT `user_coupons_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_coupons_ibfk_2` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_coupons`
--

LOCK TABLES `user_coupons` WRITE;
/*!40000 ALTER TABLE `user_coupons` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_coupons` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('customer','carrier','owner') NOT NULL,
  `address` text,
  `neighborhood` varchar(50) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `phone_number` varchar(20) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'cust','cust','customer','Vişnezade Mah. Şair Nedim Cad. No:12','Beşiktaş','cust@mail.com','5321000001','2026-01-01 19:35:15'),(2,'carr','carr','carrier','Merkez Mah. Abide-i Hürriyet Cad.','Şişli','carr@mail.com','5322000002','2026-01-01 19:35:15'),(3,'own','own','owner','Caferağa Mah. Moda Cad.','Kadıköy','own@mail.com','5323000003','2026-01-01 19:35:15'),(4,'ahmet','1234','customer','Teşvikiye Mah. Valikonağı Cad.','Şişli','ahmet@mail.com','5324000004','2026-01-01 19:35:15'),(5,'ayse','1234','customer','Rasimpaşa Mah. Rıhtım Cad.','Kadıköy','ayse@mail.com','5325000005','2026-01-01 19:35:15'),(6,'zeynep','1234','customer','Bağdat Cad. No:10','Kadıköy','zeynep@mail.com','5326000006','2026-01-01 19:35:15'),(7,'burak','1234','customer','Nispetiye Cad. No:5','Beşiktaş','burak@mail.com','5327000007','2026-01-01 19:35:15'),(8,'selin','1234','customer','Halaskargazi Cad.','Şişli','selin@mail.com','5328000008','2026-01-01 19:35:15');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `v_active_coupons`
--

DROP TABLE IF EXISTS `v_active_coupons`;
/*!50001 DROP VIEW IF EXISTS `v_active_coupons`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_active_coupons` AS SELECT 
 1 AS `id`,
 1 AS `code`,
 1 AS `discount_percentage`,
 1 AS `min_purchase_amount`,
 1 AS `max_discount_amount`,
 1 AS `valid_until`,
 1 AS `is_active`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_inventory_status`
--

DROP TABLE IF EXISTS `v_inventory_status`;
/*!50001 DROP VIEW IF EXISTS `v_inventory_status`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_inventory_status` AS SELECT 
 1 AS `id`,
 1 AS `name`,
 1 AS `type`,
 1 AS `stock`,
 1 AS `threshold`,
 1 AS `price`,
 1 AS `current_price`,
 1 AS `stock_status`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_products_sorted`
--

DROP TABLE IF EXISTS `v_products_sorted`;
/*!50001 DROP VIEW IF EXISTS `v_products_sorted`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_products_sorted` AS SELECT 
 1 AS `id`,
 1 AS `name`,
 1 AS `type`,
 1 AS `price`,
 1 AS `stock`,
 1 AS `threshold`,
 1 AS `image`,
 1 AS `image_type`,
 1 AS `is_active`,
 1 AS `created_at`*/;
SET character_set_client = @saved_cs_client;

--
-- Dumping routines for database 'greengrocer'
--

--
-- Final view structure for view `v_active_coupons`
--

/*!50001 DROP VIEW IF EXISTS `v_active_coupons`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_active_coupons` AS select `coupons`.`id` AS `id`,`coupons`.`code` AS `code`,`coupons`.`discount_percentage` AS `discount_percentage`,`coupons`.`min_purchase_amount` AS `min_purchase_amount`,`coupons`.`max_discount_amount` AS `max_discount_amount`,`coupons`.`valid_until` AS `valid_until`,`coupons`.`is_active` AS `is_active` from `coupons` where ((`coupons`.`is_active` = true) and ((`coupons`.`valid_until` is null) or (`coupons`.`valid_until` >= now()))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_inventory_status`
--

/*!50001 DROP VIEW IF EXISTS `v_inventory_status`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_inventory_status` AS select `p`.`id` AS `id`,`p`.`name` AS `name`,`p`.`type` AS `type`,`p`.`stock` AS `stock`,`p`.`threshold` AS `threshold`,`p`.`price` AS `price`,(case when (`p`.`stock` <= `p`.`threshold`) then (`p`.`price` * 2) else `p`.`price` end) AS `current_price`,(case when (`p`.`stock` <= `p`.`threshold`) then 'Low Stock' else 'In Stock' end) AS `stock_status` from `products` `p` where (`p`.`is_active` = true) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_products_sorted`
--

/*!50001 DROP VIEW IF EXISTS `v_products_sorted`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_products_sorted` AS select `products`.`id` AS `id`,`products`.`name` AS `name`,`products`.`type` AS `type`,`products`.`price` AS `price`,`products`.`stock` AS `stock`,`products`.`threshold` AS `threshold`,`products`.`image` AS `image`,`products`.`image_type` AS `image_type`,`products`.`is_active` AS `is_active`,`products`.`created_at` AS `created_at` from `products` where (`products`.`is_active` = true) order by `products`.`name` */;
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

-- Dump completed on 2026-01-01 22:36:31
