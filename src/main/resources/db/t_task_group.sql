/*
 Navicat Premium Data Transfer

 Source Server         : 10.80.20.8_3306
 Source Server Type    : MySQL
 Source Server Version : 50729
 Source Host           : 10.80.20.8:3306
 Source Schema         : ump_develop

 Target Server Type    : MySQL
 Target Server Version : 50729
 File Encoding         : 65001

 Date: 17/01/2022 23:57:21
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_task_group
-- ----------------------------
DROP TABLE IF EXISTS `t_task_group`;
CREATE TABLE `t_task_group` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `group_name` varchar(255) DEFAULT NULL,
  `desc` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
