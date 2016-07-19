-- phpMyAdmin SQL Dump
-- version 3.3.7deb5build0.10.10.1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: May 01, 2012 at 11:49 AM
-- Server version: 5.1.49
-- PHP Version: 5.3.3-1ubuntu9.10

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `tm`
--

-- --------------------------------------------------------

--
-- Table structure for table `admins`
--

CREATE TABLE IF NOT EXISTS `admins` (
  `uid` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Dumping data for table `admins`
--


-- --------------------------------------------------------

--
-- Table structure for table `annotation`
--

CREATE TABLE IF NOT EXISTS `annotation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `h` int(11) NOT NULL,
  `w` int(11) NOT NULL,
  `folio` int(11) NOT NULL,
  `projectID` int(11) NOT NULL,
  `text` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=8 ;

--
-- Dumping data for table `annotation`
--


-- --------------------------------------------------------

--
-- Table structure for table `archivedannotation`
--

CREATE TABLE IF NOT EXISTS `archivedannotation` (
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
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- Dumping data for table `archivedannotation`
--


-- --------------------------------------------------------

--
-- Table structure for table `archivedAnnotation`
--

CREATE TABLE IF NOT EXISTS `archivedAnnotation` (
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
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=13 ;

--
-- Dumping data for table `archivedAnnotation`
--


-- --------------------------------------------------------

--
-- Table structure for table `archivedTranscription`
--

CREATE TABLE IF NOT EXISTS `archivedTranscription` (
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
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=79224 ;

--
-- Dumping data for table `archivedTranscription`
--


-- --------------------------------------------------------

--
-- Table structure for table `archives`
--

CREATE TABLE IF NOT EXISTS `archives` (
  `name` varchar(512) NOT NULL,
  `baseImageUrl` varchar(512) NOT NULL,
  `citation` varchar(512) NOT NULL,
  `eula` text NOT NULL,
  `message` text,
  `uname` varchar(255) NOT NULL,
  `pass` varchar(255) NOT NULL,
  `cookieURL` text NOT NULL,
  `local` tinyint(1) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `archives`
--

INSERT INTO `archives` (`name`, `baseImageUrl`, `citation`, `eula`, `message`, `uname`, `pass`, `cookieURL`, `local`) VALUES
('ENAP', '', '', '', '', '', '', '', 0);

-- --------------------------------------------------------

--
-- Table structure for table `badFols`
--

CREATE TABLE IF NOT EXISTS `badFols` (
  `pageNumber` int(11) DEFAULT NULL,
  `cnt` int(11) DEFAULT NULL,
  KEY `a` (`pageNumber`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `badFols`
--


-- --------------------------------------------------------

--
-- Table structure for table `biblio`
--

CREATE TABLE IF NOT EXISTS `biblio` (
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
  KEY `author` (`author`(333))
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=414 ;

--
-- Dumping data for table `biblio`
--


-- --------------------------------------------------------

--
-- Table structure for table `bibliorefs`
--

CREATE TABLE IF NOT EXISTS `bibliorefs` (
  `tract` varchar(255) NOT NULL,
  `page` varchar(255) NOT NULL,
  `id` varchar(255) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `bibliorefs`
--


-- --------------------------------------------------------

--
-- Table structure for table `blobmatches`
--

CREATE TABLE IF NOT EXISTS `blobmatches` (
  `img1` varchar(255) NOT NULL,
  `blob1` varchar(255) NOT NULL,
  `img2` varchar(255) NOT NULL,
  `blob2` varchar(255) NOT NULL,
  KEY `img1` (`img1`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `blobmatches`
--


-- --------------------------------------------------------

--
-- Table structure for table `blobs`
--

CREATE TABLE IF NOT EXISTS `blobs` (
  `img` varchar(256) NOT NULL,
  `blob` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  `x` int(11) NOT NULL,
  `h` int(11) NOT NULL,
  `w` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `blobs`
--


-- --------------------------------------------------------

--
-- Table structure for table `buttons`
--

CREATE TABLE IF NOT EXISTS `buttons` (
  `uid` int(11) NOT NULL,
  `position` int(11) NOT NULL,
  `text` varchar(256) NOT NULL,
  `param1` varchar(512) NOT NULL DEFAULT '',
  `param2` varchar(512) NOT NULL DEFAULT '',
  `param3` varchar(512) NOT NULL DEFAULT '',
  `param4` varchar(512) NOT NULL DEFAULT '',
  `param5` varchar(512) NOT NULL DEFAULT '',
  `description` varchar(512) NOT NULL DEFAULT '',
  `color` varchar(255) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `buttons`
--


-- --------------------------------------------------------

--
-- Table structure for table `capelli`
--

CREATE TABLE IF NOT EXISTS `capelli` (
  `image` varchar(512) NOT NULL,
  `label` varchar(512) NOT NULL DEFAULT 'none',
  `group` varchar(256) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `collection` varchar(512) NOT NULL DEFAULT 'capelli',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=811 ;

--
-- Dumping data for table `capelli`
--


-- --------------------------------------------------------

--
-- Table structure for table `charactercount`
--

CREATE TABLE IF NOT EXISTS `charactercount` (
  `count` int(11) NOT NULL,
  `img` varchar(256) NOT NULL,
  `blob` int(11) NOT NULL,
  `MS` varchar(128) NOT NULL,
  KEY `blob` (`blob`),
  KEY `img` (`img`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `charactercount`
--


-- --------------------------------------------------------

--
-- Table structure for table `citymap`
--

CREATE TABLE IF NOT EXISTS `citymap` (
  `city` text NOT NULL,
  `value` text NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `citymap`
--

INSERT INTO `citymap` (`city`, `value`) VALUES
('Cambridge', '');

-- --------------------------------------------------------

--
-- Table structure for table `Comments`
--

CREATE TABLE IF NOT EXISTS `Comments` (
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
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=54 ;

--
-- Dumping data for table `Comments`
--


-- --------------------------------------------------------

--
-- Table structure for table `folioMap`
--

CREATE TABLE IF NOT EXISTS `folioMap` (
  `folio` int(11) NOT NULL,
  `msPage` int(11) NOT NULL,
  PRIMARY KEY (`folio`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `folioMap`
--


-- --------------------------------------------------------

--
-- Table structure for table `foliomap`
--

CREATE TABLE IF NOT EXISTS `foliomap` (
  `folio` int(11) NOT NULL,
  `msPage` int(11) NOT NULL,
  PRIMARY KEY (`folio`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `foliomap`
--


-- --------------------------------------------------------

--
-- Table structure for table `folios`
--

CREATE TABLE IF NOT EXISTS `folios` (
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
  KEY `coll` (`collection`(333)),
  KEY `imageName` (`imageName`(333)),
  KEY `archive` (`archive`(333)),
  KEY `msID` (`msID`),
  KEY `pagename` (`pageName`(333))
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=12841939 ;

--
-- Dumping data for table `folios`
--

INSERT INTO `folios` (`pageNumber`, `uri`, `collection`, `pageName`, `imageName`, `archive`, `force`, `msID`, `sequence`, `canvas`, `paleography`) VALUES
(1, 'http://normananonymous.org/images/415_001_TC_46z.jpg', '', '001', 'http://normananonymous.org/images/415_001_TC_46z.jpg', 'ENAP', 1, 1, 0, '', '2012-05-01 10:12:20');

-- --------------------------------------------------------

--
-- Table structure for table `groupMembers`
--

CREATE TABLE IF NOT EXISTS `groupMembers` (
  `UID` int(11) NOT NULL,
  `GID` int(11) NOT NULL,
  `role` varchar(256) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `groupMembers`
--

INSERT INTO `groupMembers` (`UID`, `GID`, `role`) VALUES
(1, 619, 'Leader');

-- --------------------------------------------------------

--
-- Table structure for table `groups`
--

CREATE TABLE IF NOT EXISTS `groups` (
  `name` varchar(512) NOT NULL,
  `GID` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`GID`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=620 ;

--
-- Dumping data for table `groups`
--

INSERT INTO `groups` (`name`, `GID`) VALUES
('Cambridge, Corpus Christi College 415 project', 619);

-- --------------------------------------------------------

--
-- Table structure for table `hotkeys`
--

CREATE TABLE IF NOT EXISTS `hotkeys` (
  `key` int(11) NOT NULL,
  `position` int(11) NOT NULL,
  `uid` int(11) NOT NULL,
  `projectID` int(11) NOT NULL DEFAULT '0'
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `hotkeys`
--


-- --------------------------------------------------------

--
-- Table structure for table `imagecache`
--

CREATE TABLE IF NOT EXISTS `imagecache` (
  `folio` int(11) NOT NULL,
  `image` longblob NOT NULL,
  `age` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- Dumping data for table `imagecache`
--


-- --------------------------------------------------------

--
-- Table structure for table `imageCache`
--

CREATE TABLE IF NOT EXISTS `imageCache` (
  `folio` int(11) NOT NULL,
  `image` longblob NOT NULL,
  `age` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `count` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `folio` (`folio`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=56 ;

--
-- Dumping data for table `imageCache`
--


-- --------------------------------------------------------

--
-- Table structure for table `imagepositions`
--

CREATE TABLE IF NOT EXISTS `imagepositions` (
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
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=90466 ;

--
-- Dumping data for table `imagepositions`
--

INSERT INTO `imagepositions` (`folio`, `line`, `bottom`, `top`, `id`, `colstart`, `width`, `dummy`) VALUES
(1, 1, 24, 0, 90443, 1, 461, -1),
(1, 2, 144, 24, 90444, 1, 461, -1),
(1, 3, 175, 144, 90445, 1, 461, -1),
(1, 4, 200, 175, 90446, 1, 461, -1),
(1, 5, 224, 200, 90447, 1, 461, -1),
(1, 6, 249, 224, 90448, 1, 461, -1),
(1, 7, 275, 249, 90449, 1, 461, -1),
(1, 8, 300, 275, 90450, 1, 461, -1),
(1, 9, 326, 300, 90451, 1, 461, -1),
(1, 10, 353, 326, 90452, 1, 461, -1),
(1, 11, 377, 353, 90453, 1, 461, -1),
(1, 12, 401, 377, 90454, 1, 461, -1),
(1, 13, 428, 401, 90455, 1, 461, -1),
(1, 14, 452, 428, 90456, 1, 461, -1),
(1, 15, 479, 452, 90457, 1, 461, -1),
(1, 16, 503, 479, 90458, 1, 461, -1),
(1, 17, 528, 503, 90459, 1, 461, -1),
(1, 18, 554, 528, 90460, 1, 461, -1),
(1, 19, 579, 554, 90461, 1, 461, -1),
(1, 20, 604, 579, 90462, 1, 461, -1),
(1, 21, 630, 604, 90463, 1, 461, -1),
(1, 22, 656, 630, 90464, 1, 461, -1),
(1, 23, 741, 656, 90465, 1, 461, -1);

-- --------------------------------------------------------

--
-- Table structure for table `imageRequest`
--

CREATE TABLE IF NOT EXISTS `imageRequest` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `elapsedTime` int(11) NOT NULL,
  `UID` int(11) NOT NULL,
  `folio` int(11) NOT NULL,
  `cacheHit` int(11) NOT NULL,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `succeeded` int(11) NOT NULL,
  `msg` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=885 ;

--
-- Dumping data for table `imageRequest`
--

INSERT INTO `imageRequest` (`id`, `elapsedTime`, `UID`, `folio`, `cacheHit`, `date`, `succeeded`, `msg`) VALUES
(884, 803, 0, 1, 0, '2012-05-01 10:15:04', 1, '');

-- --------------------------------------------------------

--
-- Table structure for table `imgTmp`
--

CREATE TABLE IF NOT EXISTS `imgTmp` (
  `img` varchar(512) COLLATE utf8_bin NOT NULL,
  `page` varchar(512) COLLATE utf8_bin NOT NULL,
  KEY `image` (`img`(333)),
  KEY `page` (`page`(333))
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Dumping data for table `imgTmp`
--


-- --------------------------------------------------------

--
-- Table structure for table `ipr`
--

CREATE TABLE IF NOT EXISTS `ipr` (
  `uid` int(11) NOT NULL,
  `archive` varchar(512) NOT NULL,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ipr`
--


-- --------------------------------------------------------

--
-- Table structure for table `linebreakingText`
--

CREATE TABLE IF NOT EXISTS `linebreakingText` (
  `projectID` int(11) NOT NULL,
  `remainingText` longtext NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `linebreakingText`
--


-- --------------------------------------------------------

--
-- Table structure for table `manuscript`
--

CREATE TABLE IF NOT EXISTS `manuscript` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `city` varchar(512) NOT NULL,
  `archive` varchar(512) NOT NULL,
  `repository` varchar(512) NOT NULL,
  `msIdentifier` varchar(512) NOT NULL,
  `restricted` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `city` (`city`(333)),
  KEY `msIdentifier` (`msIdentifier`(333))
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=17588 ;

--
-- Dumping data for table `manuscript`
--

INSERT INTO `manuscript` (`id`, `city`, `archive`, `repository`, `msIdentifier`, `restricted`) VALUES
(1, 'Cambridge', 'ENAP', 'Corpus Christi College', '415', 0);

-- --------------------------------------------------------

--
-- Table structure for table `manuscriptPermissions`
--

CREATE TABLE IF NOT EXISTS `manuscriptPermissions` (
  `msID` int(11) NOT NULL,
  `uid` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `manuscriptPermissions`
--


-- --------------------------------------------------------

--
-- Table structure for table `metadata`
--

CREATE TABLE IF NOT EXISTS `metadata` (
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
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `metadata`
--

INSERT INTO `metadata` (`title`, `subject`, `language`, `author`, `date`, `location`, `description`, `subtitle`, `msIdentifier`, `msSettlement`, `msIdNumber`, `msRepository`, `msCollection`, `projectID`) VALUES
('Cambridge, Corpus Christi College 415 project', ' ', '', '', '', '', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 2411);

-- --------------------------------------------------------

--
-- Table structure for table `oldfimagepositions`
--

CREATE TABLE IF NOT EXISTS `oldfimagepositions` (
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
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=195072 ;

--
-- Dumping data for table `oldfimagepositions`
--


-- --------------------------------------------------------

--
-- Table structure for table `paragraphs`
--

CREATE TABLE IF NOT EXISTS `paragraphs` (
  `tract` varchar(512) NOT NULL,
  `sentences` int(12) NOT NULL,
  `words` int(12) NOT NULL,
  `characters` int(12) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `paragraphs`
--


-- --------------------------------------------------------

--
-- Table structure for table `partnerProject`
--

CREATE TABLE IF NOT EXISTS `partnerProject` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` int(11) NOT NULL,
  `projectID` int(11) NOT NULL,
  `name` varchar(512) NOT NULL,
  `description` text NOT NULL,
  `url` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=7 ;

--
-- Dumping data for table `partnerProject`
--


-- --------------------------------------------------------

--
-- Table structure for table `partnerproject`
--

CREATE TABLE IF NOT EXISTS `partnerproject` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` int(11) NOT NULL,
  `projectID` int(11) NOT NULL,
  `name` varchar(512) NOT NULL,
  `description` text NOT NULL,
  `url` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3 ;

--
-- Dumping data for table `partnerproject`
--


-- --------------------------------------------------------

--
-- Table structure for table `project`
--

CREATE TABLE IF NOT EXISTS `project` (
  `grp` int(11) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(512) NOT NULL,
  `schemaURL` text NOT NULL,
  `linebreakCharacterLimit` int(11) NOT NULL DEFAULT '7500',
  `linebreakSymbol` varchar(256) NOT NULL DEFAULT '-',
  `imageBounding` varchar(255) NOT NULL DEFAULT 'lines',
  `partner` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=2412 ;

--
-- Dumping data for table `project`
--

INSERT INTO `project` (`grp`, `id`, `name`, `schemaURL`, `linebreakCharacterLimit`, `linebreakSymbol`, `imageBounding`, `partner`) VALUES
(619, 2411, 'Cambridge, Corpus Christi College 415 project', '', 5000, '-', 'lines', 0);

-- --------------------------------------------------------

--
-- Table structure for table `projectButtons`
--

CREATE TABLE IF NOT EXISTS `projectButtons` (
  `project` int(11) NOT NULL,
  `position` int(11) NOT NULL,
  `text` varchar(256) NOT NULL,
  `param1` varchar(512) NOT NULL DEFAULT '',
  `param2` varchar(512) NOT NULL DEFAULT '',
  `param3` varchar(512) NOT NULL DEFAULT '',
  `param4` varchar(512) NOT NULL DEFAULT '',
  `param5` varchar(512) NOT NULL DEFAULT '',
  `description` text NOT NULL,
  `color` varchar(255) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `projectButtons`
--


-- --------------------------------------------------------

--
-- Table structure for table `projectFolios`
--

CREATE TABLE IF NOT EXISTS `projectFolios` (
  `position` int(11) NOT NULL,
  `project` int(11) NOT NULL,
  `folio` int(11) NOT NULL,
  KEY `proj` (`project`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `projectFolios`
--

INSERT INTO `projectFolios` (`position`, `project`, `folio`) VALUES
(1, 2411, 1);

-- --------------------------------------------------------

--
-- Table structure for table `projectHeader`
--

CREATE TABLE IF NOT EXISTS `projectHeader` (
  `projectID` int(11) NOT NULL,
  `header` text NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `projectHeader`
--


-- --------------------------------------------------------

--
-- Table structure for table `projectimagepositions`
--

CREATE TABLE IF NOT EXISTS `projectimagepositions` (
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
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=8758 ;

--
-- Dumping data for table `projectimagepositions`
--


-- --------------------------------------------------------

--
-- Table structure for table `projectLog`
--

CREATE TABLE IF NOT EXISTS `projectLog` (
  `projectID` int(11) NOT NULL,
  `uid` int(11) NOT NULL,
  `content` text CHARACTER SET latin1 NOT NULL,
  `creationDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `projectID` (`projectID`),
  KEY `creationDate` (`creationDate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Dumping data for table `projectLog`
--

INSERT INTO `projectLog` (`projectID`, `uid`, `content`, `creationDate`) VALUES
(2411, 1, 'Added manuscript Cambridge, Corpus Christi College 415', '2012-05-01 10:15:03');

-- --------------------------------------------------------

--
-- Table structure for table `projectlog`
--

CREATE TABLE IF NOT EXISTS `projectlog` (
  `projectID` int(11) NOT NULL,
  `uid` int(11) NOT NULL,
  `content` text CHARACTER SET latin1 NOT NULL,
  `creationDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Dumping data for table `projectlog`
--


-- --------------------------------------------------------

--
-- Table structure for table `projectlogBackup`
--

CREATE TABLE IF NOT EXISTS `projectlogBackup` (
  `projectID` int(11) NOT NULL,
  `uid` int(11) NOT NULL,
  `content` text CHARACTER SET latin1 NOT NULL,
  `creationDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Dumping data for table `projectlogBackup`
--


-- --------------------------------------------------------

--
-- Table structure for table `ProjectPermissions`
--

CREATE TABLE IF NOT EXISTS `ProjectPermissions` (
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
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ProjectPermissions`
--

INSERT INTO `ProjectPermissions` (`allow_OAC_read`, `allow_OAC_write`, `allow_export`, `allow_public_copy`, `allow_public_modify`, `allow_public_modify_annotation`, `allow_public_modify_buttons`, `allow_public_modify_line_parsing`, `allow_public_modify_metadata`, `allow_public_modify_notes`, `allow_public_read_transcription`, `projectID`) VALUES
(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2411);

-- --------------------------------------------------------

--
-- Table structure for table `projectPriorities`
--

CREATE TABLE IF NOT EXISTS `projectPriorities` (
  `uid` int(11) NOT NULL,
  `priority` int(11) NOT NULL,
  `projectID` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `projectPriorities`
--

INSERT INTO `projectPriorities` (`uid`, `priority`, `projectID`) VALUES
(1, 0, 2411);

-- --------------------------------------------------------

--
-- Table structure for table `sentences`
--

CREATE TABLE IF NOT EXISTS `sentences` (
  `sentence` text NOT NULL,
  `length` int(12) NOT NULL,
  `period` int(12) NOT NULL,
  `question` int(12) NOT NULL,
  `exclaimation` int(12) NOT NULL,
  `comma` int(12) NOT NULL,
  `semicolon` int(12) NOT NULL,
  `colon` int(12) NOT NULL,
  `tract` varchar(512) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `sentences`
--


-- --------------------------------------------------------

--
-- Table structure for table `sources`
--

CREATE TABLE IF NOT EXISTS `sources` (
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
  KEY `book` (`book`(333)),
  KEY `chapter` (`chapter`(333)),
  KEY `verse` (`verse`(333)),
  KEY `tract` (`tract`(333))
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=23461 ;

--
-- Dumping data for table `sources`
--


-- --------------------------------------------------------

--
-- Table structure for table `tagTracking`
--

CREATE TABLE IF NOT EXISTS `tagTracking` (
  `tag` varchar(256) COLLATE utf8_bin NOT NULL,
  `folio` int(11) NOT NULL,
  `line` int(11) NOT NULL,
  `projectID` int(11) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `folio` (`folio`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=16539 ;

--
-- Dumping data for table `tagTracking`
--


-- --------------------------------------------------------

--
-- Table structure for table `tasks`
--

CREATE TABLE IF NOT EXISTS `tasks` (
  `project` int(11) NOT NULL,
  `beginFolio` int(11) NOT NULL,
  `endFolio` int(11) NOT NULL,
  `UID` int(11) NOT NULL,
  `title` varchar(512) NOT NULL,
  `id` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `tasks`
--


-- --------------------------------------------------------

--
-- Table structure for table `tools`
--

CREATE TABLE IF NOT EXISTS `tools` (
  `tool` varchar(512) DEFAULT '',
  `uid` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `tools`
--


-- --------------------------------------------------------

--
-- Table structure for table `transcription`
--

CREATE TABLE IF NOT EXISTS `transcription` (
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
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=100169168 ;

--
-- Dumping data for table `transcription`
--


-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `Uname` varchar(255) CHARACTER SET latin1 NOT NULL,
  `UID` int(11) NOT NULL AUTO_INCREMENT,
  `pass` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `lname` varchar(512) CHARACTER SET latin1 NOT NULL DEFAULT 'new',
  `fname` varchar(512) CHARACTER SET latin1 NOT NULL DEFAULT 'new',
  `openID` text CHARACTER SET latin1 NOT NULL,
  `accepted` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `hasAccepted` int(11) DEFAULT '0',
  `lastActive` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`UID`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_bin AUTO_INCREMENT=265 ;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`Uname`, `UID`, `pass`, `lname`, `fname`, `openID`, `accepted`, `hasAccepted`, `lastActive`) VALUES
('admin', 1, '335ded56c9ca54f9fb7aa4cd61455a4bfa0af7c8', 'admin', 'admin', '', '0000-00-00 00:00:00', 0, '2012-05-01 10:21:33');

-- --------------------------------------------------------

--
-- Table structure for table `userTools`
--

CREATE TABLE IF NOT EXISTS `userTools` (
  `projectID` int(11) NOT NULL,
  `url` varchar(512) DEFAULT 'http://vulsearch.sourceforge.net/cgi-bin/vulsearch',
  `name` varchar(512) DEFAULT 'Latin Vulgate Search'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `userTools`
--

INSERT INTO `userTools` (`projectID`, `url`, `name`) VALUES
(2411, 'http://vulsearch.sourceforge.net/cgi-bin/vulsearch', 'Latin Vulgate'),
(2411, 'http://t-pen.org/hopper/morph.jsp', 'Latin Dictionary');

-- --------------------------------------------------------

--
-- Table structure for table `usertools`
--

CREATE TABLE IF NOT EXISTS `usertools` (
  `projectID` int(11) NOT NULL,
  `url` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `usertools`
--


-- --------------------------------------------------------

--
-- Table structure for table `words`
--

CREATE TABLE IF NOT EXISTS `words` (
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
  KEY `word` (`word`(333))
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=520015 ;

--
-- Dumping data for table `words`
--


-- --------------------------------------------------------

--
-- Table structure for table `xml`
--

CREATE TABLE IF NOT EXISTS `xml` (
  `work` varchar(512) NOT NULL,
  `text` mediumtext NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `xml`
--

CREATE TABLE IF NOT EXISTS `welcomemessage` (
  `msg` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
