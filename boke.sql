-- --------------------------------------------------------
-- 主机:                           127.0.0.1
-- 服务器版本:                        8.4.0 - MySQL Community Server - GPL
-- 服务器操作系统:                      Win64
-- HeidiSQL 版本:                  12.1.0.6537
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- 导出 boke 的数据库结构
CREATE DATABASE IF NOT EXISTS `boke` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `boke`;

-- 导出  表 boke.articles 结构
CREATE TABLE IF NOT EXISTS `articles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(150) NOT NULL,
  `summary` varchar(300) DEFAULT NULL,
  `content` text,
  `cover_image_url` varchar(255) DEFAULT NULL,
  `author_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_published` tinyint(1) DEFAULT '0',
  `view_count` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_articles_author` (`author_id`),
  KEY `idx_articles_created_at` (`created_at`),
  KEY `idx_articles_title` (`title`),
  CONSTRAINT `fk_articles_author` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 正在导出表  boke.articles 的数据：~16 rows (大约)
INSERT INTO `articles` (`id`, `title`, `summary`, `content`, `cover_image_url`, `author_id`, `created_at`, `updated_at`, `is_published`, `view_count`) VALUES
	(1, '第一篇：Spring Boot 博客系统开篇', '项目简介与技术栈说明', '这是开篇内容，介绍了本博客系统的结构、技术栈与规划。127', '/uploads/images/2025-11-14/d2c006ee44e8474b8290f113e7a57497.jpg', 1, '2025-11-11 21:35:31', '2025-12-19 04:01:29', 0, 17),
	(2, '第二篇：JPA 与 Hibernate 入门', '实体、Repository、事务的基本用法', '本文讲解如何使用 Spring Data JPA 进行常见的 CRUD 操作与事务管理。', '/uploads/images/2025-11-14/5129672a4cb242d9b217e4f8251c9858.jpg', 1, '2025-11-11 21:35:31', '2025-12-19 18:04:10', 1, 125),
	(3, '我的新文章', '这是一篇基于关键词【】生成的文章摘要，供快速发布。', '<p>北恒 引言：围绕主题“我的新文章”展开，结合关键词“”。</p><p><br></p><p>## 小节 1</p><p>在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。</p><p><br></p><p>## 小节 2</p><p>在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。</p><p><br></p><p>## 小节 3</p><p>在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。</p><p><br></p><p>## 小节 4</p><p>在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。</p><p><br></p><p>## 小节 5</p><p>在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。</p><p><br></p><p>结语：总结全文观点，并提出可落地的行动建议。</p>', '/uploads/images/2025-11-14/48b7ea63c0374d03922600a7260b1d27.jpg', 1, '2025-11-14 05:33:40', '2025-12-19 11:53:13', 1, 81),
	(4, '我的新文章', '这是一篇基于关键词【】生成的文章摘要，供快速发布。', '# 我的新文章\n\n引言：围绕主题“我的新文章”展开，结合关键词“”。\n\n## 小节 1\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 2\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 3\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 4\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 5\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n结语：总结全文观点，并提出可落地的行动建议。\n', '/uploads/images/2025-11-14/eca110e671ae4a849589ddef889d9f79.jpg', 1, '2025-11-14 05:57:09', '2026-03-03 08:06:31', 1, 26),
	(5, '我的新文章', '这是一篇基于关键词【】生成的文章摘要，供快速发布。', '# 我的新文章\n\n引言：围绕主题“我的新文章”展开，结合关键词“”。\n\n## 小节 1\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 2\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 3\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 4\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 5\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n结语：总结全文观点，并提出可落地的行动建议。\n', '', 1, '2025-11-15 01:56:18', '2025-12-19 07:05:48', 1, 3),
	(7, '我的新文章', '这是一篇基于关键词【】生成的文章摘要，供快速发布。', '# 我的新文章\n\n引言：围绕主题“我的新文章”展开，结合关键词“”。\n\n## 小节 1\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 2\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 3\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 4\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 5\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n结语：总结全文观点，并提出可落地的行动建议。\n', '', 1, '2025-11-15 01:56:34', '2025-11-25 16:44:13', 1, 7),
	(8, '我的新文章', '这是一篇基于关键词【】生成的文章摘要，供快速发布。', '# 我的新文章\n\n引言：围绕主题“我的新文章”展开，结合关键词“”。\n\n## 小节 1\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 2\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 3\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 4\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 5\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n结语：总结全文观点，并提出可落地的行动建议。\n', '', 1, '2025-11-15 01:56:39', '2025-11-15 09:56:39', 0, 1),
	(11, '我的新文章', '这是一篇基于关键词【】生成的文章摘要，供快速发布。', '# 我的新文章\n\n引言：围绕主题“我的新文章”展开，结合关键词“”。\n\n## 小节 1\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 2\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 3\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 4\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 5\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n结语：总结全文观点，并提出可落地的行动建议。\n', '', 1, '2025-11-15 01:56:57', '2025-12-19 07:05:45', 1, 1),
	(12, '我的新文章', '这是一篇基于关键词【】生成的文章摘要，供快速发布。', '# 我的新文章\n\n引言：围绕主题“我的新文章”展开，结合关键词“”。\n\n## 小节 1\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 2\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 3\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 4\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 5\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n结语：总结全文观点，并提出可落地的行动建议。', '', 1, '2025-11-15 03:45:17', '2025-11-15 03:45:17', 0, 0),
	(13, '北恒是一个strong man', '这是一篇基于关键词【】生成的文章摘要，供快速发布。', '# 北恒是一个strong man\n\n引言：围绕主题“北恒是一个strong man”展开，结合关键词“”。\n\n## 小节 1\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 2\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 3\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 4\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 5\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n结语：总结全文观点，并提出可落地的行动建议。', '', 1, '2025-11-15 11:12:10', '2025-11-22 13:40:26', 1, 6),
	(14, '我的新文章', '这是一篇基于关键词【】生成的文章摘要，供快速发布。', '# 我的新文章\n\n引言：围绕主题“我的新文章”展开，结合关键词“”。\n\n## 小节 1\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 2\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 3\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 4\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 5\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n结语：总结全文观点，并提出可落地的行动建议。', '', 1, '2025-11-15 14:38:44', '2025-11-22 13:25:09', 1, 2),
	(15, '我的新文章', '这是一篇基于关键词【】生成的文章摘要，供快速发布。', '###我的新文章\n\n引言：围绕主题“我的新文章”展开，结合关键词“”。\n\n## 小节 1\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 2\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 3\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 4\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n## 小节 5\n在这一小节中，我们详细阐述与主题相关的要点、思考与案例。通过层层递进的叙述方式，帮助读者快速把握核心。\n\n结语：总结全文观点，并提出可落地的行动建议。', '/uploads/images/2025-11-15/918d72bc5d6a4d849884499b110b8f39.jpg', 1, '2025-11-15 14:39:44', '2025-11-22 12:58:44', 1, 4),
	(16, 'wangeditor组件试用', 'wangeditor组件库', '<h2>这组件库世人啊</h2><p>太好看了吧</p><p><br></p>', '/uploads/images/2025-11-15/a6bd15730bc7488e816278a24cbdeed1.jpg', 1, '2025-11-15 14:43:06', '2025-11-15 22:46:20', 1, 2),
	(17, '网安第一章 网络信息安全概述', '考点：1网安概念\n3网安基本属性****\n4网安基本技术需求***\n5网安管理现状\n6网安管理流程***\n7常见安全模型\n8主要网安原则\n9信息安全标准化组织***\n10常见信息安全标志、法律与规范\n11我国信息安全等级保护体系****', '<h4><strong>1网安概念</strong></h4><p><strong>狭义上</strong>:网络信息系统的各组成要素符合安全属性的要求，即<strong>机密性、完整性、可用性、抗抵赖性、可控性</strong></p><p>广义上:涉及国家安全、城市安全、经济安全、社会安全等在内的“大安全”，网络信息安全通常简称为网络安全</p><p>网络安全:是指通过采取必要措施，防范对网络的攻击、侵入、干扰和非法使用以及意外事故，使网络处于稳定可靠运行的状态，以及保厚网给数据的完整性、保密性、可用性的能力——《中华人民共和国网络安全法》</p><h4>2信息安全发展史</h4><p>20世纪初-通信保密阶段</p><p>20世纪90年代后-信息安全阶段</p><p>21世纪至今-信息保障阶段</p><h4>3:网络信息安全基本属性(五要素)</h4><p>机密性(Confidentality) :网络信息不泄露给非授权的用户、实体或程序，能够防止非授权者获取信息。(授权才可用，口令密码)</p><p>完整性(Integrity) :网络信息或系统未经授权不能进行更改的特性。(不被篡改)</p><p>可用性(Availability) ∶合法许可的用户能够及时获取网络信息或服务的特性。(随时可用)</p><p>抗抵赖性:防止网络信息系统相关用户事后不认账（不可否认性可控性:对信息系统实施安全监控。</p><p>助记提醒:机密性、完整性、可用性，前三个属性统称网络系统安全C.I.A</p><p><br></p>', '/uploads/images/2025-11-19/a8a82d58444d449aa3406ecc37622155.jpg', 1, '2025-11-19 08:59:34', '2025-11-25 16:53:30', 1, 10),
	(18, 'black wukong', 'ccc', '<p><br></p>', '/uploads/images/2026-03-03/9fb6d7c801524246a84048a3330637e4.png', 1, '2026-03-03 08:06:05', '2026-03-03 16:07:32', 1, 1),
	(19, '爱弥丝', '测试图片上传的功能', '<p><img src="/uploads/images/2026-03-08/88c5ac6b9c9948a798201fc4a7981e14.jpg" alt="/uploads/images/2026-03-08/88c5ac6b9c9948a798201fc4a7981e14.jpg" data-href="/uploads/images/2026-03-08/88c5ac6b9c9948a798201fc4a7981e14.jpg" width="" height="" style="width: 506.00px;height: 284.63px;"/></p><p>爱弥丝我爱你</p>', '/uploads/images/2026-03-08/b5fccf3a7e614804853ccd0ebef03a58.jpg', 1, '2026-03-08 02:58:37', '2026-03-08 10:58:41', 1, 1);

-- 导出  表 boke.article_likes 结构
CREATE TABLE IF NOT EXISTS `article_likes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `article_id` bigint NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_like_user_article` (`user_id`,`article_id`),
  KEY `fk_like_article` (`article_id`),
  CONSTRAINT `fk_like_article` FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 正在导出表  boke.article_likes 的数据：~9 rows (大约)
INSERT INTO `article_likes` (`id`, `user_id`, `article_id`, `created_at`) VALUES
	(1, 2, 1, '2025-11-11 13:35:31'),
	(2, 2, 2, '2025-11-11 13:35:31'),
	(4, 1, 1, '2025-11-14 05:01:51'),
	(5, 1, 4, '2025-11-14 05:02:09'),
	(9, 3, 2, '2025-11-19 02:02:51'),
	(10, 1, 3, '2025-11-21 21:06:58'),
	(14, 1, 2, '2025-11-25 00:45:55'),
	(16, 9, 2, '2025-12-19 01:57:15'),
	(17, 1, 18, '2026-03-03 00:06:15');

-- 导出  表 boke.comments 结构
CREATE TABLE IF NOT EXISTS `comments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `article_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `content` varchar(1000) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_comments_user` (`user_id`),
  KEY `idx_comments_article_created` (`article_id`,`created_at`),
  KEY `idx_comments_created_at` (`created_at`),
  CONSTRAINT `fk_comments_article` FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_comments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 正在导出表  boke.comments 的数据：~11 rows (大约)
INSERT INTO `comments` (`id`, `article_id`, `user_id`, `content`, `created_at`) VALUES
	(2, 2, 2, '讲得很清晰，感谢分享！', '2025-11-11 21:35:31'),
	(3, 1, 1, '123', '2025-11-14 04:22:06'),
	(4, 1, 1, 'tql', '2025-11-14 04:28:22'),
	(5, 1, 2, '666', '2025-11-14 04:28:51'),
	(6, 1, 3, '北恒太帅了', '2025-11-14 04:40:49'),
	(7, 4, 1, '芙芙太好看了', '2025-11-14 09:17:47'),
	(9, 1, 1, '123', '2025-11-15 14:58:06'),
	(10, 2, 1, '太好了', '2025-11-22 05:07:10'),
	(11, 14, 1, 'lll', '2025-11-22 05:25:18'),
	(14, 2, 1, '66666', '2025-11-25 08:47:41'),
	(15, 2, 9, '666', '2025-12-19 09:56:53');

-- 导出  表 boke.conversations 结构
CREATE TABLE IF NOT EXISTS `conversations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user1_id` bigint NOT NULL,
  `user2_id` bigint NOT NULL,
  `last_message_id` bigint DEFAULT NULL,
  `last_message_content` text COLLATE utf8mb4_general_ci,
  `last_message_at` timestamp NULL DEFAULT NULL,
  `unread_count_user1` int DEFAULT '0',
  `unread_count_user2` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_pair` (`user1_id`,`user2_id`),
  KEY `idx_user1` (`user1_id`,`last_message_at`),
  KEY `idx_user2` (`user2_id`,`last_message_at`),
  KEY `FKd9sy3cjuppb511olt5pv3ixe` (`last_message_id`),
  CONSTRAINT `FK8wv0rmd8jb3cqcbyng15ubrmk` FOREIGN KEY (`user1_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKd9sy3cjuppb511olt5pv3ixe` FOREIGN KEY (`last_message_id`) REFERENCES `messages` (`id`),
  CONSTRAINT `FKe7w0k1xem21pp85wxh5moodnk` FOREIGN KEY (`user2_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 正在导出表  boke.conversations 的数据：~12 rows (大约)
INSERT INTO `conversations` (`id`, `user1_id`, `user2_id`, `last_message_id`, `last_message_content`, `last_message_at`, `unread_count_user1`, `unread_count_user2`, `created_at`, `updated_at`) VALUES
	(1, 1, 6, 15, '555', '2025-11-21 21:58:00', 0, 4, '2025-11-15 08:11:43', '2025-11-21 21:58:00'),
	(2, 1, 3, 11, '卡提西亚最好看了', '2025-11-21 21:02:05', 0, 1, '2025-11-15 08:29:37', '2025-11-21 21:02:05'),
	(3, 1, 4, 10, 'Helloween', '2025-11-21 21:01:48', 0, 3, '2025-11-15 08:46:29', '2025-11-21 21:01:48'),
	(4, 1, 5, 16, 'ni好', '2025-11-25 00:54:24', 0, 1, '2025-11-25 00:54:24', '2025-11-25 00:54:24'),
	(5, 1, 2, 17, 'hjh', '2025-11-25 00:54:52', 0, 1, '2025-11-25 00:54:52', '2025-11-25 00:54:52'),
	(6, 1, 9, 32, 'import { defineConfig } from \'vite\'\nimport vue from \'@vitejs/plugin-vue\'\nimport { fileURLToPath, URL } from \'node:url\'\nimport path from \'path\'\n\nexport default defineConfig({\n  plugins: [vue()],\n  server: {\n    port: 5173,\n    // 允许访问项目根目录外的文件\n    fs: {\n      allow: [\'..\', \'../..\', path.resolve(__dirname, \'../../uploads\')]\n    },\n    proxy: {\n      // 开发时代理 API 请求到后端\n      \'/api\': {\n        target: \'http://localhost:8080\',\n        changeOrigin: true\n      },\n      // 代理上传的文件到后端\n      \'/uploads\': {\n        target: \'http://localhost:8080\',\n        changeOrigin: true\n      }\n    }\n  },\n  resolve: {\n    alias: {\n      \'@\': fileURLToPath(new URL(\'./src\', import.meta.url))\n    }\n  },\n  build: {\n    // 构建优化配置\n    target: \'es2015\',\n    minify: \'terser\',\n    cssCodeSplit: true,\n    sourcemap: false,\n    // 代码分割配置\n    rollupOptions: {\n      output: {\n        manualChunks: {\n          // 将 Vue 相关库单独打包\n          \'vue-vendor\': [\'vue\', \'vue-router\', \'pinia\'],\n          // 将 markdown 相关库单独打包\n          \'markdown\': [\'marked\', \'dompurify\'],\n          // 将工具库单独打包\n          \'utils\': [\'axios\', \'lodash-es\']\n        },\n        chunkFileNames: \'assets/js/[name]-[hash].js\',\n        entryFileNames: \'assets/js/[name]-[hash].js\',\n        assetFileNames: \'assets/[ext]/[name]-[hash].[ext]\'\n      }\n    },\n    // 压缩配置\n    terserOptions: {\n      compress: {\n        drop_console: true,\n        drop_debugger: true\n      }\n    },\n    // 分块策略\n    chunkSizeWarningLimit: 1000\n  },\n  // 预览服务器配置\n  preview: {\n    port: 4173\n  }\n})', '2026-03-05 04:44:40', 0, 0, '2025-11-25 00:55:14', '2026-03-05 04:51:47'),
	(7, 1, 7, 19, '牛马', '2025-11-25 01:07:33', 0, 1, '2025-11-25 01:07:33', '2025-11-25 01:07:33'),
	(8, 1, 8, 20, '恒999', '2025-11-25 01:08:10', 0, 1, '2025-11-25 01:08:10', '2025-11-25 01:08:10'),
	(9, 1, 12, 21, '我喜欢你', '2025-11-25 01:10:20', 0, 0, '2025-11-25 01:10:20', '2025-11-25 01:37:12'),
	(10, 1, 10, 22, '我喜欢你', '2025-11-25 01:10:30', 0, 1, '2025-11-25 01:10:30', '2025-11-25 01:10:30'),
	(11, 1, 11, 23, '我喜欢你', '2025-11-25 01:11:37', 0, 1, '2025-11-25 01:11:37', '2025-11-25 01:11:37'),
	(12, 1, 13, 24, '我也喜欢你', '2025-11-25 01:11:59', 0, 0, '2025-11-25 01:11:59', '2025-11-25 01:34:37');

-- 导出  表 boke.favorites 结构
CREATE TABLE IF NOT EXISTS `favorites` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `article_id` bigint NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fav_user_article` (`user_id`,`article_id`),
  KEY `fk_fav_article` (`article_id`),
  CONSTRAINT `fk_fav_article` FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_fav_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 正在导出表  boke.favorites 的数据：~10 rows (大约)
INSERT INTO `favorites` (`id`, `user_id`, `article_id`, `created_at`) VALUES
	(2, 1, 1, '2025-11-14 05:01:50'),
	(4, 1, 4, '2025-11-14 05:02:10'),
	(5, 1, 5, '2025-11-14 19:12:20'),
	(7, 3, 3, '2025-11-14 19:24:33'),
	(8, 3, 4, '2025-11-14 19:25:11'),
	(10, 3, 2, '2025-11-14 23:20:27'),
	(11, 1, 3, '2025-11-21 21:06:59'),
	(16, 1, 2, '2025-11-25 00:45:56'),
	(17, 9, 2, '2025-12-19 01:57:14'),
	(19, 1, 18, '2026-03-03 00:06:18');

-- 导出  表 boke.follows 结构
CREATE TABLE IF NOT EXISTS `follows` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `follower_id` bigint NOT NULL COMMENT '关注者ID',
  `following_id` bigint NOT NULL COMMENT '被关注者ID',
  `created_at` timestamp NULL DEFAULT (now()),
  `updated_at` timestamp NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_following` (`follower_id`,`following_id`),
  UNIQUE KEY `UK4faelgsm2rxl2jf3iyjy981ro` (`follower_id`,`following_id`),
  KEY `idx_follower_id` (`follower_id`),
  KEY `idx_following_id` (`following_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `FKonkdkae2ngtx70jqhsh7ol6uq` FOREIGN KEY (`following_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKqnkw0cwwh6572nyhvdjqlr163` FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 正在导出表  boke.follows 的数据：~9 rows (大约)
INSERT INTO `follows` (`id`, `follower_id`, `following_id`, `created_at`, `updated_at`) VALUES
	(1, 1, 2, '2025-11-15 11:16:28', '2025-11-15 11:16:35'),
	(11, 6, 1, '2025-11-15 07:35:37', '2025-11-15 07:35:37'),
	(12, 6, 4, '2025-11-15 07:35:48', '2025-11-15 07:35:48'),
	(13, 3, 1, '2025-11-15 08:29:17', '2025-11-15 08:29:17'),
	(14, 1, 8, '2025-11-16 01:14:33', '2025-11-16 01:14:33'),
	(16, 1, 3, '2025-11-19 03:27:46', '2025-11-19 03:27:46'),
	(22, 1, 6, '2025-11-21 21:11:36', '2025-11-21 21:11:36'),
	(23, 1, 4, '2025-11-21 21:11:43', '2025-11-21 21:11:43'),
	(25, 1, 9, '2025-12-18 23:06:31', '2025-12-18 23:06:31');

-- 导出  表 boke.messages 结构
CREATE TABLE IF NOT EXISTS `messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sender_id` bigint NOT NULL,
  `receiver_id` bigint NOT NULL,
  `content` text COLLATE utf8mb4_general_ci NOT NULL,
  `message_type` enum('TEXT','IMAGE','FILE') COLLATE utf8mb4_general_ci DEFAULT 'TEXT',
  `file_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `file_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `is_read` tinyint(1) DEFAULT '0',
  `read_at` timestamp NULL DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sender_receiver` (`sender_id`,`receiver_id`),
  KEY `idx_receiver_sender` (`receiver_id`,`sender_id`),
  KEY `idx_conversation` (`sender_id`,`receiver_id`,`created_at`),
  KEY `idx_unread` (`receiver_id`,`is_read`,`created_at`),
  CONSTRAINT `FK4ui4nnwntodh6wjvck53dbk9m` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKt05r0b6n0iis8u7dfna4xdh73` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 正在导出表  boke.messages 的数据：~32 rows (大约)
INSERT INTO `messages` (`id`, `sender_id`, `receiver_id`, `content`, `message_type`, `file_url`, `file_name`, `file_size`, `is_read`, `read_at`, `is_deleted`, `created_at`, `updated_at`) VALUES
	(1, 6, 1, '你好', 'TEXT', NULL, NULL, NULL, 1, '2025-11-15 08:12:15', 0, '2025-11-15 08:11:42', '2025-11-15 16:12:15'),
	(2, 1, 6, 'ohhhh!!!', 'TEXT', NULL, NULL, NULL, 1, '2025-11-15 08:12:28', 0, '2025-11-15 08:12:25', '2025-11-15 16:12:28'),
	(3, 6, 1, 'np', 'TEXT', NULL, NULL, NULL, 1, '2025-11-15 08:19:34', 0, '2025-11-15 08:19:30', '2025-11-15 16:19:34'),
	(4, 6, 1, '666', 'TEXT', NULL, NULL, NULL, 1, '2025-11-15 08:28:28', 0, '2025-11-15 08:19:44', '2025-11-15 16:28:27'),
	(5, 6, 1, 'what', 'TEXT', NULL, NULL, NULL, 1, '2025-11-15 08:28:28', 1, '2025-11-15 08:28:11', '2025-11-15 16:28:27'),
	(6, 3, 1, '北恒，你是我的偶像啊', 'TEXT', NULL, NULL, NULL, 1, '2025-11-15 08:29:42', 0, '2025-11-15 08:29:37', '2025-11-15 16:29:42'),
	(7, 1, 3, '__camellya_wuthering_waves_drawn_by_qi_xie__sample-55c0220db14ca5a7bd8e4282b100621e.jpg', 'FILE', '/uploads/messages/2025-11-16/ef04d9d0a1804d7eb66750052348f83c.jpg', '__camellya_wuthering_waves_drawn_by_qi_xie__sample-55c0220db14ca5a7bd8e4282b100621e.jpg', 69478, 1, '2025-11-15 08:37:48', 0, '2025-11-15 08:37:41', '2025-11-15 16:37:48'),
	(8, 1, 4, '无敌了', 'TEXT', NULL, NULL, NULL, 0, NULL, 1, '2025-11-15 08:46:29', NULL),
	(9, 1, 4, '__jinhsi_and_jue_wuthering_waves_drawn_by_baofandxy__sample-f38b26711f035f3488c4e98166c67a14.jpg', 'FILE', '/uploads/messages/2025-11-16/04553a5349fa49debc90adee572ff019.jpg', '__jinhsi_and_jue_wuthering_waves_drawn_by_baofandxy__sample-f38b26711f035f3488c4e98166c67a14.jpg', 80651, 0, NULL, 0, '2025-11-15 08:47:51', NULL),
	(10, 1, 4, 'Helloween', 'TEXT', NULL, NULL, NULL, 0, NULL, 0, '2025-11-21 21:01:48', '2025-11-21 21:01:48'),
	(11, 1, 3, '卡提西亚最好看了', 'TEXT', NULL, NULL, NULL, 0, NULL, 0, '2025-11-21 21:02:05', '2025-11-21 21:02:05'),
	(12, 1, 6, '127', 'TEXT', NULL, NULL, NULL, 0, NULL, 0, '2025-11-21 21:09:58', '2025-11-21 21:09:58'),
	(13, 1, 6, '127.0.0.1', 'TEXT', NULL, NULL, NULL, 0, NULL, 0, '2025-11-21 21:10:03', '2025-11-21 21:10:03'),
	(14, 1, 6, 'test', 'TEXT', NULL, NULL, NULL, 0, NULL, 0, '2025-11-21 21:10:10', '2025-11-21 21:10:10'),
	(15, 1, 6, '555', 'TEXT', NULL, NULL, NULL, 0, NULL, 0, '2025-11-21 21:58:00', '2025-11-21 21:58:00'),
	(16, 1, 5, 'ni好', 'TEXT', NULL, NULL, NULL, 0, NULL, 0, '2025-11-25 00:54:24', '2025-11-25 00:54:24'),
	(17, 1, 2, 'hjh', 'TEXT', NULL, NULL, NULL, 0, NULL, 0, '2025-11-25 00:54:52', '2025-11-25 00:54:52'),
	(18, 1, 9, '127', 'TEXT', NULL, NULL, NULL, 1, '2025-12-18 19:57:43', 0, '2025-11-25 00:55:14', '2025-12-19 03:57:42'),
	(19, 1, 7, '牛马', 'TEXT', NULL, NULL, NULL, 0, NULL, 0, '2025-11-25 01:07:33', '2025-11-25 01:07:33'),
	(20, 1, 8, '恒999', 'TEXT', NULL, NULL, NULL, 0, NULL, 0, '2025-11-25 01:08:10', '2025-11-25 01:08:10'),
	(21, 1, 12, '我喜欢你', 'TEXT', NULL, NULL, NULL, 1, '2025-11-25 01:37:12', 0, '2025-11-25 01:10:20', '2025-11-25 09:37:12'),
	(22, 1, 10, '我喜欢你', 'TEXT', NULL, NULL, NULL, 0, NULL, 0, '2025-11-25 01:10:30', '2025-11-25 01:10:30'),
	(23, 1, 11, '我喜欢你', 'TEXT', NULL, NULL, NULL, 0, NULL, 0, '2025-11-25 01:11:37', '2025-11-25 01:11:37'),
	(24, 1, 13, '我也喜欢你', 'TEXT', NULL, NULL, NULL, 1, '2025-11-25 01:34:37', 0, '2025-11-25 01:11:59', '2025-11-25 09:34:36'),
	(25, 9, 1, 'www', 'TEXT', NULL, NULL, NULL, 1, '2025-12-18 19:59:10', 0, '2025-12-18 19:57:46', '2025-12-19 03:59:10'),
	(26, 9, 1, '99', 'TEXT', NULL, NULL, NULL, 1, '2025-12-18 22:48:41', 0, '2025-12-18 22:48:35', '2025-12-19 06:48:40'),
	(27, 9, 1, '66', 'TEXT', NULL, NULL, NULL, 1, '2025-12-19 02:04:02', 0, '2025-12-19 01:57:30', '2025-12-19 10:04:02'),
	(28, 1, 9, '死不可怕', 'TEXT', NULL, NULL, NULL, 1, '2026-03-05 04:51:47', 0, '2026-03-05 04:43:33', '2026-03-05 12:51:47'),
	(29, 1, 9, '死是凉爽的夏夜', 'TEXT', NULL, NULL, NULL, 1, '2026-03-05 04:51:47', 0, '2026-03-05 04:43:50', '2026-03-05 12:51:47'),
	(30, 1, 9, '可供人无尽的安眠', 'TEXT', NULL, NULL, NULL, 1, '2026-03-05 04:51:47', 0, '2026-03-05 04:43:56', '2026-03-05 12:51:47'),
	(31, 1, 9, '不可能绝对不可能', 'TEXT', NULL, NULL, NULL, 1, '2026-03-05 04:51:47', 0, '2026-03-05 04:44:12', '2026-03-05 12:51:47'),
	(32, 1, 9, 'import { defineConfig } from \'vite\'\nimport vue from \'@vitejs/plugin-vue\'\nimport { fileURLToPath, URL } from \'node:url\'\nimport path from \'path\'\n\nexport default defineConfig({\n  plugins: [vue()],\n  server: {\n    port: 5173,\n    // 允许访问项目根目录外的文件\n    fs: {\n      allow: [\'..\', \'../..\', path.resolve(__dirname, \'../../uploads\')]\n    },\n    proxy: {\n      // 开发时代理 API 请求到后端\n      \'/api\': {\n        target: \'http://localhost:8080\',\n        changeOrigin: true\n      },\n      // 代理上传的文件到后端\n      \'/uploads\': {\n        target: \'http://localhost:8080\',\n        changeOrigin: true\n      }\n    }\n  },\n  resolve: {\n    alias: {\n      \'@\': fileURLToPath(new URL(\'./src\', import.meta.url))\n    }\n  },\n  build: {\n    // 构建优化配置\n    target: \'es2015\',\n    minify: \'terser\',\n    cssCodeSplit: true,\n    sourcemap: false,\n    // 代码分割配置\n    rollupOptions: {\n      output: {\n        manualChunks: {\n          // 将 Vue 相关库单独打包\n          \'vue-vendor\': [\'vue\', \'vue-router\', \'pinia\'],\n          // 将 markdown 相关库单独打包\n          \'markdown\': [\'marked\', \'dompurify\'],\n          // 将工具库单独打包\n          \'utils\': [\'axios\', \'lodash-es\']\n        },\n        chunkFileNames: \'assets/js/[name]-[hash].js\',\n        entryFileNames: \'assets/js/[name]-[hash].js\',\n        assetFileNames: \'assets/[ext]/[name]-[hash].[ext]\'\n      }\n    },\n    // 压缩配置\n    terserOptions: {\n      compress: {\n        drop_console: true,\n        drop_debugger: true\n      }\n    },\n    // 分块策略\n    chunkSizeWarningLimit: 1000\n  },\n  // 预览服务器配置\n  preview: {\n    port: 4173\n  }\n})', 'TEXT', NULL, NULL, NULL, 1, '2026-03-05 04:51:47', 0, '2026-03-05 04:44:40', '2026-03-05 12:51:47');

-- 导出  表 boke.users 结构
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` varchar(20) NOT NULL,
  `display_name` varchar(100) DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `signature` varchar(200) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `home_background_url` varchar(500) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 正在导出表  boke.users 的数据：~13 rows (大约)
INSERT INTO `users` (`id`, `username`, `password_hash`, `role`, `display_name`, `avatar_url`, `signature`, `created_at`, `home_background_url`, `email`) VALUES
	(1, 'admin', '$2a$10$UEWh8qTE9PGJZwBBolediunalruC9HZZ.NwVHdOJ4GL932Aaia3aa', 'BLOGGER', '北恒', '/uploads/images/2025-11-14/ce796a9943614a4da46b6fdf327eb120.jpg', '北恒的北是北方的北', '2025-11-11 21:35:31', '/uploads/images/2026-03-03/118adfa858024caa8d1b0ed3f071cec1.jpg', '934705339@qq.com'),
	(2, 'user1', '$2a$10$VjUu1gfiddR1/1ah41y/fe8GzCjUVizx.UdztN6fobTdQm7gu6noS', 'VIEWER', '读者一号', '/uploads/images/default/touxiang.jpg', '我帅的要命', '2025-11-11 21:35:31', NULL, ''),
	(3, 'zjr', '$2a$10$Sx14izfCDxjSsvAhG23p8eNaDwcNnXauBjzhfcnGfoW3JOcg1na2e', 'VIEWER', '景睿', '/uploads/images/default/touxiang.jpg', 'jingrui', '2025-11-14 04:32:08', '/uploads/images/2025-11-15/a3271f2271a947f7aa35a2a8dc9654ea.jpg', ''),
	(4, 'bh', '$2a$10$yRnyB.xp8xhYuY5vDDzFJu9xQJ9XjATfrEkdlBnhm2mCOWeP/77ya', 'BLOGGER', '北恒127', '/uploads/images/default/touxiang.jpg', NULL, '2025-11-14 06:24:40', NULL, ''),
	(5, 'sjk', '$2a$10$eUZgFo3iz.HUtuAfHeKM3.Nrg9vv0B.pOGDLUR9Va/iOmomEvQ1fC', 'VIEWER', '宋菌葵', '/uploads/images/default/touxiang.jpg', NULL, '2025-11-14 08:36:36', NULL, ''),
	(6, 'jr', '$2a$10$9po/76NnHLVSkyx4vJ0wauDw4R3s3tfvoTb8TA7mtlr59YOJE0hKG', 'BLOGGER', '赵景睿', '/uploads/images/2025-11-15/c064da4b46f54c75819c73c3883f3ed7.jpg', '深入了', '2025-11-14 11:13:37', '/uploads/images/2025-11-15/07ea80ab9157405f89ac60615e8a27cf.jpg', ''),
	(7, 'l78', '$2a$10$SfTIznux/7HZhArW5XERpun40AsV1BmNB0FSau2UaWh.zr9rgT7rO', 'BLOGGER', '崩坏', '/uploads/images/default/touxiang.jpg', '', '2025-11-16 07:55:57', NULL, ''),
	(8, 'lyh', '$2a$10$nDwhRqGDYk2J881lhXHsd.10TwnpTiI4mePL2xwDeHeniW4AYdalu', 'BLOGGER', 'LYH', '/uploads/images/default/touxiang.jpg', '', '2025-11-16 09:14:26', NULL, ''),
	(9, 'bhy', '$2a$10$PDDZX.wrBouM5EnZ8vaXWOffnYtmnHT/xMrZnSkJ1mzkmew5PnDdC', 'VIEWER', '北恒吖', '/uploads/images/2025-12-19/bacb26b0935f4378ba9f4908b7f6e589.jpg', '飞龙在天', '2025-11-16 10:11:44', '/uploads/images/2026-03-05/8fc58f8f0bf84937b8c962dfddce5a96.jpg', 'ruia36791@163.com'),
	(10, 'ktxy', '$2a$10$54WP7I5mEYOtkeMUMmWsIORnVz3uB.DoLEtvysefauwGtTP/updVq', 'VIEWER', '卡提西亚', '/uploads/images/2025-11-25/7f5669e17c2543778ef16f2c90746212.jpg', '', '2025-11-25 09:09:05', NULL, 'ruia36792@163.com'),
	(11, 'qianxiao', '$2a$10$N2K8wCTFtYG7yOn9XGl9pO5CdnV3oH8SzmhEHOdS7xvSZoUnPSu/y', 'VIEWER', '千咲', '/uploads/images/default/touxiang.jpg', '', '2025-11-25 09:09:28', NULL, 'ruia36793@163.com'),
	(12, 'dashachun', '$2a$10$edERQ1mWJyJd1/FpO81YP.O8W6QJ0q13R.zJRge7N.lc2ts79Gy8C', 'VIEWER', '椿', '/uploads/images/2025-11-25/d18edac78bdf4c76bc24a6a550ee2e6f.jpg', '', '2025-11-25 09:09:46', NULL, 'ruia36794@163.com'),
	(13, 'shoumaker', '$2a$10$VDtWpb.D9y0ZuLckCo0.wuYIymSW/EiSpU1aSuQyLe6gCoPBpnoK6', 'VIEWER', '守岸人', '/uploads/images/2025-11-25/763b3eb0448c489e8814804a9bf749a5.jpg', '', '2025-11-25 09:10:08', NULL, 'ruia36795@163.com');

-- 导出  表 boke.verification_codes 结构
CREATE TABLE IF NOT EXISTS `verification_codes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(10) COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `is_used` bit(1) NOT NULL,
  `type` enum('REGISTER','RESET_PASSWORD') COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 正在导出表  boke.verification_codes 的数据：~13 rows (大约)
INSERT INTO `verification_codes` (`id`, `code`, `created_at`, `email`, `expires_at`, `is_used`, `type`) VALUES
	(2, '338516', '2025-11-16 10:10:57.424742', 'ruia36791@163.com', '2025-11-16 10:20:57.416745', b'1', 'REGISTER'),
	(3, '780610', '2025-11-16 10:13:12.542554', 'ruia36791@163.com', '2025-11-16 10:23:12.526544', b'1', 'RESET_PASSWORD'),
	(4, '964447', '2025-11-16 10:23:01.970348', 'ruia36791@163.com', '2025-11-16 10:33:01.970348', b'1', 'RESET_PASSWORD'),
	(5, '622281', '2025-11-16 10:26:01.168418', 'ruia36791@163.com', '2025-11-16 10:36:01.159859', b'1', 'RESET_PASSWORD'),
	(6, '408149', '2025-11-16 10:27:04.174080', 'ruia36791@163.com', '2025-11-16 10:37:04.174080', b'1', 'RESET_PASSWORD'),
	(7, '106013', '2025-11-16 10:31:57.933826', 'ruia36791@163.com', '2025-11-16 10:41:57.933826', b'1', 'RESET_PASSWORD'),
	(8, '698376', '2025-11-16 10:32:33.790451', 'ruia36791@163.com', '2025-11-16 10:42:33.790451', b'1', 'RESET_PASSWORD'),
	(9, '979175', '2025-11-16 10:32:42.857613', 'ruia36791@163.com', '2025-11-16 10:42:42.857613', b'1', 'RESET_PASSWORD'),
	(10, '044096', '2025-11-16 10:34:02.533564', 'ruia36791@163.com', '2025-11-16 10:44:02.533564', b'1', 'RESET_PASSWORD'),
	(11, '647763', '2025-11-16 10:36:40.129303', 'ruia36791@163.com', '2025-11-16 10:46:40.129303', b'1', 'RESET_PASSWORD'),
	(12, '989385', '2025-11-16 10:39:43.743962', 'ruia36791@163.com', '2025-11-16 10:49:43.743962', b'1', 'RESET_PASSWORD'),
	(13, '464465', '2025-12-19 03:55:15.465549', 'ruia36791@163.com', '2025-12-19 04:05:15.431755', b'1', 'RESET_PASSWORD'),
	(14, '309604', '2025-12-19 03:58:05.070250', '934705339@qq.com', '2025-12-19 04:08:05.069273', b'1', 'RESET_PASSWORD');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
