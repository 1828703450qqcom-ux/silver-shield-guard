# 银盾守护 - 数据库设计文档

## 数据库概述

- **数据库名称**: yindun_shouhu_database
- **数据库类型**: Room (SQLite)
- **版本**: 1
- **加密**: AES-256 (敏感字段)

## 数据表清单

### 1. 用户表 (users)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INTEGER | 主键，自增 |
| name | TEXT | 用户姓名 |
| phone | TEXT | 手机号 |
| idNumber | TEXT | 身份证号（加密存储） |
| isElderly | INTEGER | 是否是老年人（0/1） |
| createdAt | INTEGER | 创建时间戳 |

### 2. 家庭关联表 (family_links)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INTEGER | 主键，自增 |
| elderlyUserId | INTEGER | 老年人用户ID |
| childUserId | INTEGER | 子女用户ID |
| relationship | TEXT | 关系类型 |
| createdAt | INTEGER | 创建时间戳 |

### 3. 账户表 (accounts)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INTEGER | 主键，自增 |
| userId | INTEGER | 用户ID |
| bankName | TEXT | 银行名称 |
| accountNumber | TEXT | 账号（加密存储） |
| accountType | TEXT | 账户类型 |
| balance | REAL | 余额 |
| createdAt | INTEGER | 创建时间戳 |

### 4. 交易记录表 (transactions)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INTEGER | 主键，自增 |
| accountId | INTEGER | 账户ID |
| amount | REAL | 交易金额 |
| counterparty | TEXT | 对手方 |
| description | TEXT | 交易描述 |
| transactionTime | INTEGER | 交易时间戳 |
| isSuspicious | INTEGER | 是否可疑（0/1） |
| riskScore | INTEGER | 风险分数 |
| createdAt | INTEGER | 创建时间戳 |

### 5. 风控规则表 (risk_rules)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INTEGER | 主键，自增 |
| ruleName | TEXT | 规则名称 |
| ruleType | TEXT | 规则类型 |
| parameters | TEXT | 参数（JSON） |
| riskLevel | INTEGER | 风险等级 |
| isActive | INTEGER | 是否启用（0/1） |

### 6. 诈骗话术表 (fraud_scripts)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INTEGER | 主键，自增 |
| category | TEXT | 诈骗类型 |
| title | TEXT | 话术标题 |
| scriptContent | TEXT | 话术内容（JSON） |
| keywords | TEXT | 关键词（JSON） |
| riskLevel | INTEGER | 风险等级 |
| dialect | TEXT | 方言 |
| createdAt | INTEGER | 创建时间戳 |
| updatedAt | INTEGER | 更新时间戳 |

### 7. 训练记录表 (training_records)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INTEGER | 主键，自增 |
| userId | INTEGER | 用户ID |
| scriptId | INTEGER | 话术ID |
| startTime | INTEGER | 开始时间戳 |
| endTime | INTEGER | 结束时间戳 |
| score | INTEGER | 得分 |
| identifiedKeywords | TEXT | 识别的关键词（JSON） |
| rewardAmount | REAL | 奖励金额 |
| completed | INTEGER | 是否完成（0/1） |

### 8. 积分账户表 (points_account)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INTEGER | 主键，自增 |
| userId | INTEGER | 用户ID |
| balance | REAL | 当前余额 |
| totalEarned | REAL | 累计获得 |
| totalSpent | REAL | 累计消费 |
| updatedAt | INTEGER | 更新时间戳 |

### 9. 通话记录表 (call_logs)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INTEGER | 主键，自增 |
| userId | INTEGER | 用户ID |
| phoneNumber | TEXT | 电话号码 |
| callTime | INTEGER | 通话时间戳 |
| duration | INTEGER | 通话时长（秒） |
| riskLevel | INTEGER | 风险等级 |
| riskReason | TEXT | 风险原因 |
| isWhitelisted | INTEGER | 是否白名单（0/1） |
| action | TEXT | 处理动作 |

### 10. 白名单表 (whitelist)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INTEGER | 主键，自增 |
| userId | INTEGER | 用户ID |
| phoneNumber | TEXT | 电话号码 |
| contactName | TEXT | 联系人姓名 |
| relationship | TEXT | 关系 |

### 11. 号码风险库 (phone_risk_db)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INTEGER | 主键，自增 |
| phoneNumber | TEXT | 电话号码 |
| riskType | TEXT | 风险类型 |
| reportCount | INTEGER | 举报次数 |
| lastReportTime | INTEGER | 最后举报时间戳 |

### 12. 案件表 (cases)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INTEGER | 主键，自增 |
| caseNumber | TEXT | 案件编号（唯一） |
| reporterId | INTEGER | 举报人ID |
| reporterType | TEXT | 举报人类型 |
| caseType | TEXT | 案件类型 |
| amount | REAL | 涉案金额 |
| suspectInfo | TEXT | 嫌疑人信息（JSON） |
| description | TEXT | 案件描述 |
| status | TEXT | 案件状态 |
| createdAt | INTEGER | 创建时间戳 |
| updatedAt | INTEGER | 更新时间戳 |

### 13. 证据表 (evidence)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INTEGER | 主键，自增 |
| caseId | INTEGER | 案件ID |
| evidenceType | TEXT | 证据类型 |
| filePath | TEXT | 文件路径 |
| fileHash | TEXT | 文件哈希 |
| uploadedAt | INTEGER | 上传时间戳 |

### 14. 案件进度表 (case_progress)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INTEGER | 主键，自增 |
| caseId | INTEGER | 案件ID |
| status | TEXT | 状态 |
| description | TEXT | 描述 |
| operator | TEXT | 操作人 |
| timestamp | INTEGER | 时间戳 |

### 15. 诈骗举报表 (fraud_reports)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INTEGER | 主键，自增 |
| phoneNumber | TEXT | 电话号码 |
| fraudType | TEXT | 诈骗类型 |
| fraudContent | TEXT | 诈骗内容 |
| location | TEXT | 位置 |
| reportTime | INTEGER | 举报时间戳 |
| isAnonymous | INTEGER | 是否匿名（0/1） |

## 索引设计

- accounts: userId
- transactions: accountId
- training_records: userId, scriptId
- points_account: userId
- call_logs: userId
- whitelist: userId
- cases: reporterId
- evidence: caseId
- case_progress: caseId

## 外键关系

- accounts → users (CASCADE)
- transactions → accounts (CASCADE)
- training_records → users, fraud_scripts (CASCADE)
- points_account → users (CASCADE)
- call_logs → users (CASCADE)
- whitelist → users (CASCADE)
- evidence → cases (CASCADE)
- case_progress → cases (CASCADE)

## 数据安全

1. **加密字段**: idNumber, accountNumber 使用AES-256加密
2. **密钥管理**: 使用Android Keystore
3. **本地存储**: 所有数据仅存储在本地
4. **数据删除**: 支持完全清除所有本地数据
