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

 Date: 21/01/2022 18:01:49
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_source_task
-- ----------------------------
DROP TABLE IF EXISTS `t_source_task`;
CREATE TABLE `t_source_task` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `group` varchar(255) DEFAULT NULL,
  `task_name` varchar(255) DEFAULT NULL,
  `type` int(11) unsigned DEFAULT NULL,
  `status` int(11) unsigned DEFAULT '0',
  `url` varchar(255) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `table_name` var`target_count` int(11) DEFAULT NULL,char(255) DEFAULT NULL,
  `sql` varchar(255) DEFAULT NULL,
  `batch_size` int(11) DEFAULT NULL,
  `source_count` int(11) DEFAULT NULL,

  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
