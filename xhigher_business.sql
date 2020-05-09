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
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info` (
  `userid` char(12) NOT NULL COMMENT '用户ID',
  `username` char(16) NOT NULL DEFAULT '' COMMENT '用户名',
  `password` varchar(40) NOT NULL DEFAULT '' COMMENT '密码',
  `nickname` varchar(100) NOT NULL DEFAULT '' COMMENT '昵称',
  `avatar` varchar(200) NOT NULL DEFAULT '' COMMENT '头像地址',
  `score_left` bigint(14) unsigned NOT NULL DEFAULT '0' COMMENT '剩余积分',
  `score_total` bigint(14) unsigned NOT NULL DEFAULT '0' COMMENT '积分总数',
  `level` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '等级',
  `profile` text NOT NULL COMMENT '简介',
  `regtime` varchar(20) NOT NULL DEFAULT '' COMMENT '注册时间',
  `nickname_num` tinyint(2) unsigned NOT NULL DEFAULT '0' COMMENT '更新次数',
  `updatetime` varchar(20) NOT NULL DEFAULT '' COMMENT '更新时间',
  PRIMARY KEY (`userid`),
  UNIQUE KEY `idx_username` (`username`) USING BTREE,
  UNIQUE KEY `idx_nickname` (`nickname`) USING BTREE COMMENT '防止nickname重复'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

INSERT INTO `user_info`(`userid`, `username`, `password`, `nickname`, `avatar`, `score_left`, `score_total`, `level`, `profile`, `regtime`, `nickname_num`, `updatetime`) VALUES 
('j5399rep4444', '18800000001', '', '管理客服', 'https://zea8.oss-cn-beijing.aliyuncs.com/QMBK_20DC533195A24FFD9FB611515EBF875D.png?x-oss-process=image/auto-orient,1', 0, 0, 1, '大道至简 开箱即用', '2018-04-02 22:35:42', 0, '2018-04-02 22:35:42'),
('j5399res4444', '18800000002', '', 'ooo4', 'https://zea8.oss-cn-beijing.aliyuncs.com/QMBK_20DC533195A24FFD9FB611515EBF875D.png?x-oss-process=image/auto-orient,1', 0, 0, 1, '简介', '2018-04-02 22:35:42', 0, '2018-05-16 17:32:03'),
('j5399reu4444', '18800000003', '', '我是', 'https://zea8.oss-cn-beijing.aliyuncs.com/QMBK_AF870876653841CF88A783CAC59F19FC.jpg?x-oss-process=image/auto-orient,1', 0, 0, 1, '我是谁', '2018-04-02 22:35:42', 0, '2019-03-08 21:23:41'),
('j5399rev4444', '18800000004', '', 'drt', 'https://zea8.oss-cn-beijing.aliyuncs.com/QMBK_22B8D2EB4E044CEEAB25CEA7343FE219.png?x-oss-process=image/auto-orient,1', 0, 0, 1, '大道至简 开箱即用', '2018-04-02 22:35:42', 0, '2019-03-08 18:21:43'),
('j5399rex4444', '18800000005', '', 'xunlijiang001', 'https://zea8.oss-cn-beijing.aliyuncs.com/QMBK_20DC533195A24FFD9FB611515EBF875D.png?x-oss-process=image/auto-orient,1', 0, 0, 1, '大道至简 开箱即用', '2018-04-02 22:35:42', 0, '2018-05-17 21:55:54'),
('j5399rf24444', '18800000006', '', 'lx123', 'https://zea8.oss-cn-beijing.aliyuncs.com/QMBK_20DC533195A24FFD9FB611515EBF875D.png?x-oss-process=image/auto-orient,1', 0, 0, 1, '大道至简 开箱即用', '2018-04-02 22:35:42', 0, '2018-08-24 18:59:55'),
('j5399rf34444', '18800000007', '', 'Miaos', 'https://zea8.oss-cn-beijing.aliyuncs.com/QMBK_8A44E8FB39234625A22057590AE9B42C.png?x-oss-process=image/auto-orient,1', 0, 0, 1, '大道至简 开箱即用', '2018-04-02 22:35:42', 0, '2019-08-25 23:01:56'),
('j5399rf64444', '18800000008', '', 'yyy12345', 'https://zea8.oss-cn-beijing.aliyuncs.com/QMBK_20DC533195A24FFD9FB611515EBF875D.png?x-oss-process=image/auto-orient,1', 0, 0, 1, '大道至简 开箱即用', '2018-04-02 22:35:42', 0, '2018-05-17 21:12:59'),
('j5399rf74444', '18800000009', '', 'zk', 'https://zea8.oss-cn-beijing.aliyuncs.com/QMBK_D7754152F40A4D2C94FA2E459BD91CFE.png?x-oss-process=image/auto-orient,1', 0, 0, 1, '大道至简 开箱即用', '2018-04-02 22:35:42', 0, '2018-06-22 01:17:14'),
('j5399rf84444', '18800000010', '', 'zk1', 'https://zea8.oss-cn-beijing.aliyuncs.com/QMBK_20DC533195A24FFD9FB611515EBF875D.png?x-oss-process=image/auto-orient,1', 0, 0, 1, '大道至简 开箱即用', '2018-04-02 22:35:42', 0, '2020-04-30 14:36:22');


SET FOREIGN_KEY_CHECKS = 1;
