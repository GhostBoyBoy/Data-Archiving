/*
 Navicat Premium Data Transfer

 Source Server         : 10.80.20.8_3306
 Source Server Type    : MySQL
 Source Server Version : 50729
 Source Host           : 10.80.20.8:3306
 Source Schema         : db_compare

 Target Server Type    : MySQL
 Target Server Version : 50729
 File Encoding         : 65001

 Date: 20/01/2022 15:09:58
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_target_task
-- ----------------------------
DROP TABLE IF EXISTS `t_target_task`;
CREATE TABLE `t_target_task` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `source_task_id` int(11) unsigned NOT NULL,
  `type` int(11) unsigned DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `table_name` varchar(255) DEFAULT NULL,
  `concurrency` int(11) DEFAULT NULL,
  `interval` int(11) DEFAULT NULL,
  `sleep` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_source_id` (`source_task_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
