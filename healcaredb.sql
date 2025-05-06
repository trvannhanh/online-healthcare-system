-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: healcaredb
-- ------------------------------------------------------
-- Server version	8.0.41

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
-- Table structure for table `appointment`
--

DROP TABLE IF EXISTS `appointment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `appointment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int NOT NULL,
  `doctor_id` int NOT NULL,
  `appointment_date` datetime NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` enum('PENDING','CONFIRMED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  PRIMARY KEY (`id`),
  UNIQUE KEY `patient_id` (`patient_id`,`doctor_id`,`appointment_date`),
  KEY `doctor_id` (`doctor_id`),
  CONSTRAINT `appointment_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE CASCADE,
  CONSTRAINT `appointment_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appointment`
--

LOCK TABLES `appointment` WRITE;
/*!40000 ALTER TABLE `appointment` DISABLE KEYS */;
INSERT INTO `appointment` VALUES (1,1,3,'2025-04-10 10:00:00','2025-04-02 03:48:14','2025-04-02 03:48:14','PENDING'),(3,1,4,'2025-04-12 09:30:00','2025-04-02 03:48:14','2025-04-02 03:48:14','PENDING'),(4,2,3,'2025-04-13 11:00:00','2025-04-02 03:48:14','2025-04-02 03:48:14','PENDING'),(10,2,9,'2025-04-20 15:00:00','2025-04-22 02:16:27','2025-04-22 02:16:27','PENDING'),(12,2,4,'2025-04-25 15:00:00','2025-04-22 02:24:30','2025-04-22 02:24:30','PENDING'),(18,2,9,'2025-05-29 15:00:00','2025-04-22 03:06:26','2025-04-22 08:24:14','CANCELLED'),(19,2,9,'2025-05-11 15:00:00','2025-04-22 06:45:22','2025-04-22 08:30:12','CANCELLED'),(20,1,3,'2025-05-14 17:27:00','2025-04-23 09:27:57','2025-04-23 09:27:57','PENDING'),(21,1,3,'2025-05-20 16:29:00','2025-04-23 09:29:40','2025-04-23 09:29:40','PENDING'),(22,1,3,'2025-04-30 17:56:00','2025-04-23 10:56:14','2025-04-23 10:56:14','PENDING'),(23,21,14,'2025-05-05 14:30:00','2025-05-03 09:59:09','2025-05-04 14:42:48','COMPLETED');
/*!40000 ALTER TABLE `appointment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `doctor`
--

DROP TABLE IF EXISTS `doctor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doctor` (
  `id` int NOT NULL,
  `specialization` int DEFAULT NULL,
  `hospital` int DEFAULT NULL,
  `license_number` varchar(20) DEFAULT NULL,
  `is_verified` tinyint(1) DEFAULT '0',
  `bio` text,
  `experience_years` int DEFAULT NULL,
  `rating` float DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `license_number` (`license_number`),
  KEY `fk_hospital` (`hospital`),
  KEY `fk_specialization` (`specialization`),
  CONSTRAINT `doctor_ibfk_1` FOREIGN KEY (`id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_hospital` FOREIGN KEY (`hospital`) REFERENCES `hospital` (`hospital_id`),
  CONSTRAINT `fk_specialization` FOREIGN KEY (`specialization`) REFERENCES `specialization` (`specialization_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doctor`
--

LOCK TABLES `doctor` WRITE;
/*!40000 ALTER TABLE `doctor` DISABLE KEYS */;
INSERT INTO `doctor` VALUES (3,1,1,'LIC123456',1,'Bác sĩ nội khoa với 5 năm kinh nghiệm.',5,4.5),(4,2,2,'LIC654321',1,'Bác sĩ ngoại khoa với 10 năm kinh nghiệm.',10,4.8),(9,3,3,'LIC543222',0,'Orthopedic specialist',12,4.8),(10,4,3,'LIC543555',0,'Orthopedic specialist',12,4.8),(14,4,2,'83111763812',1,'Orthopedic specialist',0,0),(22,2,3,'83128763812',0,NULL,0,0);
/*!40000 ALTER TABLE `doctor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `health_records`
--

DROP TABLE IF EXISTS `health_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `health_records` (
  `id` int NOT NULL AUTO_INCREMENT,
  `patient_id` int NOT NULL,
  `doctor_id` int NOT NULL,
  `medical_history` text,
  `examination_results` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `patient_id` (`patient_id`),
  KEY `doctor_id` (`doctor_id`),
  CONSTRAINT `health_records_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE CASCADE,
  CONSTRAINT `health_records_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `health_records`
--

LOCK TABLES `health_records` WRITE;
/*!40000 ALTER TABLE `health_records` DISABLE KEYS */;
INSERT INTO `health_records` VALUES (1,1,3,'Không có tiền sử bệnh lý','Kết quả xét nghiệm bình thường.','2025-04-02 03:48:14','2025-04-02 03:48:14'),(2,2,4,'Tiểu đường type 2','Kết quả xét nghiệm đường huyết cao.','2025-04-02 03:48:14','2025-04-02 03:48:14');
/*!40000 ALTER TABLE `health_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hospital`
--

DROP TABLE IF EXISTS `hospital`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hospital` (
  `hospital_id` int NOT NULL AUTO_INCREMENT,
  `hospital_name` varchar(255) NOT NULL,
  PRIMARY KEY (`hospital_id`),
  UNIQUE KEY `hospital_name` (`hospital_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hospital`
--

LOCK TABLES `hospital` WRITE;
/*!40000 ALTER TABLE `hospital` DISABLE KEYS */;
INSERT INTO `hospital` VALUES (1,'Bệnh viện A'),(2,'Bệnh viện B'),(3,'General Hospital');
/*!40000 ALTER TABLE `hospital` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages` (
  `id` int NOT NULL AUTO_INCREMENT,
  `sender_id` int DEFAULT NULL,
  `receiver_id` int DEFAULT NULL,
  `message` text,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `sender_id` (`sender_id`),
  KEY `receiver_id` (`receiver_id`),
  CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
/*!40000 ALTER TABLE `messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `message` text,
  `sent_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `patient`
--

DROP TABLE IF EXISTS `patient`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient` (
  `id` int NOT NULL,
  `date_of_birth` date DEFAULT NULL,
  `insurance_number` varchar(20) DEFAULT NULL,
  `medical_history` text,
  PRIMARY KEY (`id`),
  CONSTRAINT `patient_ibfk_1` FOREIGN KEY (`id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient`
--

LOCK TABLES `patient` WRITE;
/*!40000 ALTER TABLE `patient` DISABLE KEYS */;
INSERT INTO `patient` VALUES (1,'1990-01-01','BH123456','Không có tiền sử bệnh lý'),(2,'1985-05-15','BH654321','Tiểu đường type 2'),(20,'1990-01-01','0666111555222',NULL),(21,'1990-01-01','0666111555222',NULL);
/*!40000 ALTER TABLE `patient` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment`
--

DROP TABLE IF EXISTS `payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `appointment_id` int DEFAULT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `payment_method` enum('Momo','VNPay','Stripe') DEFAULT NULL,
  `payment_status` enum('PENDING','SUCCESSFUL','FAILED') DEFAULT 'PENDING',
  `transaction_id` varchar(100) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `appointment_id` (`appointment_id`),
  CONSTRAINT `payment_ibfk_1` FOREIGN KEY (`appointment_id`) REFERENCES `appointment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment`
--

LOCK TABLES `payment` WRITE;
/*!40000 ALTER TABLE `payment` DISABLE KEYS */;
INSERT INTO `payment` VALUES (1,1,500.00,'Momo','PENDING','TRANSACTION123','2025-04-02 03:48:14'),(3,3,450.00,'Stripe','SUCCESSFUL','TRANSACTION789','2025-04-02 03:48:14'),(4,4,250.00,'Momo','FAILED','TRANSACTION321','2025-04-02 03:48:14'),(7,23,500.00,'Momo','SUCCESSFUL','ORDER_7_1746373933666','2025-05-03 17:00:00');
/*!40000 ALTER TABLE `payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rating`
--

DROP TABLE IF EXISTS `rating`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rating` (
  `id` int NOT NULL AUTO_INCREMENT,
  `doctor_id` int NOT NULL,
  `patient_id` int NOT NULL,
  `rating` int DEFAULT NULL,
  `comment` text,
  `created_date` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `doctor_id` (`doctor_id`),
  KEY `patient_id` (`patient_id`),
  CONSTRAINT `rating_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE CASCADE,
  CONSTRAINT `rating_ibfk_2` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE CASCADE,
  CONSTRAINT `rating_chk_1` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rating`
--

LOCK TABLES `rating` WRITE;
/*!40000 ALTER TABLE `rating` DISABLE KEYS */;
INSERT INTO `rating` VALUES (1,3,1,5,'Bác sĩ rất tận tình và chuyên nghiệp.','2025-04-10 12:00:00'),(2,4,2,4,'Bác sĩ giỏi nhưng thời gian chờ hơi lâu.','2025-04-11 15:00:00');
/*!40000 ALTER TABLE `rating` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `specialization`
--

DROP TABLE IF EXISTS `specialization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `specialization` (
  `specialization_id` int NOT NULL AUTO_INCREMENT,
  `specialization_name` varchar(100) NOT NULL,
  PRIMARY KEY (`specialization_id`),
  UNIQUE KEY `specialization_name` (`specialization_name`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `specialization`
--

LOCK TABLES `specialization` WRITE;
/*!40000 ALTER TABLE `specialization` DISABLE KEYS */;
INSERT INTO `specialization` VALUES (4,'Neurology'),(2,'Ngoại khoa'),(1,'Nội khoa'),(3,'Orthopedics');
/*!40000 ALTER TABLE `specialization` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `username` varchar(30) NOT NULL,
  `password` varchar(255) NOT NULL,
  `created_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `phone_number` varchar(10) DEFAULT NULL,
  `gender` enum('MALE','FEMALE','OTHER') DEFAULT NULL,
  `email` varchar(50) NOT NULL,
  `identity_number` varchar(12) DEFAULT NULL,
  `role` enum('ADMIN','DOCTOR','PATIENT') NOT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `phone_number` (`phone_number`),
  UNIQUE KEY `identity_number` (`identity_number`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'Nguyen','Van A','nguyenvana','password123','2025-04-02 10:48:14','0123456789','MALE','nguyenvana@example.com','123456789012','PATIENT','avatar1.png'),(2,'Tran','Thi B','tranthib','password123','2025-04-02 10:48:14','0987654321','FEMALE','tranthib@example.com','234567890123','PATIENT','avatar2.png'),(3,'Le','Van C','levanc','{noop}password123','2025-04-02 10:48:14','0912345678','MALE','levanc@example.com','345678901234','DOCTOR','avatar3.png'),(4,'Pham','Thi D','phamthid','password123','2025-04-02 10:48:14','0901234567','FEMALE','phamthid@example.com','456789012345','DOCTOR','avatar4.png'),(5,'Tran','Nguyen Linh Chi','linhchi123','{noop}12345678','2025-04-02 10:48:14','0909475806','FEMALE','chitrannguyenlinh@gmail.com','079304014219','ADMIN','avatar5.png'),(6,'Charlie','Davis','charliedavis','password123',NULL,'1234567890',NULL,'charlie@example.com',NULL,'DOCTOR',NULL),(9,'Charlie','Davis','charliedavis12','password123',NULL,'1234561111',NULL,'charlie2@example.com',NULL,'DOCTOR',NULL),(10,'Charles','Davis','charliedavis4','password123',NULL,'1234561555',NULL,'charles.davis@example.com',NULL,'DOCTOR',NULL),(12,'test1','t','test1','$2a$10$U3xAhVBJ8nfj59F7e7gfiOOp1dI4da3kEfuqQGLF99NbU0ofBcqT2',NULL,'0123987654',NULL,'test1@gmail.com',NULL,'ADMIN','https://res.cloudinary.com/dqpkxxzaf/image/upload/v1744887357/azltogghcyqkysaewkby.jpg'),(13,'','test2','test2','$2a$10$jDE2nOkaPXDefOwtDuySQOeVzxZ5MGs65ASsA.TJ8/xGanAfi2y0S',NULL,'0986756789',NULL,'test2@gmail.com',NULL,'ADMIN','https://res.cloudinary.com/dqpkxxzaf/image/upload/v1744887955/gombsu2rhgej2xyjj0wc.jpg'),(14,'','test2','test3','$2a$10$d4cfIQV0BTktTPNRoM/eaOKj.BqLaQve5A5.AGLJkRUcwTdhhhwq2',NULL,'053826451',NULL,'test3@gmail.com',NULL,'DOCTOR','https://res.cloudinary.com/dqpkxxzaf/image/upload/v1745248585/xoehoednqfk1pyk09zrn.jpg'),(20,'','test4','test4','$2a$10$UpfSNKYS3tFMMQl.ZnZjm..3BOCyAZL7GGgjSdJFsPXYg1n9kf7DK',NULL,'0126856711',NULL,'test4@gmail.com',NULL,'PATIENT','https://res.cloudinary.com/dqpkxxzaf/image/upload/v1746172170/oofnz4yfx5iq96gaag2c.jpg'),(21,'Nguyen','test6','test6','$2a$10$TyqoAQXLZ1sl5ECQDzRnt.7SyJC4m7iI3Q3ZzCISx9iWqCYY/ECum',NULL,'0162748391',NULL,'test6@gmail.com',NULL,'PATIENT','https://res.cloudinary.com/dqpkxxzaf/image/upload/v1746173523/eaopmc6vnc6w6f30dgbo.jpg'),(22,'','test5','test5','$2a$10$bF2LCaiX3oJgMSxdMDr6XeXjp7B5vtJ8OrKrBo71S/2l3ZBOhG4pa',NULL,'0777125741',NULL,'test5@gmail.com',NULL,'DOCTOR','https://res.cloudinary.com/dqpkxxzaf/image/upload/v1746173589/oi6kflv4rhbplauvva94.jpg');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-05 23:52:58
