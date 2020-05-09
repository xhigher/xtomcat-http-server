/*
 Navicat Premium Data Transfer

 Source Server         : 106.12.103.19-测试mysql
 Source Server Type    : MySQL
 Source Server Version : 50728
 Source Host           : 106.12.103.19:3306
 Source Schema         : qs_business

 Target Server Type    : MySQL
 Target Server Version : 50728
 File Encoding         : 65001

 Date: 09/05/2020 10:40:36
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for config_error
-- ----------------------------
DROP TABLE IF EXISTS `config_error`;
CREATE TABLE `config_error` (
  `errorid` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '错误ID',
  `project` varchar(30) NOT NULL DEFAULT '' COMMENT '所属项目',
  `module` varchar(30) NOT NULL DEFAULT '' COMMENT '所属模块',
  `action` varchar(30) NOT NULL DEFAULT '' COMMENT '错误动作',
  `info` varchar(100) NOT NULL DEFAULT '' COMMENT '错误信息',
  `msg` varchar(200) NOT NULL DEFAULT '' COMMENT '错误详情',
  PRIMARY KEY (`errorid`),
  UNIQUE KEY `mixid` (`project`,`module`,`action`,`info`)
) ENGINE=InnoDB AUTO_INCREMENT=504 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for config_info
-- ----------------------------
DROP TABLE IF EXISTS `config_info`;
CREATE TABLE `config_info` (
  `cfgid` varchar(100) NOT NULL DEFAULT '' COMMENT '配置ID',
  `name` varchar(100) NOT NULL DEFAULT '' COMMENT '配置名',
  `fullname` varchar(200) NOT NULL DEFAULT '' COMMENT '配置全称',
  `upcfgid` varchar(100) NOT NULL DEFAULT '' COMMENT '父配置ID',
  PRIMARY KEY (`cfgid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for config_item
-- ----------------------------
DROP TABLE IF EXISTS `config_item`;
CREATE TABLE `config_item` (
  `cfgid_itemid` varchar(120) NOT NULL DEFAULT '',
  `itemid` varchar(20) NOT NULL DEFAULT '',
  `name` varchar(100) NOT NULL DEFAULT '',
  `fullname` varchar(200) NOT NULL DEFAULT '',
  `cfgid` varchar(100) NOT NULL DEFAULT '',
  `orderno` bigint(14) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`cfgid_itemid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;


SET FOREIGN_KEY_CHECKS = 1;
