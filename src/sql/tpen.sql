-- MySQL dump 10.13  Distrib 5.6.27, for debian-linux-gnu (x86_64)
--
-- Host: 127.0.0.1    Database: tpenPaleography
-- ------------------------------------------------------
-- Server version	5.5.5-10.0.22-MariaDB-1~trusty-wsrep-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admins`
--

DROP TABLE IF EXISTS `admins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `admins` (
  `uid` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `annotation`
--

DROP TABLE IF EXISTS `annotation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `annotation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `h` int(11) NOT NULL,
  `w` int(11) NOT NULL,
  `folio` int(11) NOT NULL,
  `projectID` int(11) NOT NULL,
  `text` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `archivedannotation`
--

DROP TABLE IF EXISTS `archivedannotation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `archivedannotation` (
  `archivedID` int(11) NOT NULL AUTO_INCREMENT,
  `id` int(11) NOT NULL,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `h` int(11) NOT NULL,
  `w` int(11) NOT NULL,
  `folio` int(11) NOT NULL,
  `projectID` int(11) NOT NULL,
  `text` text NOT NULL,
  PRIMARY KEY (`archivedID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `archivedtranscription`
--

DROP TABLE IF EXISTS `archivedtranscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `archivedtranscription` (
  `folio` int(11) NOT NULL,
  `line` int(11) NOT NULL,
  `comment` text NOT NULL,
  `text` mediumtext NOT NULL,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `creator` int(11) NOT NULL,
  `projectID` int(11) NOT NULL DEFAULT '0',
  `id` int(11) NOT NULL,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `width` int(11) NOT NULL,
  `height` int(11) NOT NULL,
  `uniqueID` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`uniqueID`),
  KEY `folio` (`folio`),
  KEY `line` (`line`),
  KEY `creator` (`creator`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `archives`
--

DROP TABLE IF EXISTS `archives`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `archives` (
  `name` varchar(512) NOT NULL,
  `baseImageUrl` varchar(512) NOT NULL,
  `citation` varchar(512) NOT NULL,
  `eula` text NOT NULL,
  `message` text,
  `cookieURL` text,
  `uname` text,
  `pass` text,
  `local` tinyint(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `badfols`
--

DROP TABLE IF EXISTS `badfols`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `badfols` (
  `pageNumber` int(11) DEFAULT NULL,
  `cnt` int(11) DEFAULT NULL,
  KEY `a` (`pageNumber`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `biblio`
--

DROP TABLE IF EXISTS `biblio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `biblio` (
  `author` varchar(512) NOT NULL,
  `titleM` mediumtext NOT NULL,
  `titleA` mediumtext NOT NULL,
  `title` mediumtext NOT NULL,
  `vol` varchar(512) NOT NULL,
  `date` varchar(512) NOT NULL,
  `pagination` varchar(512) NOT NULL,
  `pubplace` varchar(512) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `subtitle` mediumtext NOT NULL,
  `series` mediumtext NOT NULL,
  `type` tinyint(4) NOT NULL,
  `editor` varchar(512) NOT NULL,
  `publisher` varchar(512) NOT NULL,
  `multivol` varchar(512) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `author` (`author`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bibliorefs`
--

DROP TABLE IF EXISTS `bibliorefs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bibliorefs` (
  `tract` varchar(255) NOT NULL,
  `page` varchar(255) NOT NULL,
  `id` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `blobmatches`
--

DROP TABLE IF EXISTS `blobmatches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blobmatches` (
  `img1` varchar(255) NOT NULL,
  `blob1` varchar(255) NOT NULL,
  `img2` varchar(255) NOT NULL,
  `blob2` varchar(255) NOT NULL,
  KEY `img1` (`img1`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `blobs`
--

DROP TABLE IF EXISTS `blobs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `blobs` (
  `img` varchar(256) NOT NULL,
  `blob` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `x` int(11) NOT NULL,
  `h` int(11) NOT NULL,
  `w` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `buttons`
--

DROP TABLE IF EXISTS `buttons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `buttons` (
  `uid` int(11) NOT NULL,
  `position` int(11) NOT NULL,
  `text` varchar(256) NOT NULL,
  `param1` varchar(512) NOT NULL DEFAULT '',
  `param2` varchar(512) NOT NULL DEFAULT '',
  `param3` varchar(512) NOT NULL DEFAULT '',
  `param4` varchar(512) NOT NULL DEFAULT '',
  `param5` varchar(512) NOT NULL DEFAULT '',
  `description` varchar(512) NOT NULL DEFAULT '',
  `color` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `capelli`
--

DROP TABLE IF EXISTS `capelli`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `capelli` (
  `image` varchar(512) NOT NULL,
  `label` varchar(512) NOT NULL DEFAULT 'none',
  `group` varchar(256) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `collection` varchar(512) NOT NULL DEFAULT 'capelli',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `charactercount`
--

DROP TABLE IF EXISTS `charactercount`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `charactercount` (
  `count` int(11) NOT NULL,
  `img` varchar(256) NOT NULL,
  `blob` int(11) NOT NULL,
  `MS` varchar(128) NOT NULL,
  KEY `blob` (`blob`),
  KEY `img` (`img`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `citymap`
--

DROP TABLE IF EXISTS `citymap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `citymap` (
  `city` text NOT NULL,
  `value` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comments`
--

DROP TABLE IF EXISTS `comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `comments` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `security` int(11) NOT NULL,
  `owner` int(11) NOT NULL,
  `type` int(11) NOT NULL,
  `text` text NOT NULL,
  `shortText` varchar(255) NOT NULL,
  `tract` varchar(255) NOT NULL,
  `updated` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' ON UPDATE CURRENT_TIMESTAMP,
  `paragraph` varchar(255) NOT NULL,
  `grp` int(11) NOT NULL,
  `response` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `foliomap`
--

DROP TABLE IF EXISTS `foliomap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `foliomap` (
  `folio` int(11) NOT NULL,
  `msPage` int(11) NOT NULL,
  PRIMARY KEY (`folio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `folios`
--

DROP TABLE IF EXISTS `folios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `folios` (
  `pageNumber` int(11) NOT NULL AUTO_INCREMENT,
  `uri` text NOT NULL,
  `collection` varchar(512) NOT NULL,
  `pageName` varchar(512) NOT NULL,
  `imageName` varchar(512) NOT NULL,
  `archive` varchar(512) NOT NULL,
  `force` int(11) NOT NULL DEFAULT '1',
  `msID` int(11) NOT NULL,
  `sequence` int(11) DEFAULT '0',
  `canvas` varchar(512) DEFAULT '',
  `paleography` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `pageNumber` (`pageNumber`),
  KEY `coll` (`collection`(255)),
  KEY `imageName` (`imageName`(255)),
  KEY `archive` (`archive`(255)),
  KEY `msID` (`msID`),
  KEY `pagename` (`pageName`(255))
) ENGINE=InnoDB AUTO_INCREMENT=956 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `groupmembers`
--

DROP TABLE IF EXISTS `groupmembers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `groupmembers` (
  `UID` int(11) NOT NULL,
  `GID` int(11) NOT NULL,
  `role` varchar(256) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `groups`
--

DROP TABLE IF EXISTS `groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `groups` (
  `name` varchar(512) NOT NULL,
  `GID` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`GID`)
) ENGINE=InnoDB AUTO_INCREMENT=128 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hotkeys`
--

DROP TABLE IF EXISTS `hotkeys`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hotkeys` (
  `key` int(11) NOT NULL,
  `position` int(11) NOT NULL,
  `uid` int(11) NOT NULL,
  `projectID` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `image`
--

DROP TABLE IF EXISTS `image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `image` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `uploader` int(11) DEFAULT '0' COMMENT 'the userID who uploaded the image',
  `project` varchar(255) DEFAULT NULL COMMENT 'project image belongs to',
  `path` varchar(255) NOT NULL DEFAULT '' COMMENT 'the place where image stored on server',
  `size` bigint(20) DEFAULT NULL COMMENT 'size of uploaded file (image)',
  `upload_date` datetime DEFAULT NULL,
  `permission` int(2) NOT NULL DEFAULT '0' COMMENT '0: privaet, 1: public',
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `imagecache`
--

DROP TABLE IF EXISTS `imagecache`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `imagecache` (
  `folio` int(11) NOT NULL,
  `image` longblob NOT NULL,
  `age` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `count` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fol` (`folio`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `imagepositions`
--

DROP TABLE IF EXISTS `imagepositions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `imagepositions` (
  `folio` int(11) NOT NULL,
  `line` int(11) NOT NULL,
  `bottom` int(11) NOT NULL,
  `top` int(11) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `colstart` int(11) NOT NULL,
  `width` int(11) NOT NULL,
  `dummy` int(11) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`id`),
  KEY `folio` (`folio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `imagerequest`
--

DROP TABLE IF EXISTS `imagerequest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `imagerequest` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `elapsedTime` int(11) NOT NULL,
  `UID` int(11) NOT NULL,
  `folio` int(11) NOT NULL,
  `cacheHit` int(11) NOT NULL,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `succeeded` int(11) NOT NULL,
  `msg` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `imgtmp`
--

DROP TABLE IF EXISTS `imgtmp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `imgtmp` (
  `img` varchar(512) COLLATE utf8_bin NOT NULL,
  `page` varchar(512) COLLATE utf8_bin NOT NULL,
  KEY `image` (`img`(255)),
  KEY `page` (`page`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ipr`
--

DROP TABLE IF EXISTS `ipr`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ipr` (
  `uid` int(11) NOT NULL,
  `archive` varchar(512) NOT NULL,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `linebreakingtext`
--

DROP TABLE IF EXISTS `linebreakingtext`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `linebreakingtext` (
  `projectID` int(11) NOT NULL,
  `remainingText` longtext NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `manuscript`
--

DROP TABLE IF EXISTS `manuscript`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `manuscript` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `city` varchar(512) NOT NULL,
  `archive` varchar(512) NOT NULL,
  `repository` varchar(512) NOT NULL,
  `msIdentifier` varchar(512) NOT NULL,
  `restricted` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `city` (`city`(255)),
  KEY `msIdentifier` (`msIdentifier`(255))
) ENGINE=InnoDB AUTO_INCREMENT=74 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `manuscriptpermissions`
--

DROP TABLE IF EXISTS `manuscriptpermissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `manuscriptpermissions` (
  `msID` int(11) NOT NULL,
  `uid` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `metadata`
--

DROP TABLE IF EXISTS `metadata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metadata` (
  `title` text NOT NULL,
  `subject` text NOT NULL,
  `language` text NOT NULL,
  `author` text NOT NULL,
  `date` text NOT NULL,
  `location` text NOT NULL,
  `description` text NOT NULL,
  `subtitle` text NOT NULL,
  `msIdentifier` text NOT NULL,
  `msSettlement` text NOT NULL,
  `msIdNumber` text NOT NULL,
  `msRepository` text NOT NULL,
  `msCollection` text NOT NULL,
  `projectID` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `oldfimagepositions`
--

DROP TABLE IF EXISTS `oldfimagepositions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `oldfimagepositions` (
  `folio` int(11) NOT NULL,
  `line` int(11) NOT NULL,
  `bottom` int(11) NOT NULL,
  `top` int(11) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `colstart` int(11) NOT NULL,
  `width` int(11) NOT NULL,
  `dummy` int(11) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`id`),
  KEY `folio` (`folio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `paragraphs`
--

DROP TABLE IF EXISTS `paragraphs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `paragraphs` (
  `tract` varchar(512) NOT NULL,
  `sentences` int(12) NOT NULL,
  `words` int(12) NOT NULL,
  `characters` int(12) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `partnerproject`
--

DROP TABLE IF EXISTS `partnerproject`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `partnerproject` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` int(11) NOT NULL,
  `projectID` int(11) NOT NULL,
  `name` varchar(512) NOT NULL,
  `description` text NOT NULL,
  `url` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `project`
--

DROP TABLE IF EXISTS `project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project` (
  `grp` int(11) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(512) NOT NULL,
  `schemaURL` text NOT NULL,
  `linebreakCharacterLimit` int(11) NOT NULL DEFAULT '7500',
  `linebreakSymbol` varchar(256) NOT NULL DEFAULT '-',
  `imageBounding` varchar(255) NOT NULL DEFAULT 'lines',
  `partner` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=128 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `projectbuttons`
--

DROP TABLE IF EXISTS `projectbuttons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projectbuttons` (
  `project` int(11) NOT NULL,
  `position` int(11) NOT NULL,
  `text` varchar(256) NOT NULL,
  `param1` varchar(512) NOT NULL DEFAULT '',
  `param2` varchar(512) NOT NULL DEFAULT '',
  `param3` varchar(512) NOT NULL DEFAULT '',
  `param4` varchar(512) NOT NULL DEFAULT '',
  `param5` varchar(512) NOT NULL DEFAULT '',
  `description` text NOT NULL,
  `color` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `projectfolios`
--

DROP TABLE IF EXISTS `projectfolios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projectfolios` (
  `position` int(11) NOT NULL,
  `project` int(11) NOT NULL,
  `folio` int(11) NOT NULL,
  KEY `proj` (`project`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `projectheader`
--

DROP TABLE IF EXISTS `projectheader`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projectheader` (
  `projectID` int(11) NOT NULL,
  `header` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `projectimagepositions`
--

DROP TABLE IF EXISTS `projectimagepositions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projectimagepositions` (
  `folio` int(11) NOT NULL,
  `line` int(11) NOT NULL,
  `bottom` int(11) NOT NULL,
  `top` int(11) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `colstart` int(11) NOT NULL,
  `width` int(11) NOT NULL,
  `dummy` int(11) NOT NULL DEFAULT '-1',
  `project` int(11) NOT NULL,
  `linebreakSymbol` varchar(10) NOT NULL DEFAULT '-',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `projectlog`
--

DROP TABLE IF EXISTS `projectlog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projectlog` (
  `projectID` int(11) NOT NULL,
  `uid` int(11) NOT NULL,
  `content` text CHARACTER SET latin1 NOT NULL,
  `creationDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `projectlogbackup`
--

DROP TABLE IF EXISTS `projectlogbackup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projectlogbackup` (
  `projectID` int(11) NOT NULL,
  `uid` int(11) NOT NULL,
  `content` text CHARACTER SET latin1 NOT NULL,
  `creationDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `projectpermissions`
--

DROP TABLE IF EXISTS `projectpermissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projectpermissions` (
  `allow_OAC_read` tinyint(1) DEFAULT NULL,
  `allow_OAC_write` tinyint(1) DEFAULT NULL,
  `allow_export` tinyint(1) DEFAULT NULL,
  `allow_public_copy` tinyint(1) DEFAULT NULL,
  `allow_public_modify` tinyint(1) DEFAULT NULL,
  `allow_public_modify_annotation` tinyint(1) DEFAULT NULL,
  `allow_public_modify_buttons` tinyint(1) DEFAULT NULL,
  `allow_public_modify_line_parsing` tinyint(1) DEFAULT NULL,
  `allow_public_modify_metadata` tinyint(1) DEFAULT NULL,
  `allow_public_modify_notes` tinyint(1) DEFAULT NULL,
  `allow_public_read_transcription` tinyint(1) DEFAULT NULL,
  `projectID` int(11) NOT NULL,
  PRIMARY KEY (`projectID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `projectpriorities`
--

DROP TABLE IF EXISTS `projectpriorities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projectpriorities` (
  `uid` int(11) NOT NULL,
  `priority` int(11) NOT NULL,
  `projectID` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sentences`
--

DROP TABLE IF EXISTS `sentences`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sentences` (
  `sentence` text NOT NULL,
  `length` int(12) NOT NULL,
  `period` int(12) NOT NULL,
  `question` int(12) NOT NULL,
  `exclaimation` int(12) NOT NULL,
  `comma` int(12) NOT NULL,
  `semicolon` int(12) NOT NULL,
  `colon` int(12) NOT NULL,
  `tract` varchar(512) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sources`
--

DROP TABLE IF EXISTS `sources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sources` (
  `book` varchar(512) NOT NULL,
  `chapter` varchar(512) NOT NULL,
  `verse` varchar(512) NOT NULL,
  `tract` varchar(512) NOT NULL,
  `biblioId` int(11) NOT NULL,
  `pageStart` int(11) NOT NULL,
  `loc` varchar(512) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `exclude` int(1) NOT NULL DEFAULT '0',
  `quoteId` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `biblioId` (`biblioId`),
  KEY `book` (`book`(255)),
  KEY `chapter` (`chapter`(255)),
  KEY `verse` (`verse`(255)),
  KEY `tract` (`tract`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tagtracking`
--

DROP TABLE IF EXISTS `tagtracking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tagtracking` (
  `tag` varchar(256) COLLATE utf8_bin NOT NULL,
  `folio` int(11) NOT NULL,
  `line` int(11) NOT NULL,
  `projectID` int(11) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `folio` (`folio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tasks`
--

DROP TABLE IF EXISTS `tasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tasks` (
  `project` int(11) NOT NULL,
  `beginFolio` int(11) NOT NULL,
  `endFolio` int(11) NOT NULL,
  `UID` int(11) NOT NULL,
  `title` varchar(512) NOT NULL,
  `id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tools`
--

DROP TABLE IF EXISTS `tools`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tools` (
  `tool` varchar(512) DEFAULT '',
  `uid` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `transcription`
--

DROP TABLE IF EXISTS `transcription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transcription` (
  `folio` int(11) NOT NULL,
  `line` int(11) NOT NULL,
  `comment` text NOT NULL,
  `text` mediumtext NOT NULL,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `creator` int(11) NOT NULL,
  `projectID` int(11) NOT NULL DEFAULT '0',
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `width` int(11) NOT NULL,
  `height` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `folio` (`folio`),
  KEY `line` (`line`),
  KEY `creator` (`creator`),
  KEY `proj` (`projectID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `Uname` varchar(255) CHARACTER SET latin1 NOT NULL,
  `UID` int(11) NOT NULL AUTO_INCREMENT,
  `pass` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT 'DRUPAL_LOGIN',
  `lname` varchar(512) CHARACTER SET latin1 NOT NULL DEFAULT 'new',
  `fname` varchar(512) CHARACTER SET latin1 NOT NULL DEFAULT 'new',
  `openID` varchar(1024) CHARACTER SET latin1 NOT NULL DEFAULT 'DRUPAL_LOGIN',
  `accepted` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `hasAccepted` int(11) DEFAULT '0',
  `lastActive` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `email` varchar(254) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`UID`)
) ENGINE=InnoDB AUTO_INCREMENT=639 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usertools`
--

DROP TABLE IF EXISTS `usertools`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usertools` (
  `projectID` int(11) NOT NULL,
  `url` text NOT NULL,
  `name` varchar(512) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `welcomemessage`
--

DROP TABLE IF EXISTS `welcomemessage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `welcomemessage` (
  `msg` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `words`
--

DROP TABLE IF EXISTS `words`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `words` (
  `tract` varchar(512) NOT NULL,
  `word` varchar(512) NOT NULL,
  `root` varchar(512) NOT NULL,
  `folio` varchar(512) NOT NULL,
  `line` varchar(512) NOT NULL,
  `paragraph` varchar(512) NOT NULL,
  `sentence` varchar(512) NOT NULL,
  `id` int(12) NOT NULL AUTO_INCREMENT,
  `page` int(12) NOT NULL,
  `length` int(12) NOT NULL,
  `def` text NOT NULL,
  `count` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `word` (`word`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `xml`
--

DROP TABLE IF EXISTS `xml`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `xml` (
  `work` varchar(512) NOT NULL,
  `text` mediumtext NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'tpenPaleography'
--

--
-- Dumping routines for database 'tpenPaleography'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-12-18 17:43:32
