CREATE DATABASE  IF NOT EXISTS `chatapp_schema` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `chatapp_schema`;
-- MySQL dump 10.13  Distrib 8.0.31, for Win64 (x86_64)
--
-- Host: localhost    Database: chatapp_schema
-- ------------------------------------------------------
-- Server version	8.0.31

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
-- Table structure for table `chat`
--

DROP TABLE IF EXISTS `chat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat` (
  `chat_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `chat_user_sender` int unsigned NOT NULL,
  `last_message` text,
  `last_message_time` datetime DEFAULT NULL,
  `last_message_seen` tinyint(1) DEFAULT '1',
  `chat_uuid` bigint unsigned NOT NULL,
  `unseen_chats` int DEFAULT '0',
  PRIMARY KEY (`chat_id`),
  UNIQUE KEY `chat_uuid_UNIQUE` (`chat_uuid`),
  KEY `chat_uuid_idx` (`chat_uuid`) /*!80000 INVISIBLE */,
  KEY `chat_ibfk_1_idx` (`chat_user_sender`),
  CONSTRAINT `chat_ibfk_1` FOREIGN KEY (`chat_user_sender`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `message`
--

DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message` (
  `message_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `message_datetime` datetime NOT NULL,
  `message_text` text NOT NULL,
  `chat_uuid` bigint unsigned NOT NULL,
  `message_user_id` int unsigned NOT NULL,
  `message_seen` tinyint(1) NOT NULL DEFAULT '1',
  `is_file` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`message_id`),
  KEY `message_ibfk_1_idx` (`message_user_id`),
  KEY `message_ibfk_3` (`chat_uuid`),
  CONSTRAINT `message_ibfk_1` FOREIGN KEY (`message_user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `message_ibfk_3` FOREIGN KEY (`chat_uuid`) REFERENCES `chat` (`chat_uuid`)
) ENGINE=InnoDB AUTO_INCREMENT=314 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_login` varchar(255) DEFAULT NULL,
  `user_password` char(64) DEFAULT NULL,
  `salt` int NOT NULL,
  PRIMARY KEY (`user_id`),
  KEY `user_login_idx` (`user_login`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_contacts`
--

DROP TABLE IF EXISTS `user_contacts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_contacts` (
  `contact_user_id` int unsigned NOT NULL,
  `contact_friend_id` int unsigned NOT NULL,
  `contact_alias` varchar(255) NOT NULL,
  `contact_status` tinyint NOT NULL,
  `chat_uuid` varchar(255) NOT NULL,
  PRIMARY KEY (`contact_user_id`,`contact_friend_id`),
  KEY `contact_chat_uuid` (`chat_uuid`),
  KEY `user_contacts_ibfk_3_idx` (`contact_status`),
  KEY `user_contacts_ibfk_2_idx` (`contact_friend_id`),
  CONSTRAINT `user_contacts_ibfk_1` FOREIGN KEY (`contact_user_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `user_contacts_ibfk_2` FOREIGN KEY (`contact_friend_id`) REFERENCES `user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `user_contacts_ibfk_3` FOREIGN KEY (`contact_status`) REFERENCES `user_status` (`status_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_status`
--

DROP TABLE IF EXISTS `user_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_status` (
  `status_id` tinyint NOT NULL,
  `status_name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`status_id`),
  UNIQUE KEY `status_name_UNIQUE` (`status_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-12-03 13:26:45
